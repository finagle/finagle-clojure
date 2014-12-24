(ns finagle-clojure.mysql.client
  (:import (com.twitter.finagle Stack$Param)
           (com.twitter.finagle.exp.mysql Row Value ByteValue ShortValue IntValue LongValue DoubleValue FloatValue
                                          StringValue Type RawValue BigDecimalValue NullValue EmptyValue
                                          NullValue$ EmptyValue$ Client PreparedStatement Result OK ResultSet Field
                                          DateValue$ TimestampValue)
           (com.twitter.finagle.exp Mysql$Client Mysql)
           (com.twitter.util Future)
           (scala Function1)
           (java.util TimeZone))
  (:require [finagle-clojure.scala :as scala]
            [finagle-clojure.options :as opt]))

(defmulti unbox-raw-value
  (fn [^RawValue val] (.typ val)))

(def utc-zone
  (TimeZone/getTimeZone "UTC"))

;; Punting on arbitrary timezone conversions for now, as UTC<->UTC is almost always the right thing to do.
(def utc-timestamp-value
  (TimestampValue. utc-zone utc-zone))

(defmethod unbox-raw-value (Type/NewDecimal) [^RawValue val]
  (when-let [^scala.math.BigDecimal bd (-> val (BigDecimalValue/unapply) (opt/get))]
    (.underlying bd)))

(defmethod unbox-raw-value (Type/Date) [^RawValue val]
  (-> (.unapply DateValue$/MODULE$ val) (opt/get)))

(defmethod unbox-raw-value (Type/Time) [^RawValue val]
  (-> (.unapply utc-timestamp-value val) (opt/get)))

(defmethod unbox-raw-value (Type/DateTime) [^RawValue val]
  (-> (.unapply utc-timestamp-value val) (opt/get)))

(defmethod unbox-raw-value (Type/Timestamp) [^RawValue val]
  (-> (.unapply utc-timestamp-value val) (opt/get)))

(defmethod unbox-raw-value :default [o]
  (throw (RuntimeException. (str "Don't know how to unbox value: " o))))

(defmulti unbox-value class)

(defmethod unbox-value ByteValue   [^ByteValue val]   (-> val (.b)))
(defmethod unbox-value ShortValue  [^ShortValue val]  (-> val (.s)))
(defmethod unbox-value IntValue    [^IntValue val]    (-> val (.i)))
(defmethod unbox-value LongValue   [^LongValue val]   (-> val (.l)))
(defmethod unbox-value FloatValue  [^FloatValue val]  (-> val (.f)))
(defmethod unbox-value DoubleValue [^DoubleValue val] (-> val (.d)))
(defmethod unbox-value StringValue [^StringValue val] (-> val (.s)))
(defmethod unbox-value NullValue$  [^NullValue _]     nil)
(defmethod unbox-value EmptyValue$ [^EmptyValue _]    nil)
(defmethod unbox-value RawValue    [^RawValue val]
  (unbox-raw-value val))
(defmethod unbox-value Value       [^Value val]
  (throw (RuntimeException. (str "Don't know how to unbox value: " val))))

(defmulti box-value class)

(defmethod box-value Byte           [b] (ByteValue. b))
(defmethod box-value Short          [s] (ShortValue. s))
(defmethod box-value Integer        [i] (IntValue. i))
(defmethod box-value Long           [l] (LongValue. l))
(defmethod box-value Float          [f] (FloatValue. f))
(defmethod box-value Double         [d] (DoubleValue. d))
(defmethod box-value String         [s] (StringValue. s))
(defmethod box-value nil            [_] NullValue$/MODULE$)
(defmethod box-value BigDecimal     [d]
  (BigDecimalValue/apply (scala.math.BigDecimal. d)))
(defmethod box-value java.util.Date [d]
  (box-value (java.sql.Date. (.getTime d))))
(defmethod box-value java.sql.Date  [d]
  (.apply DateValue$/MODULE$ d))
(defmethod box-value java.sql.Timestamp [t]
  (.apply utc-timestamp-value t))
(defmethod box-value :default [val]
  (throw (RuntimeException. (str "Don't know how to box value: " val))))

(defn- Field->keyword [^Field f]
  (-> f (.name) (keyword)))

(defn Row->map
  "Given a Finagle MySQL row structure, convert it to a Clojure map of field names to idiomatic
  Java data structures. Raises an exception when it can't find an appropriate way to convert
  the boxed MySQL value.

  *Arguments:*

    * `row`: a [[com.twitter.finagle.exp.mysql.Row]]

  *Returns:*

    a Clojure map of field/value pairs"
  [^Row row]
  (zipmap
    (map Field->keyword  (scala/scala-seq->vec (.fields row)))
    (map unbox-value (scala/scala-seq->vec (.values row)))))

(defn ResultSet->vec [^ResultSet rs]
  "Given a Finagle MySQL result set, convert it to a vector of Clojure maps using `Row->map`. Raises
  an exception when it can't find an appropriate way to convert the boxed MySQL value.

  *Arguments:*

    * `rs`: a [[com.twitter.finagle.exp.mysql.ResultSet]]

  *Returns:*

    a Clojure vector of maps"
  (->> (.rows rs)
       (scala/scala-seq->vec)
       (map Row->map)))

(defn- param [p]
  (reify Stack$Param (default [this] p)))

(defn ^Mysql$Client mysql-client
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

    A new [[com.twitter.finagle.exp.Mysql$Client]]."
  []
  (Mysql/client))

