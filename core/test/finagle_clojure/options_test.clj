(ns finagle-clojure.options-test
  (:refer-clojure :exclude [get empty?])
  (:import (scala Some None$))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.options :refer :all]))

(set! *warn-on-reflection* true)

(facts "option creation"
  (class (option))      => None$
  (class (option nil))  => None$
  (class (option :foo)) => Some

  (empty? (option))      => true
  (empty? (option nil))  => true
  (empty? (option :foo)) => false

  (get (option))      => nil
  (get (option nil))  => nil
  (get (option :foo)) => :foo
  )
