(ns finagle-clojure.mysql.client
  (:import (com.twitter.finagle Stack$Param)
           (com.twitter.finagle.mysql Row Client PreparedStatement Result OK ResultSet Field Parameter$ BigDecimalValue)
           (com.twitter.finagle Mysql)
           (com.twitter.util Future))
  (:require [finagle-clojure.scala :as scala]
            [finagle-clojure.mysql.value :as value]
            [finagle-clojure.futures :as f]))

(defn- Field->keyword [^Field f]
  (-> f (.name) (keyword)))

(defn Row->map
  "Given a Finagle MySQL row structure, convert it to a Clojure map of field names to idiomatic
  Java data structures. Raises an exception when it can't find an appropriate way to convert
  the boxed MySQL value.

  *Arguments:*

    * `row`: a [[com.twitter.finagle.mysql.Row]]

  *Returns:*

    a Clojure map of field/value pairs"
  [^Row row]
  (zipmap
    (map Field->keyword (scala/scala-seq->vec (.fields row)))
    (map value/unbox (scala/scala-seq->vec (.values row)))))

(defn ResultSet->vec [^ResultSet rs]
  "Given a Finagle MySQL result set, convert it to a vector of Clojure maps using `Row->map`. Raises
  an exception when it can't find an appropriate way to convert the boxed MySQL value.

  *Arguments:*

    * `rs`: a [[com.twitter.finagle.mysql.ResultSet]]

  *Returns:*

    a Clojure vector of maps"
  (->> (.rows rs)
       (scala/scala-seq->vec)
       (mapv Row->map)))

(defn- param [p]
  (reify Stack$Param (default [this] p)))

(defn ^Client mysql-client
  "Initialize a configurable MySQL stack client. The `rich-client` function must be called once configured
  in order to execute live queries against this, like so:

  ```
  (-> (mysql-client)
      (with-database \"somedb\")
      (with-credentials \"test\" \"test\")
      (rich-client))
  ```

  *Arguments:*

    * None.

  *Returns:*

    A new [[com.twitter.finagle.mysql.Client]]."
  []
  (Mysql/client))

(defn ^Client with-credentials
  "Configure the given Mysql `Client` with connection credentials.

  *Arguments:*

    * `client`: a Mysql `Client`
    * `user`: a database username
    * `pwd`: a database password

  *Returns:*

    the given Mysql `Client`"
  [^Client client user pwd]
  (.withCredentials client user pwd))

(defn ^Client with-database
  "Configure the given Mysql `Client` with a database.

  *Arguments:*

    * `client`: a Mysql `Client`
    * `db`: the name of a database

  *Returns:*

    the given Mysql `Client`"
  [^Client client db]
  (.withDatabase client db))

(defn ^Client with-charset
  "Configure the given Mysql `Client` with a charset.

  *Arguments:*

    * `client`: a Mysql `Client`
    * `charset`: a number representing the charset

  *Returns:*

    the given Mysql `Client`"
  [^Client client charset]
  (.withCharset client (short charset)))

(defn ^Client configured
  "Configure the given Mysql `Client` with an arbitrary `Stack.Param`.

  *Arguments:*

    * `client`: a Mysql `Client`
    * `charset`: an arbitrary `Stack.Param`

  *Returns:*

    the given Mysql `Client`"
  [^Client client stack-param]
  (.configured client stack-param (param stack-param)))

(defn ^Client rich-client
  "Converts the given Mysql `Client` into a rich client, which is used for actually performing queries.

  *Arguments:*

    * `client`: a Mysql `Client`
    * `dest`: a string or `Name` of the server location

  *Returns:*

    a new [[com.twitter.finagle.mysql.Client]] used for real queries"
  ([^Client client dest]
    (.newRichClient client dest))
  ([^Client client dest label]
    (.newRichClient client dest label)))

(defn ^Future query
  "Given a rich client and a SQL string, executes the SQL and returns the result as a Future[Result].

  *Arguments:*

    * `client`: a rich MySQL `Client`
    * `sql`: a SQL string

  *Returns:*

    a `Future` containing a [[com.twitter.finagle.mysql.Result]]"
  [^Client client sql]
  (.query client sql))

(defn ^Future select-sql
  "Given a rich client, a SQL string, and a mapping function, executes the SQL and returns the result as a
  Future[Seq[T]], where T is the type yielded by the given mapping function.
  *Arguments:*

    * `client`: a rich MySQL `Client`
    * `sql`: a SQL string
    * `fn1` (optional): a Clojure or Scala Function1 that accepts a [[com.twitter.finagle.mysql.Row]]

  *Returns:*

    a `Future` containing a Clojure vector whose contents are derived from `fn1` (if given) or mapped to a
    Clojure hashmap of column/value pairs (if not)"
  ([^Client client sql]
    (select-sql client sql Row->map))
  ([^Client client sql fn1]
    (-> client
        (.select sql (scala/lift->fn1 fn1))
        (f/map* scala/scala-seq->vec))))

(defprotocol ToParameter
  (->Parameter [v]))

