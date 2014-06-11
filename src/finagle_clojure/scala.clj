(ns finagle-clojure.scala
  (:import [scala.collection JavaConversions]))

(defn ^scala.collection.mutable.Buffer seq->scala-list
  [seq]
  (-> seq JavaConversions/asScalaBuffer .toList))

(defn ^java.util.List scala-seq->List
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
  The apply method will be implemented with body"
  [& body]
  `(proxy [scala.runtime.AbstractFunction0] []
     (apply []
       ~@body)))
