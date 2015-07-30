(ns finagle-clojure.scala
  "Utilities for interop with JVM classes generated from Scala code.
  Scala functions & methods expect Scala collection & function instances,
  not Java Collections or Clojure IFns."
  (:import [scala.collection JavaConversions]
           [scala Product]
           [scala.runtime BoxedUnit]))

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

(defn tuple->vec [^Product p]
  "Convert a Scala Tuple to a vector.

  *Arguments*:

    * `p`: a Scala Product, generally a tuple

  *Returns*:

    A PersistentVector with the conents of `p`."
  (->> (.productArity p)
       (range)
       (map #(.productElement p %))
       (into [])))

(def unit
  "The Scala Unit value."
  BoxedUnit/UNIT)

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
  `args-binding` should be a vector containing one element `[arg-name]`
  the name to bind the parameter to the Function to.
  The apply method will be implemented with body."
  [[arg-name] & body]
  (let [arg-tag (-> arg-name meta :tag)]
    `(Function* (fn [~arg-name] ~@body) ~arg-tag)))

(defn ^com.twitter.util.Function0 Function0*
  "Create a new scala.Function0.
  The apply method will be implemented with f."
  [f]
  (proxy [com.twitter.util.Function0] []
    (apply [] (f))))

(defmacro Function0
  "Create a new scala.Function0.
  The apply method will be implemented with body."
  [& body]
  `(Function0* (fn [] ~@body)))

(defprotocol LiftToFunction1
  (lift->fn1 [this]))

(extend-protocol LiftToFunction1
  scala.Function1
  (lift->fn1 [this] this)
  clojure.lang.IFn
  (lift->fn1 [this] (Function* this)))

(defprotocol LiftToFunction0
  (lift->fn0 [this]))

(extend-protocol LiftToFunction0
  scala.Function0
  (lift->fn0 [this] this)
  clojure.lang.IFn
  (lift->fn0 [this] (Function0* this)))
