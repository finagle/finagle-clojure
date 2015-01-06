(ns finagle-clojure.mysql.value
  "A collection of private helpers for polymorphically boxing and unboxing Finagle-MySQL values,
  which is to say, subclasses of [[com.twitter.finagle.exp.mysql.Value]]."
  (:import (com.twitter.finagle.exp.mysql Value ByteValue ShortValue IntValue LongValue DoubleValue FloatValue
                                          StringValue Type RawValue BigDecimalValue NullValue EmptyValue
                                          NullValue$ EmptyValue$ DateValue$ TimestampValue)
           (java.util TimeZone))
  (:require [finagle-clojure.options :as opt]))

(defprotocol BoxValue
  (box [v]))

(defprotocol UnboxValue
  (unbox [v]))

(defn- raw-type [^RawValue val] (.typ val))
(defmulti unbox-raw raw-type)

(def utc-zone
  (TimeZone/getTimeZone "UTC"))

(def utc-timestamp-value
  (TimestampValue. utc-zone utc-zone))

(extend-protocol BoxValue
  Byte               (box [b] (ByteValue. b))
  Short              (box [s] (ShortValue. s))
  Integer            (box [i] (IntValue. i))
  Long               (box [l] (LongValue. l))
  Float              (box [f] (FloatValue. f))
  Double             (box [d] (DoubleValue. d))
  String             (box [s] (StringValue. s))
  nil                (box [_] nil)
  BigDecimal         (box [d] (-> d (scala.math.BigDecimal.) (BigDecimalValue/apply)))
  java.util.Date     (box [d] (-> d (.getTime) (java.sql.Date.) (box)))
  java.sql.Date      (box [d] (.apply DateValue$/MODULE$ d))
  java.sql.Timestamp (box [t] (.apply utc-timestamp-value t)))

(extend-protocol UnboxValue
  ByteValue   (unbox [^ByteValue b]   (-> b (.b)))
  ShortValue  (unbox [^ShortValue s]  (-> s (.s)))
  IntValue    (unbox [^IntValue i]    (-> i (.i)))
  LongValue   (unbox [^LongValue l]   (-> l (.l)))
  FloatValue  (unbox [^FloatValue f]  (-> f (.f)))
  DoubleValue (unbox [^DoubleValue d] (-> d (.d)))
  StringValue (unbox [^StringValue s] (-> s (.s)))
  NullValue$  (unbox [^NullValue _]   nil)
  EmptyValue$ (unbox [^EmptyValue _]  nil)
  RawValue    (unbox [^RawValue val]  (unbox-raw val)))

(defmethod unbox-raw (Type/NewDecimal) [^RawValue val]
  (when-let [^scala.math.BigDecimal bd (-> val (BigDecimalValue/unapply) (opt/get))]
    (.underlying bd)))

(defmethod unbox-raw (Type/Date) [^RawValue val]
  (-> (.unapply DateValue$/MODULE$ val) (opt/get)))

(defmethod unbox-raw (Type/Time) [^RawValue val]
  (-> (.unapply utc-timestamp-value val) (opt/get)))

(defmethod unbox-raw (Type/DateTime) [^RawValue val]
  (-> (.unapply utc-timestamp-value val) (opt/get)))

(defmethod unbox-raw (Type/Timestamp) [^RawValue val]
  (-> (.unapply utc-timestamp-value val) (opt/get)))

(defmethod unbox-raw :default [o]
  (throw (RuntimeException. (str "Don't know how to unbox value: " o))))
