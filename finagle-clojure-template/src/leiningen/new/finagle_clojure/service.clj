(ns {{ns}}
  (:import [{{thrift-ns}} {{service-name}}])
  (:require [finagle-clojure.futures :as f]
            [finagle-clojure.{{project-type}} :as {{project-type}}])
  (:gen-class))

(defn make-service
  []
  ({{project-type}}/service {{service-name}}
    ;; TODO implement service methods
    ))

(defn -main
  [& args]
  (f/await ({{project-type}}/serve ":9999" (make-service))))
