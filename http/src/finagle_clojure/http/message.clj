(ns finagle-clojure.http.message
  (:import (com.twitter.finagle.http Response Request Message)
           (org.jboss.netty.handler.codec.http HttpResponseStatus HttpMethod)
           (scala Option)))

(defn- ^HttpResponseStatus int->HttpResponseStatus [c]
  (HttpResponseStatus/valueOf c))

(defn- ^HttpMethod str->HttpMethod [m]
  (HttpMethod/valueOf (-> m (name) (.toUpperCase))))

(defn ^Response response
  ([]
    (Response/apply))
  ([status-code]
    (Response/apply (int->HttpResponseStatus status-code))))

(defn ^Request request
  ([uri]
    (Request/apply uri))
  ([uri method]
    (Request/apply (str->HttpMethod method) uri)))

(defn ^Response set-status-code [^Response resp status-code]
  (.setStatusCode resp status-code)
  resp)

(defn status-code [^Response resp]
  (.statusCode resp))

(defn ^Message set-content-string [^Message msg content]
  (.setContentString msg content)
  msg)

(defn ^String content-string [^Message msg]
  (.contentString msg))

(defn ^Message set-content-type
  ([^Message msg type]
    (set-content-type msg type "utf-8"))
  ([^Message msg type charset]
    (.setContentType msg type charset)
    msg))

(defn ^String content-type [^Message msg]
  (let [^Option ct (.contentType msg)]
    (when-not (.isEmpty ct) (.get ct))))

(defn ^Request set-http-method [^Request req meth]
  (.setMethod req (str->HttpMethod meth))
  req)

(defn http-method [^Request req]
  (-> req (.method) (.getName)))