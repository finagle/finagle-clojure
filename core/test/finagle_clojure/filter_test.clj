(ns finagle-clojure.filter-test
  (:require [finagle-clojure.filter :refer :all]
            [finagle-clojure.service :as svc]
            [finagle-clojure.futures :as f]
            [midje.sweet :refer :all]))

;; set *warn-on-reflection* after loading midje to skip its reflection warnings
(set! *warn-on-reflection* true)

(let [filter-a (proxy [com.twitter.finagle.Filter] [] 
                        (apply [req service] (f/value :filter-a)))
      filter-b (proxy [com.twitter.finagle.Filter] [] 
                        (apply [req service] (f/value :filter-b)))
      service (proxy [com.twitter.finagle.Service] [] 
                         (apply [req] (f/value :service)))]
  (facts "and-then"
    (-> service (svc/apply :input) f/await) => :service
    (-> filter-a (and-then service) (svc/apply :input) f/await) => :filter-a
    (-> filter-b (and-then filter-a) (and-then service) (svc/apply :input) f/await) => :filter-b)
  (facts "chain"
    (-> service (svc/apply :input) f/await) => :service
    (-> (chain filter-a service) (svc/apply :input) f/await) => :filter-a
    (-> (chain filter-b filter-a service) (svc/apply :input) f/await) => :filter-b))
