(ns finagle-clojure.timer
  (:import [com.twitter.util JavaTimer MockTimer NullTimer]))

;; TODO add other Timers

;; TODO add docstrings

(defn java-timer
  ([] (JavaTimer.))
  ([daemon?] (JavaTimer. daemon?)))

(defn null-timer
  []
  (NullTimer.))

(defn mock-timer
  []
  (MockTimer.))

;; TODO add wrappers around Timer methods
