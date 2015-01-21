(ns finagle-clojure.mysql.integration-test
  (:import (java.sql Date Timestamp)
           (java.util TimeZone))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.mysql.client :refer :all]
            [finagle-clojure.futures :as f]
            [finagle-clojure.scala :as scala]))

(System/setProperty "java.net.preferIPv4Stack" "true")
(TimeZone/setDefault (TimeZone/getTimeZone "UTC"))

(defn create-table [db]
  (f/await
    (query db "CREATE TABLE IF NOT EXISTS widgets (
                     id INT AUTO_INCREMENT PRIMARY KEY,
                     sprockets SMALLINT,
                     sproings FLOAT,
                     sprattles BIGINT,
                     name VARCHAR(255),
                     blank VARCHAR(255),
                     description TEXT,
                     price DECIMAL(10,2),
                     mfd_on DATE,
                     in_stock BOOLEAN,
                     part_number CHAR(10),
                     created_at TIMESTAMP
                   )")))

(defn drop-table [db]
  (f/await
    (query db "DROP TABLE widgets")))

(let [db (-> (mysql-client)
             (with-credentials "finagle" "finagle")
             (with-database "finagle_clojure_test")
             (rich-client "localhost:3306"))]

  (against-background [(before :contents (create-table db))
                       (after  :contents (drop-table db))]

    (fact :mysql "it can ping the database"
      (-> (ping db)
          (f/map* ok?)
          (f/await))
      => true)

    (fact :mysql "it can create and update a table"
      (-> (prepare db "INSERT INTO widgets (name, description, sprockets, sproings, sprattles, price, mfd_on, blank, in_stock, part_number, created_at)
                       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
          (exec
            "fizzbuzz"
            "a fizzy buzzy"
            (short 12)
            (float 18.3)
            432432423432
            10.2M
            (Date/valueOf "2014-12-23")
            nil
            true
            "HSC0424PP"
            (Timestamp/valueOf "2014-12-24 10:11:12")
            )
          (f/map* affected-rows)
          (f/await))
      => 1)

    (fact :mysql "it understands how to unbox a wide range of data types"
      (let [rows (-> (prepare db "SELECT * FROM WIDGETS")
                     (exec)
                     (f/map* ResultSet->vec)
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

        (-> rows (first) (get :sproings))
        => (float 18.3)

        (-> rows (first) (get :sprattles))
        => 432432423432

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

        (-> rows (first) (get :null_value))
        => nil

        (-> rows (first) (get :in_stock))
        => true

        (-> rows (first) (get :part_number))
        => "HSC0424PP"

        (-> rows (first) (get :created_at))
        => (Timestamp/valueOf "2014-12-24 10:11:12")
        ))

    (fact :mysql "it can select from the table using the rich client"
      (let [rows (-> (select-sql db "SELECT * FROM widgets" Row->map)
                     (f/await))]
        (count rows)
        => 1

        (-> rows (first) (select-keys [:id :name]))
        => {:id 1 :name "fizzbuzz"}
        )

      (-> (select-sql db "SELECT * FROM widgets")
          (f/await)
          (first)
          (select-keys [:id :name]))
      => {:id 1 :name "fizzbuzz"})

    (fact :mysql "it can select from the table using a prepared statement"
      (let [rows (-> (prepare db "SELECT * FROM widgets")
                     (select-stmt [] Row->map)
                     (f/await))]
        (count rows)
        => 1

        (-> rows (first) (select-keys [:id :name]))
        => {:id 1 :name "fizzbuzz"}
        )

      (-> (prepare db "SELECT * FROM widgets")
          (select-stmt [])
          (f/await)
          (first)
          (select-keys [:id :name]))
      => {:id 1 :name "fizzbuzz"})

    ))
