(ns finagle-clojure.util.test-helpers
  (:require [midje.sweet :refer :all]
            [midje.checking.core :as checking]
            [finagle-clojure.futures :as fut])
  (:import (com.twitter.util ConstFuture Promise)))

(defchecker future= [expected]
            (checker [actual]
                     (if-not (or (instance? Promise actual) (instance? ConstFuture actual))
                       (checking/as-data-laden-falsehood {:notes [(str "Expected a Future/Promise object but got a " (class actual) ": " actual)]})
                       (let [actual-value (fut/await actual)]
                         (if (= expected actual-value)
                           true
                           (checking/as-data-laden-falsehood {:notes [(str "Expected value " expected)
                                                                      (str "Actual value " actual-value)]}))))))
