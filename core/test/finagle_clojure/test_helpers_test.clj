(ns finagle-clojure.test-helpers-test
  (:require [midje.sweet :refer :all]
            [finagle-clojure.util.test-helpers :refer [future=]]
            [finagle-clojure.futures :as fut]))

(fact "future="
      (fut/value true) => (future= true)
      (fut/value "string") => (future= "string"))
