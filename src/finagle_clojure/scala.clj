(ns finagle-clojure.scala
  "Utilities for interop with JVM classes generated from Scala code.
  Scala functions & methods expect Scala collection & function instances,
  not Java Collections or Clojure IFns."
  (:import [scala.collection JavaConversions]))

;; TODO: @samn: 06/11/14 add more wrappers for JavaConversions

(defn ^scala.collection.mutable.Buffer seq->scala-buffer
  "Convert a Clojure seq to a Scala Buffer.

  *Arguments*:

    * `seq`: a Clojure seq

  *Returns*:

    A Scala Buffer with the contents of `seq`."
  [seq]
  (-> seq JavaConversions/asScalaBuffer .toList))

(defn ^java.util.List scala-seq->List
  "Convert a Scala Seq to a java.util.List.

  *Arguments*:

    * `scala-seq`: a Scala Seq

  *Returns*:

    A java.util.List with the contents of `scala-seq`."
  [scala-seq]
  (JavaConversions/seqAsJavaList scala-seq))

(defmacro Function
  "Create a new com.twitter.util.Function.
  It can be used a scala.Function1 or scala.PartialFunction.
  args-binding should be a vector containing one element,
  the name to bind the parameter to the Function to.
  The apply method will be implemented with body."
  [[arg-name] & body]
  (let [arg-tag (-> arg-name meta :tag)]
    `(proxy [com.twitter.util.Function] []
       (apply [~arg-name]
         ~@body)
       (isDefinedAt [~'v]
         (if ~arg-tag
           (instance? ~arg-tag ~'v)
           (proxy-super isDefinedAt ~'v))))))

(defmacro Function0
  "Create a new scala.Function0.
  The apply method will be implemented with body."
  [& body]
  `(proxy [scala.runtime.AbstractFunction0] []
     (apply []
       ~@body)))
