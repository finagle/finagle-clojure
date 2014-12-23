(ns finagle-clojure.mysql.integration-test
  (:import (com.twitter.finagle.exp.mysql BigDecimalValue)
           (com.twitter.util Future))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.mysql.client :refer :all]
            [finagle-clojure.futures :as f]
            [finagle-clojure.scala :as scala]))

(System/setProperty "java.net.preferIPv4Stack" "true")

(defn mapfn [^Future fut func]
  (f/map* fut (scala/Function* func)))

(facts "integrating the MySQL client"
  (let [db (-> (mysql-client)
               (with-credentials "finagle" "finagle")
               (with-database "finagle_clojure_test")
               (rich-client "localhost:3306"))]

    (fact "it can ping the database"
      (-> (ping db)
          (mapfn ok?)
          (f/await))
      => true)

    (fact "it can create and query a table"

      (-> (query db "CREATE TABLE widgets (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(255),
                       description TEXT,
                       price DECIMAL(10,2),
                       created_at DATETIME
                     )")
          (mapfn ok?)
          (f/await))
      => true

      (-> (prepare db "INSERT INTO widgets (name, description, price)
                       VALUES (?, ?, ?)")
          (exec "fizzbuzz" "a fizzy buzzy" (BigDecimalValue/apply (scala.math.BigDecimal. 10.2M)))
          (mapfn affected-rows)
          (f/await))
      => 1

      (-> (prepare db "SELECT * FROM WIDGETS")
          (exec nil)
          (mapfn ResultSet->vec)
          (f/await))
      => [{:id 1 :name "fizzbuzz" :description "a fizzy buzzy" :price 10.2M :created_at nil}]

      (-> (query db "DROP TABLE widgets")
          (mapfn ok?)
          (f/await))
      => true
    ))

  (-> (mysql-client) (with-credentials "root" "") (with-database "finagle_clojure_test") (rich-client "localhost:3306")))
