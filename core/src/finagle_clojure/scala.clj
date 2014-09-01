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

(defn scala-seq->vec
  "Convert a Scala Seq to a vector.

  *Arguments*:

    * `scala-seq`: a Scala Seq

  *Returns*:

    A PersistentVector with the contents of `scala-seq`."
  [scala-seq]
  (into [] (JavaConversions/seqAsJavaList scala-seq)))

(defn ^com.twitter.util.Function Function*
  ([apply-fn] (Function* apply-fn nil))
  ([apply-fn defined-at-class]
    (proxy [com.twitter.util.Function] []
      (apply [arg]
        (apply-fn arg))
      (isDefinedAt [v]
        (if defined-at-class
          (instance? defined-at-class v)
          (let [^com.twitter.util.Function this this]
            (proxy-super isDefinedAt v)))))))

(defmacro Function
  "Create a new com.twitter.util.Function.
  It can be used a scala.Function1 or scala.PartialFunction.
  args-binding should be a vector containing one element,
  the name to bind the parameter to the Function to.
  The apply method will be implemented with body."
  [[arg-name] & body]
  (let [arg-tag (-> arg-name meta :tag)]
    `(Function* (fn [~arg-name] ~@body) ~arg-tag)))

(defmacro Function0
  "Create a new scala.Function0.
  The apply method will be implemented with body."
  [& body]
  `(proxy [scala.runtime.AbstractFunction0] []
     (apply []
       ~@body)))
