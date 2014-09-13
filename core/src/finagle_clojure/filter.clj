(ns finagle-clojure.filter
  "Wrapper around `com.twitter.finagle.Filter`.
  Filters are like middleware and can be used to add funcionality common 
  to many Services, like instrumentation or backpressure.
  
  Filters are composed with other Filters or Services resulting in a Service.
  Use `and-then` or `chain` to compose Filters & Services together."
  (:import [com.twitter.finagle Filter]))

(defn and-then
  "Compose a Filter and a Filter or Service together.

  *Arguments*:

    * `filter`: a Filter.
    * `next`: a Filter or Service.

  *Returns*:

    A new Service that will first send a request through
    `filter` and then pass the result to `next`.

  See: [[chain]] for a higher level interface."
  [^Filter filter next] 
  (.andThen filter next))

(defn chain
  "Compose a series of Filters & Services together.
  This is shorthand for calling [[and-then]] multiple times.

  *Arguments*:

    * `filters-and-service` (variadic): the Filters & Service you want to compose together.

  *Returns*:
  
    A new Service.
  
  See: [[and-then]]."
  [& filters-and-service]
  (reduce and-then filters-and-service))
