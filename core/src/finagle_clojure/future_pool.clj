(ns finagle-clojure.future-pool
  "Functions for creating & using `com.twitter.util.FuturePool`.
  FuturePools can be used to run blocking code on a thread separate from Finagle.
  This allows synchronous libraries to be used asynchronously in an application using Finagle.
  A Future will be returned which allows for easy integration to other asynchronous Finagle code."
  (:require [finagle-clojure.scala :as scala])
  (:import [com.twitter.util Future FuturePool FuturePools]
           [java.util.concurrent ExecutorService]))

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
  (FuturePools/newFuturePool executor-service))

(defn ^FuturePool interruptible-future-pool
  "FuturePools can be used to run synchronous code on a thread pool.
  Once a FuturePool has been created tasks can be run on it, a Future
  will be returned representing its completion.

  This function returns an InterruptibleExecutorServiceFuturePool, similar to an ExecutorServiceFuturePool
  but interrupts on the Futures returned by [[run*]] or [[run]] will attempt to propagate to
  the backing ExecutorService.

  *Arguments*:

    * `executor-service`: the `java.util.concurrent.ExecutorService` that will back the returned FuturePool.

  *Returns*:

    A new InterruptibleExecutorServiceFuturePool.

  See: [[run*]] & [[run]]"
  [^ExecutorService executor-service]
  (FuturePools/newInterruptibleFuturePool executor-service))

(defn ^FuturePool unbounded-future-pool
  "FuturePools can be used to run synchronous code on a thread pool.
  Once a FuturePool has been created tasks can be run on it, a Future
  will be returned representing its completion.

  This function will return a FuturePool backed by an unbounded, cached, thread pool.
  While this FuturePool may be suitable for IO concurrency, computational concurrency
  may require finer tuning (see [[future-pool]]).

  *Returns*:

    A new FuturePool.

  See: [[run*]] & [[run]]"
  []
  (FuturePools/unboundedPool))

(defn ^FuturePool interruptible-unbounded-future-pool
  "FuturePools can be used to run synchronous code on a thread pool.
  Once a FuturePool has been created tasks can be run on it, a Future
  will be returned representing its completion.

  This function will return a FuturePool backed by an unbounded, cached, thread pool.
  While this FuturePool may be suitable for IO concurrency, computational concurrency
  may require finer tuning (see [[interruptible-future-pool]]).

  Interrupts on the Futures returned by [[run*]] or [[run]] will attempt to propagate to
  the backing thread pool.

  *Arguments*:

    * `executor-service`: the `java.util.concurrent.ExecutorService` that will back the returned FuturePool.

  *Returns*:

    A new InterruptibleExecutorServiceFuturePool.

  See: [[run*]] & [[run]]"
  [^ExecutorService executor-service]
  (FuturePools/newInterruptibleFuturePool executor-service))

(defn ^FuturePool immediate-future-pool
  "This function returns a FuturePool that will execute tasks on the calling thread,
  rather than asynchronously. This should really only be used in tests.

  *Returns*:

    A new FuturePool that will execute on the calling thread.

  See: [[run*]] & [[run]]"
  []
  (FuturePools/IMMEDIATE_POOL))

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
