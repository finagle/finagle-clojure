(ns finagle-clojure.thrift-test
  (:import test.DogBreedService)
  (:require [finagle-clojure.thrift :as thrift]
            [finagle-clojure.futures :as f]
            [midje.sweet :refer :all]
            [clojure.java.io :as io]))

;; set *warn-on-reflection* after loading midje to skip its reflection warnings
(set! *warn-on-reflection* true)

(fact "finagle-interface"
  (thrift/finagle-interface 'some.Service) => 'some.Service$ServiceIface
  (thrift/finagle-interface 'some.Service$ServiceIface) => 'some.Service$ServiceIface
  (thrift/finagle-interface 'test.DogBreedService) => 'test.DogBreedService$ServiceIface
  (thrift/finagle-interface test.DogBreedService) => 'test.DogBreedService$ServiceIface
  (thrift/finagle-interface 'DogBreedService) => 'test.DogBreedService$ServiceIface
  (thrift/finagle-interface 'DogBreedService$ServiceIface) => 'DogBreedService$ServiceIface
  (thrift/finagle-interface DogBreedService) => 'test.DogBreedService$ServiceIface)

;;; This is a high level integration test of finagle-clojure/thrift
;;; See the Thrift service definition in test/resources/service.thrift
;;; It has been compiled into finagled Java classes at test/java/
;;; To regenerate the compiled Java classes run scrooge against the thrift definition:
;;;     lein finagle-clojure scrooge

(def dog-breed-service
  (thrift/service DogBreedService
    (breedInfo [breed-name]
      (if (not= breed-name "pomeranian")
        (f/value (test.BreedInfoResponse. breed-name true))
        (f/value (test.BreedInfoResponse. breed-name false))))))

(def ^com.twitter.finagle.ListeningServer dog-breed-server (thrift/serve ":9999" dog-breed-service))

(def ^test.DogBreedService$ServiceIface dog-breed-client (thrift/client "localhost:9999" test.DogBreedService))

(defn beautiful-dog?
  "Is `dog-breed` beautiful?"
  [^test.DogBreedService$ServiceIface client dog-breed]
  (-> (.breedInfo client dog-breed)
      (f/map [^test.BreedInfoResponse breed-info] (.beautiful breed-info))))

(fact "this all works"
  (f/await (beautiful-dog? dog-breed-client "pit bull")) =>  true
  (f/await (beautiful-dog? dog-breed-client "pomeranian")) =>  false)

;; shut down the thrift server so midje :autotest will work
(f/await (.close dog-breed-server))

;;; Runs the same set of tests, but with the TLS-enabled Server and Client

(defn resolve-on-filesystem
  "Finagle expects certificates to have an absolute path on the filesystem; this fn
  reads the test certs from the classpath and writes them to temp"
  [path]
  (let [resource-uri (io/resource path)
        file-name (str "/tmp/" (-> resource-uri .getPath (clojure.string/split #"/") last))]
    (spit file-name (slurp resource-uri))
    (io/as-file file-name)))

(def ^java.io.File private-key (resolve-on-filesystem "test-only_key.pem"))

(def ^java.io.File public-key (resolve-on-filesystem "test-only_cert.pem"))

(fact "keys exist"
  (.exists private-key) => true
  (.exists public-key) => true)

(def ^com.twitter.finagle.ListeningServer tls-dog-breed-server
  (thrift/serve-tls ":9998" dog-breed-service (.getAbsolutePath private-key) (.getAbsolutePath public-key)))

(def ^test.DogBreedService$ServiceIface tls-dog-breed-client
  (thrift/client-tls "localhost:9998" test.DogBreedService (thrift/insecure-ssl-context)))

(fact "this all works with tls too"
      (f/await (beautiful-dog? tls-dog-breed-client "pit bull")) =>  true
      (f/await (beautiful-dog? tls-dog-breed-client "pomeranian")) =>  false)

(f/await (.close tls-dog-breed-server))

(io/delete-file private-key true)

(io/delete-file public-key true)
