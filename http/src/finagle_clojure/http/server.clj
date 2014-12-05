(ns finagle-clojure.http.server
  (:import (com.twitter.finagle.builder Server)
           (com.twitter.finagle Service)
           (com.twitter.finagle.http Http))
  (:require [finagle-clojure.core.builder :refer :all]))

(def http
  "The HTTP codec."
  (Http/get))

(defn ^Server http-server
  "Creates a new HttpServer with the given name, port, service and codec. Convenient but not as full-featured as a
  custom builder.

  *Arguments*:

    * `name`: the name of this server
    * `port`: the port to bind this server to
    * `svc`: the Service to use to respond to requests

  *Returns*:

    a running Server with the HTTP codec"
  [^String name port ^Service svc]
  (-> (builder)
      (named name)
      (bind-to port)
      (codec http)
      (build svc)))

