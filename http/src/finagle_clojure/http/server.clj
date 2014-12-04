(ns finagle-clojure.http.server
  (:import (com.twitter.finagle.builder ServerBuilder)
           (com.twitter.finagle Service HttpServer)
           (com.twitter.finagle.http Http)
           (java.net InetSocketAddress)
           (com.twitter.util Duration Future)
           (com.twitter.finagle.tracing Tracer)
           (com.twitter.finagle.stats StatsReceiver)))

(defn ^ServerBuilder server-builder
  "A handy Builder for constructing Servers (i.e., binding Services to a port).
  The `ServerBuilder` requires the definition of `codec`, `bind-to` and `named`.

  The main class to use is [[com.twitter.finagle.builder.ServerBuilder]], as so:

  ```
    (-> (server-builder)
        (codec (Http/get))
        (named \"servicename\")
        (bind-to (InetSocketAddress. server-port))
        (build plus-one-service))
  ```

  *Arguments*:

    * None.

  *Returns*:

    a new instance of [[com.twitter.finagle.builder.ServerBuilder]]."
  []
  (ServerBuilder/apply))

(defn ^HttpServer build
  "Construct the Server, given the provided Service, and starts responding to requests.

  *Arguments*:

    * `b`: a ServerBuilder
    * `svc`: the Service this server will use to respond to requests

  *Returns*:

    a running instance of [[com.twitter.finagle.http.HttpServer]]"
  [^ServerBuilder b ^Service svc]
  (.unsafeBuild b svc))


;; TODO Does this belong more generically in a Closable namespace?
(defn ^Future close!
  "Stops the given Server.

  *Arguments*:

    * `server`: an instance of [[com.twitter.finagle.http.HttpServer]]

  *Returns*:

    a Future that closes when the server stops"
  [^HttpServer server]
  (.close server))

(defn ^ServerBuilder named
  "Configures the given ServerBuilder with a name.

  *Arguments*:

    * `b`: a ServerBuilder
    * `name`: the name of this server

  *Returns*:

    a named ServerBuilder"
  [^ServerBuilder b ^String name]
  (.name b name))

(defn ^ServerBuilder bind-to
  "Configures the given ServerBuilder with a port.

  *Arguments*:

    * `b`: a ServerBuilder
    * `p`: the port number to bind this server to

  *Returns*:

    a bound ServerBuilder"
  [^ServerBuilder b p]
  (.bindTo b (InetSocketAddress. (int p))))

(defn ^ServerBuilder request-timeout
  "Configures the given ServerBuilder with a request timeout.

  *Arguments*:

    * `b`: a ServerBuilder
    * `d`: the duration of the request timeout for this server

  *Returns*:

    a ServerBuilder configured with the given timeout"
  [^ServerBuilder b ^Duration d]
  (.requestTimeout b d))

(defn ^ServerBuilder codec
  "Configures the given ServerBuilder with a codec.

  *Arguments*:

    * `b`: a ServerBuilder
    * `cdc`: a Codec, CodecFactory, or Function1 that defines the server codec

  *Returns*:

    a ServerBuilder configured with the given codec"
  [^ServerBuilder b cdc]
  (.codec b cdc))

(defn max-concurrent-requests
  "Configures the given ServerBuilder to accept a maximum number of concurrent requests."
  [^ServerBuilder b mcr]
  (.maxConcurrentRequests b (int mcr)))

(defn tracer
  "Configures the given ServerBuilder to use a Tracer.

  *Arguments*:

    * `b`: a ServerBuilder
    * `tracer`: a Tracer

  *Returns*:

    a ServerBuilder configured with the given tracer"
  [^ServerBuilder b ^Tracer tracer]
  (.tracer b tracer))

(defn report-to
  "Configures the given ServerBuilder to report to a stats receiver.

  *Arguments*:

    * `b`: a ServerBuilder
    * `rcvr`: a StatsReceiver

  *Returns*:

    a ServerBuilder configured with the given stats receiver"
  [^ServerBuilder b ^StatsReceiver rcvr]
  (.reportTo b rcvr))

(defn ^HttpServer http-server
  "Creates a new HttpServer with the given name, port, service and codec. Convenient but not as full-featured as a
  custom builder.

  *Arguments*:

    * `name`: the name of this server
    * `p`: the port to bind this server to
    * `svc`: the Service to use to respond to requests
    * `cdc`: the server codec (default: [[Http]])

  *Returns*:

    a running HttpServer"
  ([^String name p ^Service svc]
    (http-server name p svc (Http/get)))
  ([^String name p ^Service svc cdc]
   (-> (server-builder)
       (named name)
       (bind-to p)
       (codec cdc)
       (build svc))))
