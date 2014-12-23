(ns finagle-clojure.mysql.client
  (:import (com.twitter.finagle Stack$Param)
           (com.twitter.finagle.exp.mysql Client PreparedStatement)
           (com.twitter.finagle.exp.mysql Row Value ByteValue ShortValue IntValue LongValue DoubleValue FloatValue
                                          StringValue DateValue Type RawValue BigDecimalValue NullValue EmptyValue
                                          NullValue$ EmptyValue$ Client)
           (com.twitter.finagle.exp Mysql$Client Mysql)
           (com.twitter.util Future))
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

(defn Row->map [^Row row]
  (zipmap
    (map #(.name %)  (scala/scala-seq->vec (.fields row)))
    (map unbox-value (scala/scala-seq->vec (.values row)))))

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

(defn ^Mysql$Client configured [^Mysql$Client client stack-param]
  (.configured client stack-param (param stack-param)))

(defn ^Client rich-client
  "Converts the given `Mysql.Client` into a rich client, which is used for actually performing queries.

  *Arguments:*

    * `client`: a `Mysql.Client`
    * `dest`:

  *Returns:*

    a new [[com.twitter.finagle.exp.mysql.Client]] used for real queries"
  ([^Mysql$Client client dest]
    (.newRichClient client dest))
  ([^Mysql$Client client dest label]
    (.newRichClient client dest label)))

;; Future[Result]
(defn ^Future query [^Client client sql]
  (.query client sql))

;; Future[Seq[T]]
;; TODO wrap fn1?
(defn ^Future select [^Client client sql fn1]
  (.select client sql fn1))

(defn ^PreparedStatement prepare [^Client client sql]
  (.prepare client sql))

;; Future[Result]
(defn ^Future ping [^Client client]
  (.ping client))
