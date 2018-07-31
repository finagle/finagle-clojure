(ns finagle-clojure.http.client-test
  (:import (com.twitter.finagle Http$Client)
           (com.twitter.util StorageUnit)
           (com.twitter.finagle.http.param MaxRequestSize MaxResponseSize)
           (com.twitter.finagle.transport Transport$ClientSsl)
           (com.twitter.finagle.ssl TrustCredentials$ TrustCredentials$Insecure$ TrustCredentials$Unspecified$))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.http.stack-helpers :refer :all]
            [finagle-clojure.http.client :refer :all]
            [finagle-clojure.options :as opt]))

(defn- tls-hostname [^Http$Client client]
  (when-let [p (extract-param client Transport$ClientSsl)]
    (-> p .sslClientConfiguration opt/get .hostname)))

(defn- tls-client-engine [^Http$Client client]
  (when-let [p (extract-param client Transport$ClientSsl)]
    (-> p .sslClientConfiguration opt/get .trustCredentials)))

(defn- max-request-size [^Http$Client client]
  (when-let [p (extract-param client MaxRequestSize)]
    (.size p)))

(defn- max-response-size [^Http$Client client]
  (when-let [p (extract-param client MaxResponseSize)]
    (.size p)))

(facts "HTTP server"
  (facts "during configuration"
    (tls-hostname (http-client))
    => nil

    (tls-client-engine (http-client))
    => nil

    (-> (http-client)
        (with-tls "example.com")
        (tls-hostname)
        (opt/get))
    => "example.com"

    (-> (http-client)
        (with-tls "example.com")
        (tls-client-engine))
    => (TrustCredentials$Unspecified$/MODULE$)

    (-> (http-client)
        (with-tls-without-validation)
        (tls-hostname)
        (opt/get))
    => nil

    (-> (http-client)
        (with-tls-without-validation)
        (tls-client-engine))
    => (TrustCredentials$Insecure$/MODULE$)

    (max-request-size (http-client))
    => nil

    (max-request-size
      (with-max-request-size (http-client) (StorageUnit. 1024)))
    => (StorageUnit. 1024)

    (max-response-size (http-client))
    => nil

    (max-response-size
      (with-max-response-size (http-client) (StorageUnit. 1024)))
    => (StorageUnit. 1024)))
