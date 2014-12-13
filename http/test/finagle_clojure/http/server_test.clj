(ns finagle-clojure.http.server-test
  (:import (com.twitter.finagle.http Request)
           (com.twitter.finagle Service))
  (:require [finagle-clojure.http.server :as server]
            [finagle-clojure.http.client :as client]
            [finagle-clojure.http.message :as m]
            [finagle-clojure.service :as s]
            [finagle-clojure.futures :as f]
            [midje.sweet :refer :all]))

(def ^Service hello-world
  (s/mk [^Request req]
    (f/value
      (-> (m/response 200)
          (m/set-content-string "Hello, World")))))

(facts "about the HTTP server"
  (let [s (server/serve ":3000" hello-world)
        c (client/service ":3000")]
    (-> c (s/apply (m/request "/")) (f/await) (m/content-string)) => "Hello, World"
    ))