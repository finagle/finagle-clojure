(ns finagle-clojure.scala
  (:import [scala.collection JavaConversions]))

(defn ^scala.collection.mutable.Buffer seq->scala-list
  [seq]
  (-> seq JavaConversions/asScalaBuffer .toList))

(defn ^java.util.List scala-seq->List
  [scala-seq]
  (JavaConversions/seqAsJavaList scala-seq))

;; TODO use Twitter Util Function instead?
;; https://twitter.github.io/util/util-core/target/doc/main/api/com/twitter/util/Function.html
(defmacro Function1
  "Create a new scala.Function1.
  args-binding should be a vector containing one element,
  the name to bind the parameter to the Function1 to.
  Only Function1#apply will be implemented."
  [[arg-name] & body]
  `(reify scala.Function1
     (apply [~'this ~arg-name]
       ~@body)))

(defn ^scala.Function1 fn->Function1
  "Wrap a clojure IFn as a Function1"
  [f]
  (Function1 [arg] (f arg)))

(defmacro PartialFunction
  "Creates a new scala.PartialFunction.
  args-binding should be a vector containing one element,
  the name to bind the parameter to the PartialFunction to."
  [[arg-class arg-name] & body]
  `(reify scala.PartialFunction
     (apply [~'this ~arg-name]
       ~@body)
     (isDefinedAt [~'this ~'v]
       (instance? ~arg-class ~'v))))