(defn ^Mysql$Client with-credentials
  "Configure the given `Mysql.Client` with connection credentials.

  *Arguments:*

    * `client`: a `Mysql.Client`
    * `user`: a database username
    * `pwd`: a database password

  *Returns:*

    the given `Mysql.Client`"
  [^Mysql$Client client user pwd]
  (.withCredentials client user pwd))

(defn ^Mysql$Client with-database
  "Configure the given `Mysql.Client` with a database.

  *Arguments:*

    * `client`: a `Mysql.Client`
    * `db`: the name of a database

  *Returns:*

    the given `Mysql.Client`"
  [^Mysql$Client client db]
  (.withDatabase client db))

(defn ^Mysql$Client with-charset
  "Configure the given `Mysql.Client` with a charset.

  *Arguments:*

    * `client`: a `Mysql.Client`
    * `charset`: a number representing the charset

  *Returns:*

    the given `Mysql.Client`"
  [^Mysql$Client client charset]
  (.withCharset client (short charset)))

(defn ^Mysql$Client configured
  "Configure the given `Mysql.Client` with an arbitrary `Stack.Param`.

  *Arguments:*

    * `client`: a `Mysql.Client`
    * `charset`: an arbitrary `Stack.Param`

  *Returns:*

    the given `Mysql.Client`"
  [^Mysql$Client client stack-param]
  (.configured client stack-param (param stack-param)))

(defn ^Client rich-client
  "Converts the given `Mysql.Client` into a rich client, which is used for actually performing queries.

  *Arguments:*

    * `client`: a `Mysql.Client`
    * `dest`: a string or `Name` of the server location

  *Returns:*

    a new [[com.twitter.finagle.exp.mysql.Client]] used for real queries"
  ([^Mysql$Client client dest]
    (.newRichClient client dest))
  ([^Mysql$Client client dest label]
    (.newRichClient client dest label)))

;; Future[Result]
(defn ^Future query
  "Given a rich client and a SQL string, executes the SQL and returns the result as a Future[Result].

  *Arguments:*

    * `client`: a rich MySQL `Client`
    * `sql`: a SQL string

  *Returns:*

    a `Future` containing a [[com.twitter.finagle.exp.mysql.Result]]"
  [^Client client sql]
  (.query client sql))

(defn- ^com.twitter.util.Function wrap-fn [f]
  (if-not (instance? scala.Function f)
    (scala/Function* f)
    f))

(defn ^Future select
  "Given a rich client, a SQL string, and a mapping function, executes the SQL and returns the result as a
  Future[Seq[T]], where T is the type yielded by the given mapping function.


  *Arguments:*

    * `client`: a rich MySQL `Client`
    * `sql`: a SQL string
    * `fn1`: a Clojure or Scala Function1 that accepts a [[com.twitter.finagle.exp.mysql.Row]] and returns an arbitrary type

  *Returns:*

    a `Future` containing a Scala Seq whose contents are derived from the given function"
  [^Client client sql ^Function1 fn1]
  (.select client sql (wrap-fn fn1)))

(defn ^Future select-stmt
  "Given a `PreparedStatement`, a vector of params, and a mapping function, executes the parameterized statement
  and returns the result as a `Future[Seq[T]]`, where T is the type yielded by the given mapping function.

  *Arguments*:

    * `stmt`: a `PreparedStatement`, generally derived from the `prepare` function
    * `params`: a Clojure vector of params
    * `fn1`: a Clojure or Scala Function1 that accepts a [[com.twitter.finagle.exp.mysql.Row]] and returns an arbitrary type

  *Returns:*

    a `Future` containing a Scala Seq whose contents are derived from the given function"
  [^PreparedStatement stmt params ^Function1 fn1]
  (.select stmt (scala/seq->scala-buffer (map box-value params)) (wrap-fn fn1)))

(defn ^PreparedStatement prepare
  "Given a rich client and a SQL string, returns a `PreparedStatement` ready to be parameterized and executed.

  *Arguments:*

    * `client`: a rich MySQL `Client`
    * `sql`: a SQL string

  *Returns:*

    a [[com.twitter.finagle.exp.mysql.PreparedStatement]]"
  [^Client client sql]
  (.prepare client sql))

(defn ^Future ping
  "Given a rich client, pings it to verify connectivity.

  *Arguments:*

    * `client`: a rich MySQL `Client`

  *Returns:*

    a `Future` containing a [[com.twitter.finagle.exp.mysql.Result]]"
  [^Client client]
  (.ping client))

(defn ^Future exec
  "Given a prepared statement and a set of parameters, executes the statement and returns the result as a future.

  *Arguments:*

    * `client`: a rich MySQL `Client`
    * `params`: a variable number of args with which to parameterize the SQL statement

  *Returns:*

    a `Future` containing a [[com.twitter.finagle.exp.mysql.Result]]"
  [^PreparedStatement stmt & params]
  (->> (or params [])
       (map box-value)
       (scala/seq->scala-buffer)
       (.apply stmt)))

(defn ok?
  "Given a `Result`, returns true if that result was not an error.

  *Arguments:*

    * `result`: a [[com.twitter.finagle.exp.mysql.Result]]

  *Returns:*

    true if `result` is an instance of [[com.twitter.finagle.exp.mysql.OK]] or
    [[com.twitter.finagle.exp.mysql.ResultSet]], false otherwise"
  [^Result result]
  (or (instance? OK result) (instance? ResultSet result)))

(defn affected-rows
  "Given a `Result`, returns the number of rows affected (deleted, inserted, created) by that query.

   *Arguments:*

     * `result`: a [[com.twitter.finagle.exp.mysql.Result]]

   *Returns:*

     the number of rows affected by the query that generated the given `Result`"
  [^Result result]
  (.affectedRows result))
