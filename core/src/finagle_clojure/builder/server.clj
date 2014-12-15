(ns finagle-clojure.builder.server
  "Functions for creating and altering `com.twitter.finagle.Server` objects independent
  of any particular codec. Generally speaking codec-specific server functions
  should be preferred, but these are included for comptability with older systems
  configured at the server level."
  (:import (com.twitter.finagle.builder ServerBuilder Server)
           (com.twitter.finagle Service)
           (java.net InetSocketAddress)
           (com.twitter.util Duration Future)
           (com.twitter.finagle.tracing Tracer)
           (com.twitter.finagle.stats StatsReceiver)))

(defn ^ServerBuilder builder
  "A handy Builder for constructing Servers (i.e., binding Services to a port).
  The `ServerBuilder` requires the definition of `codec`, `bind-to` and `named`.

  The main class to use is [[com.twitter.finagle.builder.ServerBuilder]], as so:

  ```
    (-> (builder)
        (named \"servicename\")
        (bind-to 3000)
        (build some-service))
  ```

  *Arguments*:

    * None.

  *Returns*:

    a new instance of [[com.twitter.finagle.builder.ServerBuilder]]."
  []
  (ServerBuilder/apply))

(defn ^Server build
  "Given a completed `ServerBuilder` and a `Service`, constructs an `Server` which is capable of
  responding to requests.

  *Arguments*:

    * `b`: a ServerBuilder
    * `svc`: the Service this server will use to respond to requests

  *Returns*:

    a running instance of [[com.twitter.finagle.builder.Server]]"
  [^ServerBuilder b ^Service svc]
  (.unsafeBuild b svc))

(defn ^Future close!
  "Stops the given Server.

  *Arguments*:

    * `server`: an instance of [[com.twitter.finagle.builder.Server]]

  *Returns*:

    a Future that closes when the server stops"
  [^Server server]
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

(defn ^ServerBuilder max-concurrent-requests
  "Configures the given ServerBuilder to accept a maximum number of concurrent requests.

  *Arguments*:

    * `b`: a ServerBuilder
    * `mcr`: the maximum number of concurrent requests

  *Returns*:

    a ServerBuilder configured with a maximum number of concurrent requests"
  [^ServerBuilder b mcr]
  (.maxConcurrentRequests b (int mcr)))

(defn ^ServerBuilder tracer
  "Configures the given ServerBuilder to use a Tracer.

  *Arguments*:

    * `b`: a ServerBuilder
    * `tracer`: a Tracer

  *Returns*:

    a ServerBuilder configured with the given tracer"
  [^ServerBuilder b ^Tracer tracer]
  (.tracer b tracer))

(defn ^ServerBuilder report-to
  "Configures the given ServerBuilder to report to a stats receiver.

  *Arguments*:

    * `b`: a ServerBuilder
    * `rcvr`: a StatsReceiver

  *Returns*:

    a ServerBuilder configured with the given stats receiver"
  [^ServerBuilder b ^StatsReceiver rcvr]
  (.reportTo b rcvr))
