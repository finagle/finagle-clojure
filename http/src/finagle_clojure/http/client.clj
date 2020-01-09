(ns finagle-clojure.http.client
  (:import (com.twitter.finagle Http Http$Client)
           (com.twitter.finagle Stack$Param Service)
           (com.twitter.util StorageUnit Future)))

(defn- ^Stack$Param param [p]
  (reify Stack$Param (default [this] p)))

(defn ^Http$Client with-tls
  "Configures the given `Http.Client` with TLS.

  *Arguments*:

    * `client`: an Http.Client
    * `cfg-or-hostname`: a `Netty3TransporterTLSConfig` config or hostname string

  *Returns*:

    the given `Http.Client`"
  [^Http$Client client cfg-or-hostname]
  (.withTls client cfg-or-hostname))

(defn ^Http$Client with-tls-without-validation
  "Configures the given `Http.Client` with TLS without validation.

  *Arguments*:

    * `client`: an Http.Client

  *Returns*:

    the given `Http.Client`"
  [^Http$Client client]
  (.withTlsWithoutValidation client))

(defn ^Http$Client with-max-response-size
  "Configures the given `Http.Client` with a max response size.

  *Arguments*:

    * `client`: an Http.Client
    * `size`: a `StorageUnit` of the desired response size

  *Returns*:

    the given `Http.Client`"
  [^Http$Client client ^StorageUnit size]
  (.withMaxResponseSize client size))

(defn ^Http$Client configured
  "Configures the given `Http.Client` with the desired Stack.Param. Generally, prefer one of the
  explicit configuration functions over this.

  *Arguments*:

    * `client`: an Http.Client
    * `p`: a parameter that will be subsequently wrapped with `Stack.Param`

  *Returns*:

    the given `Http.Client`"
  [^Http$Client client p]
  (.configured client p (param p)))

(defn ^Http$Client http-client
  "The base HTTP client. Call `service` on this once configured to convert it to a full-fledged service.

  *Arguments*:

    * None.

  *Returns*:

    an instance of `Http.Client`"
  []
  (Http/client))

(defn ^Service service
  "Creates a new HTTP client structured as a Finagle `Service`.

  *Arguments*:

    * `dest`: a comma-separated string of one or more destinations with the form `\"hostname:port\"`
    * `client` (optional): a preconfigured `Http.Client`

  *Returns*:

    a Finagle `Service`"
  ([dest]
    (service (http-client) dest))
  ([^Http$Client client dest]
    (.newService client dest)))

(defn ^Future close!
  "Stops the given client.

  *Arguments*:

    * `client`: an instance of [[com.twitter.finagle.Client]]

  *Returns*:

    a Future that closes when the client stops"
  [^Http$Client client]
  (.close client))
