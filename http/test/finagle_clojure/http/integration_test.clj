(ns finagle-clojure.http.integration-test
  (:import (com.twitter.finagle.http Request Response)
           (com.twitter.finagle Service))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.scala :as scala]
            [finagle-clojure.futures :as f]
            [finagle-clojure.http.message :as m]
            [finagle-clojure.service :as s]
            [finagle-clojure.http.client :as http-client]
            [finagle-clojure.http.server :as http-server]
            [finagle-clojure.builder.client :as builder-client]
            [finagle-clojure.builder.server :as builder-server]
            [finagle-clojure.http.builder.codec :as http-codec]
            ))

(def ^Service hello-world
  (s/mk [^Request req]
    (f/value
      (-> (m/response 200)
          (m/set-content-string "Hello, World")))))

(facts "stack-based server and client"
  (fact "performs a full-stack integration call"
    (let [s (http-server/serve ":3000" hello-world)
          c (http-client/service ":3000")]
      (-> (s/apply c (m/request "/"))
          (f/await)
          (m/content-string))
      => "Hello, World"

      (f/await (http-client/close! c))
      => scala/unit

      (f/await (http-server/close! s))
      => scala/unit
      )))

(facts "builder-based server and client"
  (fact "performs a full-stack integration call"
    (let [s (->
              (builder-server/builder)
              (builder-server/codec http-codec/http)
              (builder-server/bind-to 3000)
              (builder-server/named "test")
              (builder-server/build hello-world))
          c (->
              (builder-client/builder)
              (builder-client/codec http-codec/http)
              (builder-client/hosts "localhost:3000")
              (builder-client/build))]
      (-> (s/apply c (m/request "/"))
          (f/map [rep] (Response/apply rep))
          (f/await)
          (m/content-string))
      => "Hello, World"

      (f/await (builder-client/close! c))
      => scala/unit

      (f/await (builder-server/close! s))
      => scala/unit
      )))