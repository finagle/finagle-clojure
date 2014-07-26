(ns finagle-clojure.filter
  "Wrapper around `com.twitter.finagle.Filter`.
  Use `and-then` to compose `Filter`s & `Service`s together."
  (:import [com.twitter.finagle Filter]))

(defn and-then
  "Compose a `Filter` and `Filter` or `Service` together.

  *Arguments*:

    * `filter`: a `Filter`.
    * `next`: a `Filter` or `Service`.

  *Returns*:

    A new `Service` that will first pass the request through
    `filter` and then pass through `next`."
  [^Filter filter next] 
  (.andThen filter next))
