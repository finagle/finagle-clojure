(ns finagle-clojure.http.server
  (:import (com.twitter.finagle Http Http$Server)
           (com.twitter.finagle Stack$Param ListeningServer)
           (com.twitter.util StorageUnit Future)
           (javax.net.ssl SSLContext)))

(defn- ^Stack$Param param [p]
  (reify Stack$Param (default [this] p)))

(defn ^Http$Server with-tls
  "Configures the given `Http.Server` with TLS.

  *Arguments*:

    * `server`: an Http.Server
    * `cfg`: a `Netty3ListenerTLSConfig` config

  *Returns*:

    the given `Http.Server`"
  [^Http$Server server ^SSLContext cfg]
  (-> server
      .withTransport
      (.tls cfg)))

(defn ^Http$Server with-max-request-size
  "Configures the given `Http.Server` with a max request size.

  *Arguments*:

    * `server`: an Http.Server
    * `size`: a `StorageUnit` of the desired request size

  *Returns*:

    the given `Http.Server`"
  [^Http$Server server ^StorageUnit size]
  (.withMaxRequestSize server size))

(defn ^Http$Server with-max-response-size
  "Configures the given `Http.Server` with a max response size.

  *Arguments*:

    * `server`: an Http.Server
    * `size`: a `StorageUnit` of the desired response size

  *Returns*:

    the given `Http.Server`"
  [^Http$Server server ^StorageUnit size]
  (.withMaxResponseSize server size))

(defn ^Http$Server configured
  "Configures the given `Http.Server` with the desired Stack.Param. Generally, prefer one of the
  explicit configuration functions over this.

  *Arguments*:

    * `server`: an Http.Server
    * `p`: a parameter that will be subsequently wrapped with `Stack.Param`

  *Returns*:

    the given `Http.Server`"
  [^Http$Server server p]
  (.configured server p (param p)))

(defn ^Http$Server http-server
  "The base HTTP server. Call `serve` on this once configured to begin listening to requests.

  *Arguments*:

    * None.

  *Returns*:

    an instance of `Http.Server`"
  []
  (Http/server))

(defn ^ListeningServer serve
  "Creates a new HTTP server listening on the given address and responding with the given service or
  service factory. The service must accept requests of type `HttpRequest`, and respond with a Future
  wrapping an `HttpResponse`.

  *Arguments*:

    * `address`: a listening address, either a string of the form `\":port\"` or a `SocketAddress`
    * `service`: a responding service, either a `Service` or a `ServiceFactory`
    * `server` (optional): a preconfigured `Http.Server`

  *Returns*:

    a running `ListeningServer`"
  ([address service]
    (serve (http-server) address service))
  ([^Http$Server server address service]
    (.serve server address service)))

(defn ^Future close!
  "Stops the given Server.

  *Arguments*:

    * `server`: an Http.Server

  *Returns*:

    a Future that closes when the server stops"
  [^Http$Server server]
  (.close server))
