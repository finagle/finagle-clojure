(ns finagle-clojure.http.server-test
  (:import (com.twitter.finagle.builder ServerBuilder IncompleteSpecification)
           (com.twitter.finagle HttpServer))
  (:require [finagle-clojure.http.server :refer :all]
            [midje.sweet :refer :all]))

(facts "about the ServerBuilder"
  (class (server-builder)) => ServerBuilder
  (build (server-builder) nil) => (throws IncompleteSpecification)

  (class (bind-to (server-builder) 3000)) => ServerBuilder
  (class (named (server-builder) "test")) => ServerBuilder)
