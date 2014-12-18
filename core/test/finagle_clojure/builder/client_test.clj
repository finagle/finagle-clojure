(ns finagle-clojure.builder.client-test
  (:import (com.twitter.finagle.builder ClientBuilder IncompleteSpecification)
           (com.twitter.finagle Service))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.builder.client :refer :all]
            [finagle-clojure.futures :as f]
            [finagle-clojure.scala :as scala]))

(facts "builder"
  (->
    (builder)
    (class))
  => ClientBuilder

  (-> (builder)
      (build))
  => (throws IncompleteSpecification)

  (-> (builder)
      (class))
  => ClientBuilder

  (-> (builder)
      (build))
  => (throws IncompleteSpecification)

  (let [s (-> (builder)
              (hosts "localhost:3000")
              (build))]
    (ancestors (class s))
    => (contains Service)
    (f/await (close! s))
    => scala/unit))
