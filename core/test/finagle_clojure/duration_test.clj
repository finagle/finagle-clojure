(ns finagle-clojure.duration-test
  (:import [java.util.concurrent TimeUnit])
  (:require [finagle-clojure.duration :refer :all]
            [midje.sweet :refer :all]))

;; set *warn-on-reflection* after loading midje to skip its reflection warnings
(set! *warn-on-reflection* true)

(facts "->Duration"
  (class (->Duration 1 :ms)) => com.twitter.util.Duration
  (.inMilliseconds (->Duration 1 :ms)) => 1
  (.inMilliseconds (->Duration 1 TimeUnit/MILLISECONDS)) => 1
  (.inSeconds (->Duration 1 :s)) => 1
  (.inSeconds (->Duration 1 TimeUnit/SECONDS)) => 1
  (.inNanoseconds (->Duration 1 :ns)) => 1
  (.inNanoseconds (->Duration 1 TimeUnit/NANOSECONDS)) => 1
  (.inMicroseconds (->Duration 1 :us)) => 1
  (.inMicroseconds (->Duration 1 TimeUnit/MICROSECONDS)) => 1
  (->Duration 1 :invalid) => (throws IllegalArgumentException))

(fact "ns->Time"
  (.inNanoseconds (ns->Time 1)) => 1)

(fact "ms->Time"
  (.inMilliseconds (ms->Time 1)) => 1)

(fact "s->Time"
  (.inSeconds (s->Time 1)) => 1)

(facts "->Time"
  (class (->Time 1 :ms)) => com.twitter.util.Time
  (.inNanoseconds (->Time 1 :ns)) => 1
  (.inNanoseconds (->Time 1 TimeUnit/NANOSECONDS)) => 1
  (.inMilliseconds (->Time 1 :ms)) => 1
  (.inMilliseconds (->Time 1 TimeUnit/MILLISECONDS)) => 1
  (.inSeconds (->Time 1 :s)) => 1
  (.inSeconds (->Time 1 TimeUnit/SECONDS)) => 1
  (->Time 1 :invalid) => (throws IllegalArgumentException))
