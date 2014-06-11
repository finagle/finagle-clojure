(ns finagle-clojure.futures
  (:refer-clojure :exclude [await ensure for future map])
  (:require [finagle-clojure.scala :as scala])
  (:import [com.twitter.util Await Future]))

(defn ^Future flatmap*
  [^Future f ^scala.Function1 fn1]
  (.flatMap f fn1))

;; TODO: @samn 06/11/14 will this require reflection? type tag needed?
(defmacro flatmap
  "Call flatMap on twitter-future.
  param-binding is a vector with 1 element, the name to bind the value of the flatMap to."
  [^Future f param-binding & body]
  `(flatmap* ~f (scala/Function ~param-binding ~@body)))

(defn map*
  [^Future f ^scala.Function1 fn1]
  (.map f fn1))

(defmacro map
  "Call map on twitter-future.
  param-binding is a vector with 1 element, the name to bind the value of the flatMap to."
  [^Future f  param-binding & body]
  `(map* ~f (scala/Function ~param-binding ~@body)))

(defn await
  "Get the value for Future f.
  Blocks until the Future is ready."
  [^Future f]
  (Await/result f))

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

(defn ^Future collect
  "Takes a seq of Futures, returns a Future of a seq of their values."
  [future-seq]
  (flatmap (Future/collect (scala/seq->scala-list future-seq))
    [scala-seq]
    (value (scala/scala-seq->List scala-seq))))

(defn ^Future rescue*
  [^Future f ^scala.PartialFunction pfn]
  (.rescue f pfn))

(defmacro rescue
  [^Future f arg-binding & body]
  `(rescue* ~f (scala/Function ^Throwable ~arg-binding ~@body)))

(defn ^Future handle*
  [^Future f ^scala.PartialFunction pfn]
  (.handle f pfn))

(defmacro handle
  [^Future f arg-binding & body]
  `(handle* ~f (scala/Function ^Throwable ~arg-binding ~@body)))

(defn ^Future ensure*
  [^Future f ^scala.Function0 fn0]
  (.ensure f fn0))

(defmacro ensure
  [^Future f & body]
  `(ensure* ~f (scala/Function0 ~@body)))
