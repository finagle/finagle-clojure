;TODO
;(set! *warn-on-reflection* true)
(ns finagle-clojure.futures-test
  (:refer-clojure :exclude [for get map])
  (:require [finagle-clojure.futures :refer :all]
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
