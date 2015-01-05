(ns finagle-clojure.mysql.client-test
  (:import (com.twitter.finagle.exp.mysql Handshake$Database Handshake$Credentials Handshake$Charset)
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
  )
