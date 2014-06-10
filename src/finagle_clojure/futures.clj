(ns finagle-clojure.futures
  (:refer-clojure :exclude [for future get map])
  (:require [finagle-clojure.scala :as scala])
  (:import [com.twitter.util Future]))

;; call this mapcat? which is the Clojure name for flatMap?
;; or is that too confusing
(defmacro flatmap
  "Call flatMap on twitter-future.
  param-binding is a vector with 1 element, the name to bind the value of the flatMap to."
  [twitter-future param-binding & body]
  (let [tagged-future (vary-meta twitter-future assoc :tag `Future)]
    `(.flatMap ~tagged-future
        (scala/Function1 ~param-binding
          ~@body))))

(defmacro map
  "Call map on twitter-future.
  param-binding is a vector with 1 element, the name to bind the value of the flatMap to."
  [twitter-future param-binding & body]
  (let [tagged-future (vary-meta twitter-future assoc :tag `Future)]
    `(.map ~tagged-future
        (scala/Function1 ~param-binding
          ~@body))))

(defn get
  "Get the value for Future f.
  Blocks until the Future is ready."
  [^Future f]
  (.get f))

(defn ^Future value
  "Returns a future with the constant value v"
  [v]
  (Future/value v))

(defn ^Future exception
  [^Throwable t]
  (Future/exception t))

;; TODO support non-future values in intermediate bindings
;; like require <- or :<- or something for future values to do flatmap
(defmacro for
  "Like a scala for comprehension with Futures,
  bindings are pairs of name future,
  body is executed when all Futures are ready.
  Bindings can refer to Futures defined before them
  (which will be realized)."
  [bindings & body]
  {:pre [(coll? bindings) (even? (count bindings))]}
  (let [[name val] (take 2 bindings)]
    (if-not (or (nil? name) (nil? val))
      `(flatmap ~val [~name]
          (for ~(drop 2 bindings) ~@body))
      `(do ~@body))))

(defn collect
  "Takes a seq of Futures, returns a Future of a seq of their values."
  [future-seq]
  (flatmap (Future/collect (scala/seq->scala-list future-seq))
    [scala-seq]
    (value (scala/scala-seq->List scala-seq))))

(defn rescue*
  [^Future f ^scala.PartialFunction pfn]
  (.rescue f pfn))

(defmacro rescue
  [^Future f arg-binding & body]
  `(rescue* ~f (scala/PartialFunction ~arg-binding ~@body)))

(defn handle*
  [^Future f ^scala.PartialFunction pfn]
  (.handle f pfn))

(defmacro handle
  [^Future f arg-binding & body]
  `(handle* ~f (scala/PartialFunction ~arg-binding ~@body)))
