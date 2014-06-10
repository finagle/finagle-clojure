;TODO
;(set! *warn-on-reflection* true)
(ns finagle-clojure.futures-test
  (:refer-clojure :exclude [await for map])
  (:require [finagle-clojure.futures :refer :all]
            [finagle-clojure.scala :as scala]
            [midje.sweet :refer :all]))

(fact "flatmap"
  (await (flatmap (value "hi") [s] (value (.toUpperCase ^String s)))) => "HI")

(fact "nested flatmap"
  (await (flatmap (value "hi") [s] (flatmap (value "bob") [t] (value (str s " " t)))))  => "hi bob")

(fact "map"
  (await (map (value "hi") [s] (.toUpperCase ^String s))) => "HI")

(fact "for"
  (await (for [a (value 1)
             b (value 2)]
    (value (+ a b)))) => 3)

(fact "for chain"
  (await (for [a (value 1)
             b (value (inc a))]
    (value (+ a b)))) => 3)

(fact "exception"
  (await (exception (Exception.))) => (throws Exception))

(facts "rescue"
  (await (rescue (exception (Exception.)) [Object _] (value 1))) => 1
  (await (rescue (exception (Exception.)) [String _] (value 1))) => (throws Exception))

(facts "handle"
  (await (handle (exception (Exception.)) [Object _] 1)) => 1
  (await (handle (exception (Exception.)) [String _] 1)) => (throws Exception))

(fact "collect"
  (await (collect [(value 1) (value 2)])) => [1 2])
