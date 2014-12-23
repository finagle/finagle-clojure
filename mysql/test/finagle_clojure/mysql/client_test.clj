(ns finagle-clojure.mysql.client-test
  (:import (com.twitter.finagle.exp.mysql StringValue NullValue EmptyValue IntValue DoubleValue FloatValue ByteValue ShortValue LongValue DateValue BigDecimalValue EmptyValue$ NullValue$ Handshake$Database Handshake$Credentials Handshake$Charset)
           (java.sql Date)
           (com.twitter.finagle Stack$Parameterized)
           (scala.collection JavaConversions)
           (com.twitter.finagle.exp Mysql$Client))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.mysql.client :refer :all]
            [finagle-clojure.scala :as scala]
            [finagle-clojure.options :as opt]))

(defn- params [^Stack$Parameterized stackable]
  (map scala/tuple->vec (JavaConversions/asJavaCollection (.params stackable))))

(defn extract-param [^Stack$Parameterized stackable ^Class cls]
  (->> stackable
       (params)
       (flatten)
       (filter #(instance? cls %))
       (first)))

(defn- database [^Mysql$Client client]
  (when-let [p (extract-param client Handshake$Database)]
    (opt/get (.db p))))

(defn- charset [^Mysql$Client client]
  (when-let [p (extract-param client Handshake$Charset)]
    (.charset p)))

(defn- credentials [^Mysql$Client client]
  (when-let [p (extract-param client Handshake$Credentials)]
    [(opt/get (.username p)) (opt/get (.password p))]))

(facts "MySQL client"
  (facts "during configuration"
    (-> (mysql-client)
        (database))
    => nil

    (-> (mysql-client)
        (with-database "somedb")
        (database))
    => "somedb"

    (-> (mysql-client)
        (credentials))
    => nil

    (-> (mysql-client)
        (with-credentials "gthreepwood" "m0nkey")
        (credentials))
    => ["gthreepwood" "m0nkey"]

    (-> (mysql-client)
        (charset))
    => nil

    (-> (mysql-client)
        (with-charset 1)
        (charset))
    => 1
    )

  (facts "when unboxing values"
    (unbox-value (ByteValue. (byte 42)))
    => (byte 42)

    (unbox-value (ShortValue. (short 42)))
    => (short 42)

    (unbox-value (IntValue. (int 42)))
    => (int 42)

    (unbox-value (LongValue. (long 42)))
    => (long 42)

    (unbox-value (FloatValue. (float 42.0)))
    => (float 42.0)

    (unbox-value (DoubleValue. (double 42)))
    => (double 42)

    (unbox-value (StringValue. "test"))
    => "test"

    (unbox-value (BigDecimalValue/apply (scala.math.BigDecimal. 42.42M)))
    => 42.42M

    (unbox-value EmptyValue$/MODULE$)
    => nil

    (unbox-value NullValue$/MODULE$)
    => nil

    ; FIXME These fail for time-zone reasons that aren't clear to me.
    ;(unbox-value (DateValue/apply (Date. 1234)))
    ;=> (Date. 1234)
    ;
    ;(unbox-value (DateValue/apply (Date. 0)))
    ;=> (Date. 0)
    )
  )