(extend-protocol ToParameter
  BigDecimal (->Parameter [d] (->> d (scala.math.BigDecimal.) (BigDecimalValue/apply) (.unsafeWrap Parameter$/MODULE$)))
  nil        (->Parameter [_] (.unsafeWrap Parameter$/MODULE$ nil))
  Object     (->Parameter [o] (.unsafeWrap Parameter$/MODULE$ o)))

(defn ^Future select-stmt
  "Given a `PreparedStatement`, a vector of params, and a mapping function, executes the parameterized statement
  and returns the result as a `Future[Seq[T]]`, where T is the type yielded by the given mapping function.

  *Arguments*:

    * `stmt`: a `PreparedStatement`, generally derived from the `prepare` function
    * `params`: a Clojure vector of params
    * `fn1` (optional): a Clojure or Scala Function1 that accepts a [[com.twitter.finagle.mysql.Row]]

  *Returns:*

    a `Future` containing a Clojure vector whose contents are derived from `fn1` (if given) or mapped to a
    Clojure hashmap of column/value pairs (if not)"
  ([^PreparedStatement stmt params]
    (select-stmt stmt params Row->map))
  ([^PreparedStatement stmt params fn1]
    (let [params (scala/seq->scala-buffer (map ->Parameter params))
          fn1    (scala/lift->fn1 fn1)]
      (-> stmt
          (.select params fn1)
          (f/map* scala/scala-seq->vec)))))

(defn ^PreparedStatement prepare
  "Given a rich client and a SQL string, returns a `PreparedStatement` ready to be parameterized and executed.

  *Arguments:*

    * `client`: a rich MySQL `Client`
    * `sql`: a SQL string

  *Returns:*

    a [[com.twitter.finagle.mysql.PreparedStatement]]"
  [^Client client sql]
  (.prepare client sql))

(defn ^Future ping
  "Given a rich client, pings it to verify connectivity.

  *Arguments:*

    * `client`: a rich MySQL `Client`

  *Returns:*

    a `Future` containing a [[com.twitter.finagle.mysql.Result]]"
  [^Client client]
  (.ping client))

(defn ^Future exec
  "Given a prepared statement and a set of parameters, executes the statement and returns the result as a future.

  *Arguments:*

    * `client`: a rich MySQL `Client`
    * `params`: a variable number of args with which to parameterize the SQL statement

  *Returns:*

    a `Future` containing a [[com.twitter.finagle.mysql.Result]]"
  [^PreparedStatement stmt & params]
  (->> (or params [])
       (map ->Parameter)
       (scala/seq->scala-buffer)
       (.apply stmt)))

(defn ok?
  "Given a `Result`, returns true if that result was not an error.

  *Arguments:*

    * `result`: a [[com.twitter.finagle.mysql.Result]]

  *Returns:*

    true if `result` is an instance of [[com.twitter.finagle.mysql.OK]] or
    [[com.twitter.finagle.mysql.ResultSet]], false otherwise"
  [^Result result]
  (or (instance? OK result) (instance? ResultSet result)))

(defn affected-rows
  "Given a `OK` result, returns the number of rows affected (deleted, inserted, created) by that query.

   *Arguments:*

     * `result`: a [[com.twitter.finagle.mysql.OK]]

   *Returns:*

     the number of rows affected by the query that generated the given `Result`"
  [^OK result]
  (.affectedRows result))

(defn insert-id
  "Given an `OK` result from an insert operation, return the ID of the inserted row.

  *Arguments:*

    * `result`: a [[com.twitter.finagle.mysql.OK]]

  *Returns:*

    the ID of the newly-inserted row"
  [^OK result]
  (.insertId result))

(defn server-status
  "Given an `OK` result, return the server status.

  *Arguments:*

    * `result`: a [[com.twitter.finagle.mysql.OK]]

  *Returns:*

    the current server status, as an int"
  [^OK result]
  (.serverStatus result))

(defn warning-count
  "Given an `OK` result, returns the count of warnings associated with the result.

  *Arguments:*

    * `result`: a [[com.twitter.finagle.mysql.OK]]

  *Returns:*

    the warning count of the result"
  [^OK result]
  (.warningCount result))

(defn message
  "Given a `Result` of subtype `OK` or `Error`, returns any message associated with that result.

  *Arguments:*

    * `result`: a [[com.twitter.finagle.mysql.OK]] or [[com.twitter.finagle.mysql.Error]]

  *Returns:*

    any message associated with the given `result`"
  [^Result result]
  (.message result))

(defn error-code
  "Given an `Error` result, returns the error code.

  *Arguments:*

    * `result`: a [[com.twitter.finagle.mysql.Error]]

  *Returns:*

    the error code of the result"
  [^com.twitter.finagle.mysql.Error result]
  (.code result))

(defn sql-state
  "Given an `Error` result, returns the SQL state.

  *Arguments:*

    * `result`: a [[com.twitter.finagle.mysql.Error]]

  *Returns:*

    the SQL state of the result"
  [^com.twitter.finagle.mysql.Error result]
  (.sqlState result))
