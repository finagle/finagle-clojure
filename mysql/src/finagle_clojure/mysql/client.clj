(ns finagle-clojure.mysql.client
  (:import (com.twitter.finagle Stack$Param)
           (com.twitter.finagle.exp.mysql Row Value ByteValue ShortValue IntValue LongValue DoubleValue FloatValue
                                          StringValue DateValue Type RawValue BigDecimalValue NullValue EmptyValue
                                          NullValue$ EmptyValue$ Client PreparedStatement Result OK ResultSet Field)
           (com.twitter.finagle.exp Mysql$Client Mysql)
           (com.twitter.util Future)
           (scala Function1))
  (:require [finagle-clojure.scala :as scala]
            [finagle-clojure.options :as opt]))

(defmulti unbox-raw-value
  (fn [^RawValue v] (.typ v)))

(defmethod unbox-raw-value (Type/Date) [^RawValue val]
  (-> val (DateValue/unapply) (opt/get)))

(defmethod unbox-raw-value (Type/NewDecimal) [^RawValue val]
  (when-let [^scala.math.BigDecimal bd (-> val (BigDecimalValue/unapply) (opt/get))]
    (.underlying bd)))

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
(defmethod unbox-value RawValue    [^RawValue val]    (unbox-raw-value val))
(defmethod unbox-value Value       [^Value val]       (throw (RuntimeException. (str "Unknown value type: " val))))

(defmulti box-value class)

(defmethod box-value Byte           [b] (ByteValue. b))
(defmethod box-value Short          [s] (ShortValue. s))
(defmethod box-value Integer        [i] (IntValue. i))
(defmethod box-value Long           [l] (LongValue. l))
(defmethod box-value Float          [f] (FloatValue. f))
(defmethod box-value Double         [d] (DoubleValue. d))
(defmethod box-value String         [s] (StringValue. s))
(defmethod box-value nil            [_] NullValue$/MODULE$)
(defmethod box-value BigDecimal     [d] (BigDecimalValue/apply (scala.math.BigDecimal. d)))
(defmethod box-value java.util.Date [d] (box-value (java.sql.Date. (.getTime d))))
(defmethod box-value java.sql.Date  [d] (DateValue/apply d))

(defn- Field->keyword [^Field f]
  (-> f (.name) (keyword)))

(defn- Row->map [^Row row]
  (zipmap
    (map Field->keyword  (scala/scala-seq->vec (.fields row)))
    (map unbox-value (scala/scala-seq->vec (.values row)))))

(defn ResultSet->vec [^ResultSet rs]
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

;; Future[Seq[T]]
(defmulti ^Future select class)

(defmethod select PreparedStatement
  [^PreparedStatement stmt params ^Function1 fn1]
  (.select stmt (scala/seq->scala-buffer (map box-value params)) fn1))

(defmethod select Client
  [^Client client sql ^Function1 fn1]
  (.select client sql fn1))

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

(defn affected-rows [^Result result]
  (.affectedRows result))
