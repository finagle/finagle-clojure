(ns finagle-clojure.mysql.value
  "A collection of helpers for polymorphically boxing and unboxing Finagle-MySQL values,
  which is to say, subclasses of [[com.twitter.finagle.exp.mysql.Value]]."
  (:import (com.twitter.finagle.exp.mysql Value ByteValue ShortValue IntValue LongValue DoubleValue FloatValue
                                          StringValue Type RawValue BigDecimalValue NullValue EmptyValue
                                          NullValue$ EmptyValue$ DateValue$ TimestampValue)
           (java.util TimeZone Date))
  (:require [finagle-clojure.options :as opt]))

(defn- raw-type [^RawValue val] (.typ val))

;; These might reasonably be named apply and unapply, but I'm cautious of clashing with Clojure core.
(defmulti box class)
(defmulti unbox class)
(defmulti unbox-raw raw-type)

(def utc-zone
  (TimeZone/getTimeZone "UTC"))

;; Punting on arbitrary timezone conversions for now, as UTC<->UTC is almost always the right thing to do.
(def utc-timestamp-value
  (TimestampValue. utc-zone utc-zone))

(defmethod box Byte           [b] (ByteValue. b))
(defmethod box Short          [s] (ShortValue. s))
(defmethod box Integer        [i] (IntValue. i))
(defmethod box Long           [l] (LongValue. l))
(defmethod box Float          [f] (FloatValue. f))
(defmethod box Double         [d] (DoubleValue. d))
(defmethod box String         [s] (StringValue. s))
(defmethod box nil            [_] NullValue$/MODULE$)
(defmethod box BigDecimal     [d] (-> d (scala.math.BigDecimal.) (BigDecimalValue/apply)))
(defmethod box java.util.Date [d] (-> d (.getTime) (java.sql.Date.) (box)))
(defmethod box java.sql.Date  [d] (.apply DateValue$/MODULE$ d))
(defmethod box java.sql.Timestamp [t] (.apply utc-timestamp-value t))

(defmethod box :default [val]
  (throw (RuntimeException. (str "Don't know how to box value: " val))))

(defmethod unbox ByteValue   [^ByteValue b]   (-> b (.b)))
(defmethod unbox ShortValue  [^ShortValue s]  (-> s (.s)))
(defmethod unbox IntValue    [^IntValue i]    (-> i (.i)))
(defmethod unbox LongValue   [^LongValue l]   (-> l (.l)))
(defmethod unbox FloatValue  [^FloatValue f]  (-> f (.f)))
(defmethod unbox DoubleValue [^DoubleValue d] (-> d (.d)))
(defmethod unbox StringValue [^StringValue s] (-> s (.s)))
(defmethod unbox NullValue$  [^NullValue _]   nil)
(defmethod unbox EmptyValue$ [^EmptyValue _]  nil)
(defmethod unbox RawValue    [^RawValue val]  (unbox-raw val))

(defmethod unbox :default    [val]
  (throw (RuntimeException. (str "Don't know how to unbox value: " val))))

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
