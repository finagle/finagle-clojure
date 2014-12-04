(ns finagle-clojure.http.message
  (:import (com.twitter.finagle.http Response Request Message)
           (org.jboss.netty.handler.codec.http HttpResponseStatus HttpMethod)))

(defn ^Response response [^HttpResponseStatus status]
  (Response/apply status))

(defn ^Request request [^HttpMethod method uri]
  (Request/apply method uri))

(defn set-content-string [^Message msg content]
  (.setContentString msg content)
  msg)

(defn content-string [^Message msg]
  (.getContentString msg))

(defn set-content-type [^Message msg type]
  (.setContentType msg type "utf-8")
  msg)