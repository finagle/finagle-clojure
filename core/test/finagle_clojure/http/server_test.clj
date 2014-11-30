(ns finagle-clojure.http.server-test
  (:import (com.twitter.finagle.builder ServerBuilder IncompleteSpecification))
  (:require [finagle-clojure.http.server :refer :all]
            [midje.sweet :refer :all]))

(facts "about the ServerBuilder"
  (class (server-builder)) => ServerBuilder
  (build (server-builder) nil) => (throws IncompleteSpecification))