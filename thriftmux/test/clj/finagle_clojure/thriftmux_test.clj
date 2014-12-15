(ns finagle-clojure.thriftmux-test
  (:import test.DogBreedService)
  (:require [finagle-clojure.thriftmux :as thriftmux]
            [finagle-clojure.futures :as f]
            [midje.sweet :refer :all]))

;; set *warn-on-reflection* after loading midje to skip its reflection warnings
(set! *warn-on-reflection* true)

;;; This is a high level integration test of finagle-clojure/thriftmux
;;; See the Thrift service definition in test/resources/service.thrift
;;; It has been compiled into finagled Java classes at test/java/
;;; To regenerate the compiled Java classes run scrooge against the Thrift definition:
;;;     lein finagle-clojure scrooge

(def dog-breed-service
  (thriftmux/service DogBreedService
    (breedInfo [breed-name]
      (if (not= breed-name "pomeranian")
        (f/value (test.BreedInfoResponse. breed-name true))
        (f/value (test.BreedInfoResponse. breed-name false))))))

(def ^com.twitter.finagle.ListeningServer dog-breed-server (thriftmux/serve ":9999" dog-breed-service))

(def ^test.DogBreedService$ServiceIface dog-breed-client (thriftmux/client "localhost:9999" test.DogBreedService))

(defn beautiful-dog?
  "Is `dog-breed` beautiful?"
  [dog-breed] 
  (-> (.breedInfo dog-breed-client dog-breed)
      (f/map [^test.BreedInfoResponse breed-info] (.beautiful breed-info))))

(fact "this all works"
  (f/await (beautiful-dog? "pit bull")) =>  true
  (f/await (beautiful-dog? "pomeranian")) =>  false)

;; shut down the thriftmux server so midje :autorun will work
(f/await (.close dog-breed-server))
