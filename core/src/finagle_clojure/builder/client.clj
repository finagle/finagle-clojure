(ns finagle-clojure.builder.client
  "Functions for creating and altering `com.twitter.finagle.Client` objects independent
  of any particular codec. Generally speaking codec-specific client functions
  should be preferred, but these are included for comptability with older systems
  configured at the client level."
  (:import (com.twitter.finagle.builder ClientBuilder)
           (com.twitter.util Duration Future)
           (com.twitter.finagle.stats StatsReceiver)
           (java.util.logging Logger)
           (com.twitter.finagle Service Client)))

(defn ^ClientBuilder builder []
  "A builder for constructing `com.twitter.finagle.Client`s. Repeated changes to the builder should be
  chained, as so:

  ```
    (-> (builder)
        (named \"servicename\")
        (bind-to 3000)
        (build some-service))
  ```

  *Arguments*:

    * None.

  *Returns*:

    a new instance of [[com.twitter.finagle.builder.ClientBuilder]]."
  (ClientBuilder/get))

(defn ^Service build
  "Given a completed `ClientBuilder`, return a new `Service` that represents this client.

  *Arguments*:

    * a ClientBuilder

  *Returns*:

    a [[com.twitter.finagle.Service]] that represents a client request"
  [^ClientBuilder b]
  (.unsafeBuild b))

(defn ^ClientBuilder codec
  "Configures the given ServerBuilder with a codec.

  *Arguments*:

    * `b`: a ClientBuilder
    * `cdc`: a Codec, CodecFactory, or Function1 that defines the server codec

  *Returns*:

    a ClientBuilder configured with the given codec"
  [^ClientBuilder b cdc]
  (.codec b cdc))

(defn ^ClientBuilder hosts
  "Configures the given ClientBuilder with one or more hosts.

  *Arguments*:

    * `b`: a ClientBuilder
    * `hosts`: a `SocketAddress`, `Seq<SocketAddress>` or comma-separated string of hostnames

  *Returns*:

    a ClientBuilder configured with the given hosts"
  [^ClientBuilder b hosts]
  (.hosts b hosts))

(defn ^ClientBuilder host-connection-limit
  "Configures the given ClientBuilder with a host connection limit.

  *Arguments*:

    * `b`: a ClientBuilder
    * `limit`: the number to limit connections to

  *Returns*:

    a ClientBuilder configured with the given limit"
  [^ClientBuilder b limit]
  (.hostConnectionLimit b (int limit)))

(defn ^ClientBuilder tcp-connect-timeout
  "Configures the given ClientBuilder with a TCP connection timeout.

  *Arguments*:

    * `b`: a ClientBuilder
    * `timeout`: a [[com.twitter.util.Duration]]

  *Returns*:

    a ClientBuilder configured with the given timeout"
  [^ClientBuilder b ^Duration timeout]
  (.tcpConnectTimeout b timeout))

(defn ^ClientBuilder retries
  "Configures the given ClientBuilder with a retry limit.

  *Arguments*:

    * `b`: a ClientBuilder
    * `retries`: the number of times to retry

  *Returns*:

    a ClientBuilder configured with the given retries"
  [^ClientBuilder b retries]
  (.retries b (int retries)))

(defn ^ClientBuilder report-to
  "Configures the given ClientBuilder with a StatsReceiver to report to.

  *Arguments*:

    * `b`: a ClientBuilder
    * `rcvr`: a [[com.twitter.finagle.stats.StatsReceiver]]

  *Returns*:

    a ClientBulider configured with the given StatsReceiver"
  [^ClientBuilder b ^StatsReceiver rcvr]
  (.reportTo b rcvr))

(defn ^ClientBuilder logger
  "Configures the given ClientBuilder with a Logger.

  *Arguments*:

    * `b`: a ClientBuilder
    * `l`: a [[java.util.logging.Logger]]

  *Returns*

    a ClientBuilder configured with the given Logger"
  [^ClientBuilder b ^Logger l]
  (.logger b l))

(defn ^Future close!
  "Stops the given client.

  *Arguments*:

    * `client`: an instance of [[com.twitter.finagle.Client]]

  *Returns*:

    a Future that closes when the client stops"
  [^Client client]
  (.close client))
