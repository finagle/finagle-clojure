(ns finagle-clojure.service
  "Functions for creating `Service`s.
  `Service` is the main abstraction for both clients & servers.
  It is where business logic is implemented.
  `Service`s can be created by using [[mk*]] & [[mk]],
  or by proxying `com.twitter.finagle.Service`.

  If creating an instance of `Service` with proxy
  at least the apply method needs to be implemented.
  Default implementations are provided for all other methods.

  Instance methods of `Service` that can be overridden:
  - (apply [req]) ; => Future[response]
  - (close [time]) ; => Future[Unit]
  - (isAvailable []) ; => boolean
  - (map [fn1]) ; => Service[new-response-type]"
  (:refer-clojure :exclude [apply])
  (:require [finagle-clojure.scala :as scala]
            [finagle-clojure.duration :as duration])
  (:import [com.twitter.finagle Service Service$]
           [com.twitter.util Time]))

(defn ^Service mk*
  "Create a new `Service.`
  The `apply` method will be implemented with `fn1`

  *Arguments*:

    * `fn1`: a `scala.Function1` that will be used to implement `apply`.

  *Returns*:

    a new `Service`.

  See: [[mk]]"
  [^scala.Function1 fn1]
  (.mk Service$/MODULE$ fn1))

(defmacro mk
  "Sugar for creating a `scala.Function` and calling `mk*`.
  Returns a new `Service` with the `apply` method will be implemented with `body`.

  *Arguments*:

    * `arg-binding`: is a vector with 1 element, the name to bind the value of the argument to `apply`.
    * `body`: the implementation of `Service#apply`

  *Returns*:

    a new `Service`.

  See: [[mk*]]"
  [arg-binding & body]
  `(mk* (scala/Function ~arg-binding ~@body)))

(defn rescue
  "Returns a service wrapping `svc` that will lift uncaught exceptions thrown in `apply` to a Future[Exception].

  *Arguments*:

    * `svc`: a `Service`.

  *Returns*:

    a new `Service`."
  [^Service svc]
  (.rescue Service$/MODULE$ svc))

(defn apply
  "Sugar for calling `Service#apply`.
  Evaluates a request using Service `svc` and returns the value calculated.

  *Arguments*:

    * `svc`: a `Service`.
    * `request`: the input to `svc`.

  *Returns*:

    the value of `svc#apply."
  [^Service svc request]
  (.apply svc request))

(defn available?
  "Is `Service` `svc` able to respond to requests?

  *Arguments*:

    * `svc`: a `Service`.

  *Returns*:

    `true` if the service is available, `false` otherwise."
  [^Service svc]
  (.isAvailable svc))

(defn close!
  "Mark `Service` `svc` as no longer in use. No further requests should be sent.

  *Arguments*:

    * `svc`: a `Service`.
    * `deadline-time`: a `com.twitter.util.Time` instance describing how long to wait for the close to complete.

  *Returns*:

    A `Future` that will be complete when `svc` has closed.

  See [[finagle-clojure.duration/->Time]]"
  ([^Service svc ^Time deadline-time]
   (.close svc deadline-time)))
