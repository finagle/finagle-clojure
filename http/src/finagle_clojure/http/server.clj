(ns finagle-clojure.http.server
  (:import (com.twitter.finagle.exp HttpServer$ HttpServer)
           (com.twitter.finagle.server StackServerLike)
           (com.twitter.finagle Stack$Param ListeningServer)
           (com.twitter.finagle.netty3 Netty3ListenerTLSConfig)
           (com.twitter.util StorageUnit))
  (:require [finagle-clojure.server :refer :all]))

(defn ^HttpServer with-tls [^HttpServer server ^Netty3ListenerTLSConfig cfg]
  (.withTls server cfg))

(defn ^HttpServer with-max-request-size [^HttpServer server ^StorageUnit size]
  (.withMaxRequestSize server size))

(defn ^HttpServer with-max-response-size [^HttpServer server ^StorageUnit size]
  (.withMaxResponseSize server size))

(defn- ^Stack$Param param [p]
  (reify Stack$Param (default [this] p)))

(defn ^HttpServer configured [^HttpServer server p]
  (.configured server p (param p)))

(defn ^ListeningServer [^HttpServer$ server address service]
  (.serve server address service))

(defn ^HttpServer http-server []
  HttpServer$/MODULE$)