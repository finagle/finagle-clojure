(ns finagle-clojure.builder.client
  "Functions for creating and altering `com.twitter.finagle.Client` objects independent
  of any particular codec. Generally speaking codec-specific client functions
  should be preferred, but these are included for comptability with older systems
  configured at the client level."
  (:import (com.twitter.finagle.builder ClientBuilder)
           (com.twitter.util Duration Closable)
           (com.twitter.finagle.stats StatsReceiver)
           (java.util.logging Logger)
           (com.twitter.finagle Service)))

(defn ^ClientBuilder builder []
  (ClientBuilder/get))

(defn ^Service build [^ClientBuilder b]
  (ClientBuilder/safeBuild b))

(defn ^ClientBuilder codec [^ClientBuilder b cdc]
  (.codec b cdc))

(defn ^ClientBuilder hosts [^ClientBuilder b hosts]
  (.hosts b hosts))

(defn ^ClientBuilder host-connection-limit [^ClientBuilder b limit]
  (.hostConnectionLimit b (int limit)))

(defn ^ClientBuilder tcp-connect-timeout [^ClientBuilder b ^Duration timeout]
  (.tcpConnectTimeout b timeout))

(defn ^ClientBuilder retries [^ClientBuilder b retries]
  (.retries b (int retries)))

(defn ^ClientBuilder report-to [^ClientBuilder b ^StatsReceiver rcvr]
  (.reportTo b rcvr))

(defn ^ClientBuilder logger [^ClientBuilder b ^Logger l]
  (.logger b l))

(defn close! [^Closable c]
  (.close c))
