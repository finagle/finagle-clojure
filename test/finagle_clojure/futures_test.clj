;TODO
;(set! *warn-on-reflection* true)
(ns finagle-clojure.futures-test
  (:refer-clojure :exclude [for get map])
  (:require [finagle-clojure.futures :refer :all]
            [finagle-clojure.scala :as scala]
            [midje.sweet :refer :all]))

(fact "flatmap"
  (get (flatmap (value "hi") [s] (value (.toUpperCase ^String s)))) => "HI")

(fact "nested flatmap"
  (get (flatmap (value "hi") [s] (flatmap (value "bob") [t] (value (str s " " t)))))  => "hi bob")

(fact "map"
  (get (map (value "hi") [s] (.toUpperCase ^String s))) => "HI")

(fact "for"
  (get (for [a (value 1)
             b (value 2)]
    (value (+ a b)))) => 3)

(fact "for chain"
  (get (for [a (value 1)
             b (value (inc a))]
    (value (+ a b)))) => 3)

(fact "exception"
  (get (exception (Exception.))) => (throws Exception))

(facts "rescue"
  (get (rescue (exception (Exception.)) [Object _] (value 1))) => 1
  (get (rescue (exception (Exception.)) [String _] (value 1))) => (throws Exception))

(facts "handle"
  (get (handle (exception (Exception.)) [Object _] 1)) => 1
  (get (handle (exception (Exception.)) [String _] 1)) => (throws Exception))
