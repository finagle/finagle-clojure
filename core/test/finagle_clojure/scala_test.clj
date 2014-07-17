(ns finagle-clojure.scala-test
  (:require [finagle-clojure.scala :refer :all]
            [midje.sweet :refer :all]))

(set! *warn-on-reflection* true)

(facts "Function#isDefinedAt"
  (.isDefinedAt (Function [^String s] nil) (Object.)) => false
  (.isDefinedAt (Function [^String s] nil) "") => true
  (.isDefinedAt (Function [s] nil) "") => true
  (.isDefinedAt (Function [s] nil) (Object.)) => true)

(fact "Function#apply"
  (.apply (Function [a] a) 1) => 1)

(facts "seq <=> scala conversion"
  (class (seq->scala-buffer [1])) => scala.collection.immutable.$colon$colon
  (-> [1] seq->scala-buffer scala-seq->List) => [1])

(fact "Function0#apply"
  (.apply (Function0 1)) => 1)
