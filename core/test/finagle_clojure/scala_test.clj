(ns finagle-clojure.scala-test
  (:require [finagle-clojure.scala :refer :all]
            [criterium.core :refer :all]
            [midje.sweet :refer :all]))

;; set *warn-on-reflection* after loading midje to skip its reflection warnings
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
  (-> [1] seq->scala-buffer scala-seq->vec) => [1])

(fact "Function0#apply"
  (.apply (Function0 1)) => 1)

(fact "Function0*#apply"
  (.apply (Function0* (constantly 1))) => 1)

(fact "LiftToFunction1"
  (->> (lift->fn1 (Function* identity)) (instance? scala.Function1)) => true
  (->> (lift->fn1 identity) (instance? scala.Function1)) => true
  (lift->fn1 1) => (throws IllegalArgumentException))

(fact "LiftToFunction0"
  (->> (lift->fn0 (Function0* (constantly 1))) (instance? scala.Function0)) => true
  (->> (lift->fn0 (constantly 1)) (instance? scala.Function0)) => true
  (lift->fn1 0) => (throws IllegalArgumentException))

;;;; Performance Tests
;;;; Run like: LEIN_JVM_OPTS= lein run -m finagle-clojure.scala-test/perf
(defn perf
  []
  (estimate-overhead)
  (println "\nBenchmarking raw creation (Function)\n")
  (with-progress-reporting
    (bench
      (Function [x] x)))
  (println "\nBenchmarking lift->fn1 with Function\n")
  (with-progress-reporting
    (bench
      (lift->fn1 (Function [x] x))))
  (println "\nBenchmarking lift->fn1 with Clojure fn\n")
  (with-progress-reporting
    (bench
      (lift->fn1 (fn [x] x)))))
