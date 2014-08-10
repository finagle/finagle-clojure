(ns lein-finagle-clojure.plugin
  (:require [leiningen.finagle-clojure]))

(defn hooks []
  (leiningen.finagle-clojure/activate))
