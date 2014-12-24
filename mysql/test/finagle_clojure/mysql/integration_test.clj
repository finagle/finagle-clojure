(ns finagle-clojure.mysql.integration-test
  (:import (com.twitter.finagle.exp.mysql BigDecimalValue)
           (com.twitter.util Future)
           (java.sql Date Timestamp)
           (java.util TimeZone))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.mysql.client :refer :all]
            [finagle-clojure.futures :as f]
            [finagle-clojure.scala :as scala]))

(System/setProperty "java.net.preferIPv4Stack" "true")
(TimeZone/setDefault (TimeZone/getTimeZone "UTC"))

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

    (fact "it can create and update a table"

      (-> (query db "CREATE TABLE widgets (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       sprockets SMALLINT,
                       name VARCHAR(255),
                       description TEXT,
                       price DECIMAL(10,2),
                       mfd_on DATE,
                       created_at TIMESTAMP
                     )")
          (mapfn ok?)
          (f/await))
      => true

      (-> (prepare db "INSERT INTO widgets (name, description, sprockets, price, mfd_on, created_at)
                       VALUES (?, ?, ?, ?, ?, ?)")
          (exec
            "fizzbuzz"
            "a fizzy buzzy"
            (short 12)
            10.2M
            (Date/valueOf "2014-12-23")
            (Timestamp/valueOf "2014-12-24 10:11:12")
            )
          (mapfn affected-rows)
          (f/await))
      => 1)

    (fact "it understands how to unbox a wide range of data types"
      (let [rows (-> (prepare db "SELECT * FROM WIDGETS")
                     (exec)
                     (mapfn ResultSet->vec)
                     (f/await))]
        (count rows)
        => 1

        (-> rows (first) (get :id))
        => 1

        (-> rows (first) (get :name))
        => "fizzbuzz"

        (-> rows (first) (get :description))
        => "a fizzy buzzy"

        (-> rows (first) (get :price))
        => 10.2M

        (-> rows (first) (get :sprockets))
        => 12

        ;; TODO  Once bug fix is applied, assert with Date/valueOf rather than constituent parts.
        ;; (-> rows (first) (get :mfd_on))
        ;; => (Date/valueOf "2014-12-23")

        ;; FIXME There appears to be a bug in com.twitter.finagle.exp.mysql.DateValue.fromBytes that fails to
        ;;       zero out time values generated when getting a Calendar instance, leading to a Date object with
        ;;       erroneous time information. This must be fixed upstream in finagle-mysql.
        (-> rows (first) (get :mfd_on) (.getYear) (+ 1900)) ;; Years are post-1900
        => 2014

        (-> rows (first) (get :mfd_on) (.getMonth) (+ 1)) ;; Months are zero-indexed
        => 12

        (-> rows (first) (get :mfd_on) (.getDate))
        => 23

        (-> rows (first) (get :created_at))
        => (Timestamp/valueOf "2014-12-24 10:11:12")
        ))

    (fact "it can select from the table using the rich client"
      (let [rows (-> (select db "SELECT * FROM widgets" Row->map)
                     (mapfn scala/scala-seq->vec)
                     (f/await))]
        (count rows)
        => 1

        (-> rows (first) (select-keys [:id :name]))
        => {:id 1 :name "fizzbuzz"}
        ))

    (fact "it can select from the table using a prepared statement"
      (let [rows (-> (prepare db "SELECT * FROM widgets")
                     (select-stmt [] Row->map)
                     (mapfn scala/scala-seq->vec)
                     (f/await))]
        (count rows)
        => 1

        (-> rows (first) (select-keys [:id :name]))
        => {:id 1 :name "fizzbuzz"}
        ))

    (fact "it can clean up after the tests by deleting a table"
      (-> (query db "DROP TABLE widgets")
          (mapfn ok?)
          (f/await))
      => true)

    )

  (-> (mysql-client) (with-credentials "root" "") (with-database "finagle_clojure_test") (rich-client "localhost:3306")))
