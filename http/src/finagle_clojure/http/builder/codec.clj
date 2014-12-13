(ns finagle-clojure.http.builder.codec
  (:import (com.twitter.finagle.http Http)))

(def http
  "The HTTP codec, for use with Finagle client and server builders."
  (Http/get))
