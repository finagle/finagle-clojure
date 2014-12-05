(ns finagle-clojure.mysql.client
  (:import (com.twitter.finagle Stack$Param)
           (com.twitter.finagle.exp Mysql MysqlClient)
           (com.twitter.finagle.exp.mysql Row Value ByteValue ShortValue IntValue LongValue DoubleValue FloatValue StringValue DateValue Type RawValue BigDecimalValue NullValue EmptyValue NullValue$ EmptyValue$ Client)
           (scala Option)
           (com.twitter.finagle.tracing Tracer)
           (com.twitter.finagle.stats StatsReceiver))
  (:require [finagle-clojure.scala :as scala]))


(defn or-nil [^Option o] (if (.isEmpty o) nil (.get o)))

(defn raw-type [^RawValue v] (.typ v))
(defmulti unbox-raw-value raw-type)

(defmethod unbox-raw-value (Type/Date) [^RawValue val] (or-nil (DateValue/unapply val)))
(defmethod unbox-raw-value (Type/NewDecimal) [^RawValue val]
  (when-let [^scala.math.BigDecimal bd (or-nil (BigDecimalValue/unapply val))]
    (.underlying bd)))

(defmulti unbox-value class)

(defmethod unbox-value ByteValue   [^ByteValue val]   (-> val (.b)))
(defmethod unbox-value ShortValue  [^ShortValue val]  (-> val (.s)))
(defmethod unbox-value IntValue    [^IntValue val]    (-> val (.i)))
(defmethod unbox-value LongValue   [^LongValue val]   (-> val (.l)))
(defmethod unbox-value FloatValue  [^FloatValue val]  (-> val (.f)))
(defmethod unbox-value DoubleValue [^DoubleValue val] (-> val (.d)))
(defmethod unbox-value StringValue [^StringValue val] (-> val (.s)))
(defmethod unbox-value NullValue$  [^NullValue _]     nil)
(defmethod unbox-value EmptyValue$ [^EmptyValue _]    nil)
(defmethod unbox-value RawValue    [^RawValue val]    (unbox-raw-value val))
(defmethod unbox-value Value       [^Value val]       (throw (RuntimeException. (str "Unknown value type: " val))))

(defn Row->map [^Row row]
  (zipmap
    (map #(.name %)  (scala/scala-seq->vec (.fields row)))
    (map unbox-value (scala/scala-seq->vec (.values row)))))

(defprotocol MysqlFinagleClojureClient
  (query [this sql])
  (select [this sql f])
  (prepare [this sql])
  (ping [this]))

(defn builder []
  Mysql)

(defn- param [p]
  (reify Stack$Param (default [this] p)))

(defn ^MysqlClient with-credentials [client user pwd]
  (.withCredentials client user pwd))

(defn ^MysqlClient with-database [client db]
  (.withDatabase client db))

(defn ^MysqlClient configured [client stack-param]
  (.configured client stack-param (param stack-param)))

(defn ^MysqlClient set-tracer [client ^Tracer tracer]
  (configured client (com.twitter.finagle.package$param$Tracer. tracer)))

(defn ^MysqlClient set-stats-receiver [client ^StatsReceiver rcvr]
  (configured client (com.twitter.finagle.package$param$Stats. rcvr)))

(defn ^MysqlClient set-label [client label]
  (configured client (com.twitter.finagle.package$param$Label. label)))

(defn mysql-client [^Client client]
  (reify MysqlFinagleClojureClient
    (query [this sql]
      (.query client sql))
    (select [this sql f]
      (.select client sql (scala/Function* f)))
    (prepare [this sql]
      (.prepare client sql))
    (ping [this]
      (.ping client))))

(defn ^Client build [^MysqlClient client server]
  (-> client (.newRichClient server) (mysql-client)))
