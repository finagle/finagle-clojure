(ns finagle-clojure.http.client
  (:import (com.twitter.finagle.exp HttpClient HttpClient$)
           (com.twitter.finagle Stack$Param Service)
           (com.twitter.util StorageUnit)))

(defn- ^Stack$Param param [p]
  (reify Stack$Param (default [this] p)))

(defn ^HttpClient with-tls
  "Configures the given `HttpClient` with TLS.

  *Arguments*:

    * `client`: an HttpClient
    * `cfg-or-hostname`: a `Netty3TransporterTLSConfig` config or hostname string

  *Returns*:

    the given `HttpClient`"
  [^HttpClient client cfg-or-hostname]
  (.withTls client cfg-or-hostname))

(defn ^HttpClient with-tls-without-validation
  "Configures the given `HttpClient` with TLS without validatioon..

  *Arguments*:

    * `client`: an HttpClient

  *Returns*:

    the given `HttpClient`"
  [^HttpClient client]
  (.withTlsWithoutValidation client))

(defn ^HttpClient with-max-request-size
  "Configures the given `HttpClient` with a max request size.

  *Arguments*:

    * `client`: an HttpClient
    * `size`: a `StorageUnit` of the desired request size

  *Returns*:

    the given `HttpClient`"
  [^HttpClient client ^StorageUnit size]
  (.withMaxRequestSize client size))

(defn ^HttpClient with-max-response-size
  "Configures the given `HttpClient` with a max response size.

  *Arguments*:

    * `client`: an HttpClient
    * `size`: a `StorageUnit` of the desired response size

  *Returns*:

    the given `HttpClient`"
  [^HttpClient client ^StorageUnit size]
  (.withMaxResponseSize client size))

(defn ^HttpClient configured
  "Configures the given `HttpClient` with the desired Stack.Param. Generally, prefer one of the
  explicit configuration functions over this.

  *Arguments*:

    * `client`: an HttpClient
    * `p`: a parameter that will be subsequently wrapped with `Stack.Param`

  *Returns*:

    the given `HttpClient`"
  [^HttpClient client p]
  (.configured client p (param p)))

(def ^HttpClient http-client
  "The base HTTP client. Call `service` on this once configured to convert it to a full-fledged service."
  HttpClient$/MODULE$)

(defn ^Service service
  "Creates a new HTTP client structured as a Finagle `Service`.

  *Arguments*:

    * `dest`: a comma-separated string of one or more destinations with the form `\"hostname:port\"`
    * `client` (optional): a preconfigured `HttpClient`

  *Returns*:

    a Finagle `Service`"
  ([dest]
    (service http-client dest))
  ([^HttpClient client dest]
    (.newService client dest)))
