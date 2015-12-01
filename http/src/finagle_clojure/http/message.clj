(ns finagle-clojure.http.message
  "Functions for working with [[com.twitter.finagle.http.Message]] and its concrete subclasses,
  [[com.twitter.finagle.http.Request]] and [[com.twitter.finagle.http.Response]].

  `Request` objects are passed to services bound to a Finagle HTTP server, and `Response` objects must be passed back
  (wrapped in a `Future`) in turn. Most requests are constructed by Finagle, but the functions here to may be helpful
  to create `MockRequest`s for service testing purposes."
  (:import (com.twitter.finagle.http Response Request Message ParamMap)
           (org.jboss.netty.handler.codec.http HttpResponseStatus HttpMethod HttpHeaders DefaultHttpHeaders$HeaderEntry)
           (java.io InputStream)
           (java.util Map$Entry))
  (:require [finagle-clojure.options :as opt]))

(defn- ^HttpResponseStatus int->HttpResponseStatus [c]
  (HttpResponseStatus/valueOf (int c)))

(defn- ^HttpMethod str->HttpMethod [m]
  (HttpMethod/valueOf (-> m (name) (.toUpperCase))))

(defn ^Response response
  "Constructs a `Response`, required for Finagle services that interact with an `HttpServer`.

  *Arguments*:

    * `code` (optional): a number representing the desired HTTP status code of the response

  *Returns*:

    an instance of [[com.twitter.finagle.http.Response]]"
  ([]
    (Response/apply))
  ([code]
    (Response/apply (int->HttpResponseStatus code))))

(defn ^Request request
  "Constructs a `Request`. Usually this will be constructed on your behalf for incoming requests;
  this function is useful primarily testing purposes, and indeed returns a `MockRequest` in its current form.

  *Arguments*:

    * `uri`: the URI of the request
    * `method` (optional): a keyword or string of the desired HTTP method

  *Returns*:

    an instance of [[com.twitter.finagle.http.Request]], specifically a MockRequest"
  ([^String uri]
    (Request/apply uri))
  ([^String uri method]
    (Request/apply (str->HttpMethod method) uri)))

(defn ^Response set-status-code
  "Sets the status code of the given response.

  *Arguments*:

    * `resp`: a [[com.twitter.finagle.http.Response]]
    * `code`: a number with the desired HTTP status code

  *Returns*:

    the given response"
  [^Response resp code]
  (.setStatusCode resp code)
  resp)

(defn status-code
  "Returns the status code of the given `Response`.

  *Arguments*:

    * `resp`: a [[com.twitter.finagle.http.Response]]

  *Returns*:

    the status code of the response as an int"
  [^Response resp]
  (.statusCode resp))

(defn ^Message set-content-string
  "Sets the content string of the given message.

  *Arguments*:

    * `msg`: a [[com.twitter.finagle.http.Message]]
    * `content`: a string of content

  *Returns*:

    the given message"
  [^Message msg content]
  (.setContentString msg content)
  msg)

(defn ^String content-string
  "Gets the content string of the given message.

  *Arguments*:

    * `msg`: a [[com.twitter.finagle.http.Message]]

  *Returns*:

    the content string of the message"
  [^Message msg]
  (.contentString msg))

(defn ^Message set-content-type
  "Sets the content type of the given message.

  *Arguments*:

    * `msg`: a [[com.twitter.finagle.http.Message]]
    * `type`: a string containing the message's content-type
    * `charset` (optional, default: `utf-8`): the charset of the content

  *Returns*:

    the given message"
  ([^Message msg type]
    (set-content-type msg type "utf-8"))
  ([^Message msg type charset]
    (.setContentType msg type charset)
    msg))

(defn ^String content-type
  "Gets the content type of the given message.

  *Arguments*:

    * `msg`: a [[com.twitter.finagle.http.Message]]

  *Returns*:

    the content type of the message"
  [^Message msg]
  (opt/get (.contentType msg)))

(defn ^Message set-charset
  "Sets the charset of the given message.

  *Arguments*:

    * `msg`: a [[com.twitter.finagle.http.Message]]
    * `charset`: a string charset

  *Returns*:

    the given message"
  [^Message msg charset]
  (.charset_$eq msg charset)
  msg)

(defn ^String charset
  "Gets the charset of the given message.

  *Arguments*:

    * `msg`: a [[com.twitter.finagle.http.Message]]

  *Returns*:

    the charset of the message"
  [^Message msg]
  (opt/get (.charset msg)))

(defn ^Request set-http-method
  "Sets the HTTP method of the given request.

  *Arguments*:

    * `req`: a [[com.twitter.finagle.http.Request]]
    * `meth`: a string or keyword containing a valid HTTP method

  *Returns*:

    the given request"
  [^Request req meth]
  (.setMethod req (str->HttpMethod meth))
  req)

(defn ^String http-method
  "Gets the HTTP method of the given request.

  *Arguments*:

    * `req`: a [[com.twitter.finagle.http.Request]]

  *Returns*:

    the HTTP method of the request as an uppercase string"
  [^Request req]
  (-> req (.method) (.getName)))

(defn ^Message set-header
  "Sets the named header in the given message.

  *Arguments*:

    * `msg`: a [[com.twitter.finagle.http.Message]]
    * `name`: a string containing the header name
    * `value`: a stringable object containing the value

    *Returns*:

      the given message"
  [^Message msg name value]
  (.set (.headers msg) name value)
  msg)

(defn ^String header
  "Gets the named header from the given message.

  *Arguments*:

    * `msg`: a [[com.twitter.finagle.http.Message]]
    * `header`: a string containing the header name

  *Returns*:

    the string contents of the named header in the given message"
  [^Message msg header]
  (.get (.headers msg) header))

(defn headers
  "Returns this message's headers as a Clojure map.

  *Arguments*:

    * `msg`: a [[com.twitter.finagle.http.Message]]

  *Returns*:

    this request's headers as a Clojure map"
  [^Message msg]
  (reduce (fn [h ^DefaultHttpHeaders$HeaderEntry e] (assoc h (.getKey e) (.getValue e))) {} (.headers msg)))

(defn ^InputStream input-stream
  "Returns this message's content as an input stream.

  *Arguments*:

    * `msg`: a [[com.twitter.finagle.http.Message]]

  *Returns*:

    this request's content as an input stream"
  [^Message msg]
  (.getInputStream msg))

(defn params
  "Returns this request's params as a Clojure map.

  *Arguments*:

    * `req`: a [[com.twitter.finagle.http.Request]]

  *Returns*:

    this request's params as a Clojure map"
  [^Request req]
  (reduce (fn [h ^Map$Entry e] (assoc h (.getKey e) (.getValue e))) {} (.getParams req)))

(defn ^String param
  "Returns the named param from the given request.

  *Arguments*:

    * `req`: a [[com.twitter.finagle.http.Request]]

  *Returns*:

    the string contents of the named param"
  [^Request req param]
  (.getParam req param))
