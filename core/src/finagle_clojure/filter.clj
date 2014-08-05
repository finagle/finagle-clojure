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
    `filter` and then pass through `next`.

  See: [[chain]] for a higher level interface."
  [^Filter filter next] 
  (.andThen filter next))

(defn chain
  "Compose a series of `Filter`s & `Service`s together.
  This is shorthand for calling [[and-then]] multiple times.

  *Arguments*:

    * `filters-and-service`: variadic argument, the `Filter`s & `Service` you want to compose together.

  *Returns*:
  
    A new `Service`.
  
  See: [[and-then]]."
  [& filters-and-service]
  (reduce and-then filters-and-service))
