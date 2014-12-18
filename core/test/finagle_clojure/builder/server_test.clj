(ns finagle-clojure.builder.server-test
  (:import (com.twitter.finagle.builder Server ServerBuilder IncompleteSpecification))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.builder.server :refer :all]
            [finagle-clojure.service :as service]
            [finagle-clojure.futures :as f]
            [finagle-clojure.scala :as scala]))

(def empty-service
  (service/mk [req]
    (f/value nil)))

(facts "builder"
  (->
    (builder)
    (class))
  => ServerBuilder

  (-> (builder)
      (build nil))
  => (throws IncompleteSpecification)

  (-> (builder)
      (bind-to 3000)
      (class))
  => ServerBuilder

  (-> (builder)
      (bind-to 3000)
      (build empty-service))
  => (throws IncompleteSpecification)

  (let [s (-> (builder)
              (bind-to 3000)
              (named "foo")
              (build empty-service))]
    (ancestors (class s))
    => (contains Server)
    (f/await (close! s))
    => scala/unit)

  (-> (builder)
      (bind-to 3000)
      (named "foo")
      (build empty-service)
      (close!)
      (f/await)) => scala/unit)
