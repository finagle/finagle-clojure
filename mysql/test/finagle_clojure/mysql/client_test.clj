(ns finagle-clojure.mysql.client-test
  (:import (com.twitter.finagle.mysql Client Handshake$Database Handshake$Credentials Handshake$Charset OK)
           (com.twitter.finagle Stack$Parameterized)
           (scala.collection JavaConversions)
           (com.twitter.finagle Mysql))
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

(defn- database [^Client client]
  (when-let [p (extract-param client Handshake$Database)]
    (opt/get (.db p))))

(defn- charset [^Client client]
  (when-let [p (extract-param client Handshake$Charset)]
    (.charset p)))

(defn- credentials [^Client client]
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

  (facts "parsing responses"
    (facts "given an OK result"
      (-> (OK. 1 0 (int 0) (int 0) "")
          (affected-rows))
      => 1

      (-> (OK. 0 2 (int 0) (int 0) "")
          (insert-id))
      => 2

      (-> (OK. 0 0 (int 3) (int 0) "")
          (server-status))
      => 3

      (-> (OK. 0 0 (int 0) (int 4) "")
          (warning-count))
      => 4

      (-> (OK. 0 0 (int 0) (int 0) "a message")
          (message))
      => "a message"

      (-> (OK. 0 0 (int 0) (int 0) "a message")
          (ok?))
      => true)

    (facts "given an error result"
      (-> (com.twitter.finagle.mysql.Error. (short 1) "" "")
          (error-code))
      => 1

      (-> (com.twitter.finagle.mysql.Error. (short 0) "" "an error message")
          (message))
      => "an error message"

      (-> (com.twitter.finagle.mysql.Error. (short 0) "a SQL state" "")
          (sql-state))
      => "a SQL state"

      (-> (com.twitter.finagle.mysql.Error. (short 0) "" "")
          (ok?))
      => false)
    )
  )
