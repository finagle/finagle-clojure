# mysql

This module contains wrappers for `com.twitter.finagle.exp.mysql.Client` and automatic boxing/unboxing of values
to idiomatic Clojure vectors of hashmaps.

### Usage

```clojure
(ns user
  (:require [finagle-clojure.mysql.client :refer :all]
            [finagle-clojure.futures :as f]
            [finagle-clojure.scala :as scala]))

(let [db (-> (mysql-client)
             (with-credentials "test" "test")
             (with-database "some_database")
             (rich-client "localhost:3306"))]
  (-> (prepare db "SELECT * FROM widgets WHERE manufacturer_id = ? AND sold_count > ?")
      (select-stmt [12 5])
      (f/map* scala/scala-seq->vec)
      (f/await)) ;; => [{:id 1 :manufacturer_id 12 :sold_count 12} ...]
      )
```

### Dependency

    [finagle-clojure/mysql "0.2.1-SNAPSHOT"]

### Namespaces

* `finagle-clojure.mysql.client`: a collection of functions for connecting to a MySQL client and parsing the results
* `finagle-clojure.mysql.value`: private helpers for translating values between Java/Clojure types and internal
  finagle-mysql types
