(ns finagle-clojure.http.server-test
  (:import (com.twitter.finagle.http.param MaxRequestSize MaxResponseSize)
           (com.twitter.util StorageUnit)
           (com.twitter.finagle Http$Server)
           (javax.net.ssl SSLContext)
           (com.twitter.finagle.transport Transport$ServerSsl)
           (com.twitter.finagle.ssl.server SslServerConfiguration))
  (:require [finagle-clojure.http.server :refer :all]
            [finagle-clojure.http.stack-helpers :refer :all]
            [finagle-clojure.options :as opt]
            [midje.sweet :refer :all]))

(defn- tls-server-engine [^Http$Server server]
  (when-let [p (extract-param server Transport$ServerSsl)]
    (-> p .sslServerConfiguration)))

(defn- max-request-size [^Http$Server server]
  (when-let [p (extract-param server MaxRequestSize)]
    (.size p)))

(defn- max-response-size [^Http$Server server]
  (when-let [p (extract-param server MaxResponseSize)]
    (.size p)))

(facts "HTTP server"
  (facts "during configuration"
    (tls-server-engine
      (http-server)) => nil

    (-> (http-server)
        (with-tls (SSLContext/getDefault))
        (tls-server-engine)
        (opt/get)
        (class))
    => SslServerConfiguration

    (max-request-size (http-server))
    => nil

    (max-request-size
      (with-max-request-size (http-server) (StorageUnit. 1024)))
    => (StorageUnit. 1024)

    (max-response-size (http-server))
    => nil))
