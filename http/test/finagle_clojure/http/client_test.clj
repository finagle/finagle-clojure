(ns finagle-clojure.http.client-test
  (:import (com.twitter.finagle Http$Client Http$param$MaxRequestSize Http$param$MaxResponseSize)
           (com.twitter.finagle.transport Transport$TLSClientEngine)
           (com.twitter.util StorageUnit)
           (com.twitter.finagle.client Transporter$TLSHostname))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.http.stack-helpers :refer :all]
            [finagle-clojure.http.client :refer :all]
            [finagle-clojure.options :as opt]))

(defn- tls-hostname [^Http$Client client]
  (some-> client (extract-param Transporter$TLSHostname) (.hostname)))

(defn- tls-client-engine [^Http$Client client]
  (some-> client (extract-param Transport$TLSClientEngine) (.e)))

(defn- max-request-size [^Http$Client client]
  (some-> client (extract-param Http$param$MaxRequestSize) (.size)))

(defn- max-response-size [^Http$Client client]
  (some-> client (extract-param Http$param$MaxResponseSize) (.size)))

(facts "HTTP server"
  (facts "during configuration"
    (tls-hostname http-client)
    => nil

    (tls-client-engine http-client)
    => nil

    (-> http-client
        (with-tls "example.com")
        (tls-hostname)
        (opt/get))
    => "example.com"

    (-> http-client
        (with-tls "example.com")
        (tls-client-engine)
        (opt/get)
        (class)
        (ancestors))
    => (contains scala.Function1)

    (-> http-client
        (with-tls-without-validation)
        (tls-hostname))
    => nil

    (-> http-client
        (with-tls-without-validation)
        (tls-client-engine)
        (opt/get)
        (class)
        (ancestors))
    => (contains scala.Function1)

    (max-request-size http-client)
    => nil

    (max-request-size
      (with-max-request-size http-client (StorageUnit. 1024)))
    => (StorageUnit. 1024)

    (max-response-size http-client)
    => nil

    (max-response-size
      (with-max-response-size http-client (StorageUnit. 1024)))
    => (StorageUnit. 1024)))