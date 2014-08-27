(ns leiningen.finagle-clojure
  (:require [leiningen.javac]
            [robert.hooke]
            [clojure.java.io :as io]))

(defn scrooge
  "Compile Thrift definitions into Java classes using Scrooge.

  Scrooge is a Thrift compiler that generates classes with 
  Finagle appropriate interfaces (wraps Service method return values in Future).
  
  This task expects the following config to present on the project:

    :finagle-clojure {:thrift-source-path \"\" :thrift-output-path \"\"}"
  [project]
   (let [project-root (:root project)
         source-path (get-in project [:finagle-clojure :thrift-source-path])
         raw-dest-path (get-in project [:finagle-clojure :thrift-output-path])]
     (if (and source-path raw-dest-path)
       (let [absolute-dest-path (->> raw-dest-path (io/file project-root) (.getAbsolutePath))
             thrift-files (->> source-path
                               (io/file project-root)
                               file-seq 
                               (filter #(.isFile %))
                               (map #(.getPath %)))
             scrooge-args (concat ["--finagle" "--skip-unchanged" "--language" "java" "--dest" absolute-dest-path] thrift-files)]
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
    "scrooge" (scrooge project)))

(defn activate []
  (robert.hooke/add-hook #'leiningen.javac/javac
                         #'javac-hook))
