(ns finagle-clojure.http.server-test
  (:import (com.twitter.finagle Http$Server Http$param$MaxRequestSize Http$param$MaxResponseSize)
           (com.twitter.util StorageUnit)
           (com.twitter.finagle.transport Transport$TLSServerEngine)
           (com.twitter.finagle.netty3 Netty3ListenerTLSConfig))
  (:require [finagle-clojure.http.server :refer :all]
            [finagle-clojure.http.stack-helpers :refer :all]
            [finagle-clojure.options :as opt]
            [midje.sweet :refer :all]
            [finagle-clojure.scala :as scala]))

(defn- tls-server-engine [^Http$Server server]
  (some-> server (extract-param Transport$TLSServerEngine) (.e)))

(defn- max-request-size [^Http$Server server]
  (some-> server (extract-param Http$param$MaxRequestSize) (.size)))

(defn- max-response-size [^Http$Server server]
  (some-> server (extract-param Http$param$MaxResponseSize) (.size)))

(facts "HTTP server"
  (facts "during configuration"
    (tls-server-engine
      (http-server)) => nil

    (-> (http-server)
        (with-tls (Netty3ListenerTLSConfig. (scala/Function0 nil)))
        (tls-server-engine)
        (opt/get)
        (.apply))
    => nil

    (max-request-size (http-server))
    => nil

    (max-request-size
      (with-max-request-size (http-server) (StorageUnit. 1024)))
    => (StorageUnit. 1024)

    (max-response-size (http-server))
    => nil

    (max-response-size
      (with-max-response-size (http-server) (StorageUnit. 1024)))
    => (StorageUnit. 1024)))