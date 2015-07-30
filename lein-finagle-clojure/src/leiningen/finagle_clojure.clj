(ns leiningen.finagle-clojure
  (:require [leiningen.javac]
            [robert.hooke]
            [clojure.java.io :as io]))

(defn- thrift-files
  [project-root source-path]
  (->> source-path
    (io/file project-root)
    (file-seq)
    (filter #(and (.isFile %) (.endsWith (.getName %) ".thrift")))
    (map #(.getPath %))))

(defn scrooge
  "Compile Thrift definitions into Java classes using Scrooge.

  Scrooge is a Thrift compiler that generates classes with 
  Finagle appropriate interfaces (wraps Service method return values in Future).

  Scrooge also provides a Thrift linter that can be run before compilation. Lint errors will
  prevent compilation. Pass :lint as an argument to this task to enable linting.
  Additional args passed after :lint will be passed to the linter as well.
  See https://twitter.github.io/scrooge/Linter.html or run :lint with --help for more info.
  
  This task expects the following config to present on the project:

    :finagle-clojure {:thrift-source-path \"\" :thrift-output-path \"\"}

  Example usage:

    lein finagle-clojure scrooge # compiles thrift files
    lein finagle-clojure scrooge :lint # lints thrift files before compilation
    lein finagle-clojure scrooge :lint --help # shows available options for the linter
    lein finagle-clojure scrooge :lint -w # show linter warnings as well (warnings won't prevent compilation)"
  [project & options]
  (let [subtask (first options)
        project-root (:root project)
        source-path (get-in project [:finagle-clojure :thrift-source-path])
        raw-dest-path (get-in project [:finagle-clojure :thrift-output-path])]
    (if-not (and source-path raw-dest-path)
      (leiningen.core.main/info "No config found for lein-finagle-clojure, not compiling Thrift for" (:name project))
      (let [absolute-dest-path (->> raw-dest-path (io/file project-root) (.getAbsolutePath))
            thrift-files (thrift-files project-root source-path)
            scrooge-args (concat ["--finagle" "--skip-unchanged" "--language" "java" "--dest" absolute-dest-path] thrift-files)]
        (when (= subtask ":lint")
          (let [default-args ["--disable-rule" "Namespaces"]
                additional-args (rest options)
                linter-args (concat default-args additional-args thrift-files)]
            (leiningen.core.main/info "Linting Thrift files:" thrift-files)
            (com.twitter.scrooge.linter.Main/main (into-array String linter-args))))
        (leiningen.core.main/info "Compiling Thrift files:" thrift-files)
        (leiningen.core.main/debug "Calling scrooge with parameters:" scrooge-args)
        (com.twitter.scrooge.Main/main (into-array String scrooge-args))))))

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
  (robert.hooke/add-hook #'leiningen.javac/javac #'javac-hook))
