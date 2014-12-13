(ns finagle-clojure.http.server
  (:import (com.twitter.finagle.exp HttpServer$ HttpServer)
           (com.twitter.finagle Stack$Param ListeningServer)
           (com.twitter.finagle.netty3 Netty3ListenerTLSConfig)
           (com.twitter.util StorageUnit))
  (:require [finagle-clojure.server :refer :all]))

(defn- ^Stack$Param param [p]
  (reify Stack$Param (default [this] p)))

(defn ^HttpServer with-tls
  "Configures the given `HttpServer` with TLS.

  *Arguments*:

    * `server`: an HttpServer
    * `cfg`: a `Netty3TransporterTLSConfig` config

  *Returns*:

    the given `HttpServer`"
  [^HttpServer server ^Netty3ListenerTLSConfig cfg]
  (.withTls server cfg))

(defn ^HttpServer with-max-request-size
  "Configures the given `HttpServer` with a max request size.

  *Arguments*:

    * `server`: an HttpServer
    * `size`: a `StorageUnit` of the desired request size

  *Returns*:

    the given `HttpServer`"
  [^HttpServer server ^StorageUnit size]
  (.withMaxRequestSize server size))

(defn ^HttpServer with-max-response-size
  "Configures the given `HttpServer` with a max response size.

  *Arguments*:

    * `server`: an HttpServer
    * `size`: a `StorageUnit` of the desired response size

  *Returns*:

    the given `HttpServer`"
  [^HttpServer server ^StorageUnit size]
  (.withMaxResponseSize server size))

(defn ^HttpServer configured
  "Configures the given `HttpServer` with the desired Stack.Param. Generally, prefer one of the
  explicit configuration functions over this.

  *Arguments*:

    * `server`: an HttpServer
    * `p`: a parameter that will be subsequently wrapped with `Stack.Param`

  *Returns*:

    the given `HttpServer`"
  [^HttpServer server p]
  (.configured server p (param p)))

(def ^HttpServer http-server
  "The base HTTP server. Call `serve` on this once configured to begin listening to requests."
  HttpServer$/MODULE$)

(defn ^ListeningServer serve
  "Creates a new HTTP server listening on the given address and responding with the given service or
  service factory. The service must accept requests of type `HttpRequest`, and respond with a Future
  wrapping an `HttpResponse`.

  *Arguments*:

    * `address`: a listening address, either a string of the form `\":port\"` or a `SocketAddress`
    * `service`: a responding service, either a `Service` or a `ServiceFactory`
    * `server` (optional): a preconfigured `HttpServer`

  *Returns*:

    a running `ListeningServer`"
  ([address service]
    (serve http-server address service))
  ([^HttpServer$ server address service]
    (.serve server address service)))
