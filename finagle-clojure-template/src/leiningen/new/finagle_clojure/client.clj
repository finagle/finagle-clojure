(ns {{ns}}
  (:import [{{thrift-ns}} {{service-name}}])
  (:require [finagle-clojure.futures :as f]
            [finagle-clojure.{{project-type}} :as {{project-type}}]))

(defn make-client
  [address]
  ({{project-type}}/client address {{service-name}}))
