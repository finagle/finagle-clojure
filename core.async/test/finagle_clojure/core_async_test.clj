(ns finagle-clojure.core-async-test
  (:require [finagle-clojure.core-async :refer :all]
            [finagle-clojure.futures :as f]
            [clojure.core.async :as a]
            [midje.sweet :refer :all]))

(facts "future->chan"
  (<?? (future->chan (f/exception (Exception.)))) => (throws Exception)
  (<?? (future->chan (f/value :value))) => :value
  (a/<!! (a/go (<? (future->chan (f/value :value))))) => :value
  (let [c (a/chan 1)
        e (Exception.)]
    (a/<!! (future->chan (f/exception e) c)) => e
    (a/<!! c) => nil)
  (let [c (a/chan 1)]
    (a/<!! (future->chan (f/value :value) c)) => :value
    (a/<!! c) => nil))
