(ns {{ns}}
  (:import [{{thrift-ns}} {{service-name}}])
  (:require [finagle-clojure.futures :as f]
            [finagle-clojure.thrift :as thrift]))

(defn make-client
  [address]
  (thrift/client address {{service-name}}))
