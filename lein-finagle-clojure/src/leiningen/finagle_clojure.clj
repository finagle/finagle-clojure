(ns leiningen.finagle-clojure
  (:require [leiningen.javac]
            [robert.hooke]
            [clojure.java.io :as io]))

(defn thrift-files
  [project-root source-path]
  (->> source-path
       (io/file project-root)
       file-seq
       (filter #(.isFile %))
       (map #(.getPath %))))

(defn scrooge
  "Compile Thrift definitions into Java classes using Scrooge.

  Scrooge is a Thrift compiler that generates classes with
  Finagle appropriate interfaces (wraps Service method return values in Future).

  This task expects the following config to present on the project and will not run if it's absent:

    :finagle-clojure {:thrift-source-path \"\" :thrift-output-path \"\"}"
  [project & options]
   (let [subtask (first options)
         project-root (:root project)
         source-path (get-in project [:finagle-clojure :thrift-source-path])
         raw-dest-path (get-in project [:finagle-clojure :thrift-output-path])]
     (if (and source-path raw-dest-path)
       (let [absolute-dest-path (->> raw-dest-path (io/file project-root) (.getAbsolutePath))
             thrift-files (thrift-files project-root source-path)
             scrooge-args (concat ["--finagle" "--skip-unchanged" "--language" "java" "--dest" absolute-dest-path] thrift-files)]
         (prn subtask options)
         (when (= subtask ":lint")
           (leiningen.core.main/info "Linting Thrift files:" thrift-files)
           (com.twitter.scrooge.linter.Main/main (into-array thrift-files)))
         (leiningen.core.main/info "Compiling Thrift files:" thrift-files)
         (leiningen.core.main/debug "Calling scrooge with parameters:" scrooge-args)
         (com.twitter.scrooge.Main/main (into-array scrooge-args)))
       (leiningen.core.main/info "No config found for lein-finagle-clojure, not compiling Thrift for" (:name project)))))

(defn javac-hook
  [f project & args]
  (scrooge project)
  (apply f project args))

(defn finagle-clojure
  "Adds a hook to lein javac to compile Thrift files first."
  {:help-arglists '([scrooge])
   :subtasks [#'scrooge]}
  [project subtask & args]
  (case subtask
    "scrooge" (apply scrooge project args)))

(defn activate []
  (robert.hooke/add-hook #'leiningen.javac/javac
                         #'javac-hook))
