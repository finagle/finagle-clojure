(ns finagle-clojure.http.client
  (:import (com.twitter.finagle.exp HttpClient HttpClient$)
           (com.twitter.finagle Stack$Param Service)
           (com.twitter.util StorageUnit)))

(defn ^HttpClient with-tls [^HttpClient client cfg-or-hostname]
  (.withTls client cfg-or-hostname))

(defn ^HttpClient with-tls-without-validation [^HttpClient client]
  (.withTlsWithoutValidation client))

(defn ^HttpClient with-max-request-size [^HttpClient client ^StorageUnit size]
  (.withMaxRequestSize client size))

(defn ^HttpClient with-max-response-size [^HttpClient client ^StorageUnit size]
  (.withMaxResponseSize client size))

(defn- ^Stack$Param param [p]
  (reify Stack$Param (default [this] p)))

(defn ^HttpClient configured [^HttpClient client p]
  (.configured client p (param p)))

(defn ^HttpClient http-client []
  HttpClient$/MODULE$)

(defn ^Service service [^HttpClient client dest]
  (.newService client dest))
