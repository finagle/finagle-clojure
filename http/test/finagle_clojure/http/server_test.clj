(ns finagle-clojure.http.server-test
  (:import (com.twitter.finagle.builder ServerBuilder IncompleteSpecification Server)
           (com.twitter.finagle.http Request))
  (:require [finagle-clojure.http.server :refer :all]
            [finagle-clojure.server :refer :all]
            [finagle-clojure.http.message :as m]
            [finagle-clojure.service :as s]
            [finagle-clojure.futures :as f]
            [midje.sweet :refer :all]
            [clj-http.client :as http]))

(def test-service
  (s/mk [^Request req]
    (f/value
      (-> (m/response 200)
          (m/set-content-string "Hello, World")))))

(facts "about the ServerBuilder"
  (class (builder)) => ServerBuilder
  (build (builder) nil) => (throws IncompleteSpecification)

  (class (bind-to (builder) 3000)) => ServerBuilder
  (class (named (builder) "test")) => ServerBuilder

  (let [server (-> (builder)
                   (named "test")
                   (bind-to 3000)
                   (codec http)
                   (build test-service))]
    (instance? Server server) => true
    (-> "http://localhost:3000" (http/get) (:body)) => "Hello, World"
    (close! server))

  (let [server (http-server "test" 3000 test-service)]
    (instance? Server server) => true
    (-> "http://localhost:3000" (http/get) (:body)) => "Hello, World"
    (close! server))
  )
