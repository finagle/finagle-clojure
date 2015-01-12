(ns finagle-clojure.service-test
  (:refer-clojure :exclude [apply])
  (:require [finagle-clojure.service :refer :all]
            [finagle-clojure.futures :as f]
            [finagle-clojure.scala :as scala]
            [midje.sweet :refer :all]))

;; set *warn-on-reflection* after loading midje to skip its reflection warnings
(set! *warn-on-reflection* true)

(fact "mk* works with Clojure & Scala functions"
  (-> (mk* (fn [in] (f/value (inc in)))) (apply 1) f/await) => 2
  (-> (mk* (scala/Function [in] (f/value (inc in)))) (apply 1) f/await) => 2)

(fact "mk"
  (-> (mk [in] (f/value (inc in))) (apply 1) f/await) => 2)

(fact "rescue"
  (-> (mk [in] (f/exception (Exception.))) (apply 1)) =not=> (throws Exception) ; returns a Future
  (-> (mk [in] (f/exception (Exception.))) (apply 1) f/await) => (throws Exception))

