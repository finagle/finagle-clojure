(ns finagle-clojure.future-pool
  "Functions for creating & using `com.twitter.util.FuturePool`.
  FuturePools can be used to run blocking code on a thread separate from Finagle.
  This allows synchronous libraries to be used asynchronously in an application using Finagle.
  A Future will be returned which allows for easy integration to other asynchronous Finagle code."
  (:require [finagle-clojure.scala :as scala])
  (:import [com.twitter.util ExecutorServiceFuturePool Future FuturePool]
           [java.util.concurrent ExecutorService]))

;; TODO: @samn 06/17/14 add support for InterruptibleExecutorServiceFuturePool

(defn ^FuturePool future-pool
  "FuturePools can be used to run synchronous code on a thread pool.
  Once a FuturePool has been created tasks can be run on it, a Future
  will be returned representing its completion.

  *Arguments*:

    * `executor-service`: the `java.util.concurrent.ExecutorService` that will back the returned FuturePool.

  *Returns*:

    A new ExecutorServiceFuturePool.

  See: [[run*]] & [[run]]"
  [^ExecutorService executor-service]
  (ExecutorServiceFuturePool. executor-service))

(defn ^Future run*
  "Run scala.Function0 or Clojure fn `fn0` on FuturePool `future-pool`.
  A Future will be returned representing the async application of `fn0`.

  *Arguments*:

    * `future-pool`: the FuturePool on which `fn0` will run
    * `fn0`: a scala.Function0 or Clojure fn to apply asynchronously

  *Returns*:

    A Future that will be defined when `fn0` has run.
    Its value will be the result of applying `fn0`, or a thrown exception.

  See: [[scala/Function0]] & [[run]]"
  [^FuturePool future-pool fn0]
  (.apply future-pool (scala/lift->fn0 fn0)))

(defmacro run
  "Sugar for creating a scala.Function0 and passing it to [[run*]].
  Run `body` on FuturePool `future-pool`.
  A Future will be returned representing the async application of `body`.

  *Arguments*:

    * `future-pool`: the FuturePool on which `body` will run
    * `body`: will execute asynchronously to relative to the current thread on `future-pool`

  *Returns*:

    A Future that will be defined when `body` has run.
    Its value will be the result of applying `body`, or a thrown exception.

  See: [[run*]] & [[scala/Function0]]"
  [^FuturePool future-pool & body]
  `(run* ~future-pool (scala/Function0 ~@body)))
