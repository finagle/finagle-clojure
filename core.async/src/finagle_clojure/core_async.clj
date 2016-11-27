(ns finagle-clojure.core-async
  "Adapters to use Futures with core.async."
  (:require [finagle-clojure.futures :as f]
            [finagle-clojure.scala :as scala]
            [clojure.core.async :as a])
  (:import [com.twitter.util Future]))

(defn ^:no-doc throw-error
  [o]
  (if (instance? Throwable o)
    (throw o)
    o))

;; this needs to be a macro so it expands in the scope of the enclosing go block
;; otherwise the async take complains that it isn't in a go block
;; throw-error needs to be public so it's visible in the macro
(defmacro <?
  "Similar to `clojure.core.async/<!`, but will throw instances of Throwable.

  *Arguments*:

    * `c`: a core.async chan

  *Returns*:
  
  The value from `c`, or throws it if it's `Throwable`."
  [c]
  `(throw-error (a/<! ~c)))

(defn <??
  "Similar to `clojure.core.async/<!!`, but will throw instances of Throwable.

  *Arguments*:

    * `c`: a core.async chan

  *Returns*:
  
  The value from `c`, or throws it if it's `Throwable`."
  [c]
  (throw-error (a/<!! c)))

(defn- enqueue-to-chan
  [c]
  (fn [v]
    ;; Close the channel after the value has been put into channel c
    ;; to make sure it isn't closed before the value has been submitted.
    (a/put! c v (fn [_] (a/close! c)))
    scala/unit))

(defn future->chan
  "Enqueues the value or Throwable that a Future is defined with to a channel.
  If no chan is provided a new `promise-chan` will be created and returned.

  *Arguments*:

    * `f`: a Future
    * `c`: (optional) a core.async chan

  *Returns*:

  The chan to which the result of Future `f` will be enqueued.
  
  See the helper fns [[<??]] & [[<?]] to take a value fro a chan and throw
  it if it's an instance of `Throwable`."
  ([^Future f]
   (future->chan f (a/promise-chan)))
  ([^Future f c]
   (f/on-success* f (enqueue-to-chan c))
   (f/on-failure* f (enqueue-to-chan c))
   c))
