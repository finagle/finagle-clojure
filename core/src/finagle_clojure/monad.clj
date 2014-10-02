(ns finagle-clojure.monad
  (:use [clojure.algo.monads :only [defmonad domonad]])
  (:require [finagle-clojure.futures :as f]))

(defmonad future-monad
          [m-result f/value

           m-bind (fn [a-future fun]
                    (f/flatmap a-future [x#] (fun x#)))

           m-map (fn [a-future fun]
                   (f/map a-future [x#] (fun x#)))])

(defmacro dofuture [& forms]
  `(domonad future-monad
            ~@forms))
