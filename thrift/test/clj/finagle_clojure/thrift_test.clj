(ns finagle-clojure.thrift-test
  (:require [finagle-clojure.thrift :as thrift]
            [finagle-clojure.futures :as f]
            [midje.sweet :refer :all]))

(fact "finagle-interface"
  (thrift/finagle-interface 'DogBreedService) => 'DogBreedService$ServiceIface)

;;; This is a high level integration test of finagle-clojure/thrift
;;; See the Thrift service definition in test/resources/service.thrift
;;; It has been compiled into finagled Java classes at test/java/
;;; To regenerate the compiled Java classes run scrooge against the thrift definition.
;;; java -jar scrooge-generator-3.9.0-jar-with-dependencies.jar --finagle --language java --dest test/java/ test/resources/service.thrift
;;; See http://github.com/samn/scrooge-clojure-demo for more info

(def dog-breed-service 
  (thrift/service test.DogBreedService
    (breedInfo [breed-name]
      (if (= breed-name "pit bull")
        (f/value (test.BreedInfoResponse. breed-name true))
        (f/value (test.BreedInfoResponse. breed-name false))))))

(def dog-breed-server (thrift/serve ":9999" dog-breed-service))

(def dog-breed-client (thrift/client "localhost:9999" test.DogBreedService))

(defn beautiful-dog?
  "Is `dog-breed` beautiful?"
  [dog-breed]
  (-> dog-breed-client
      (.breedInfo dog-breed)
      (f/map [breed-info] (.beautiful breed-info))))

(fact "this all works"
  (f/await (beautiful-dog? "pit bull")) =>  true
  (f/await (beautiful-dog? "pomeranian")) =>  false)