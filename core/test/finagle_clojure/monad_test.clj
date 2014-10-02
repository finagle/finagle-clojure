(ns finagle-clojure.monad-test
  (:require [clojure.algo.monads :refer [domonad]]
            [finagle-clojure.monad :refer :all]
            [finagle-clojure.futures :as f]
            [midje.sweet :refer :all]))

(fact "The monadic expansion"
      (f/await
        (dofuture [x (f/value 9)
                   y (f/value "stuff")]
                  (str x y)))
      => "9stuff")
