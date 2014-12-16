(ns finagle-clojure.http.integration-test
  (:import (com.twitter.finagle.http Request)
           (com.twitter.finagle Service))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.scala :as scala]
            [finagle-clojure.futures :as f]
            [finagle-clojure.http.message :as m]
            [finagle-clojure.service :as s]
            [finagle-clojure.http.client :as client]
            [finagle-clojure.http.server :as server]))

(def ^Service hello-world
  (s/mk [^Request req]
    (f/value
      (-> (m/response 200)
          (m/set-content-string "Hello, World")))))

(fact "performs a full-stack integration call between client and server"
  (let [s (server/serve ":3000" hello-world)
        c (client/service ":3000")]
    (-> c (s/apply (m/request "/"))
        (f/await)
        (m/content-string)) => "Hello, World"
    (f/await (.close s)) => scala/unit
    ))