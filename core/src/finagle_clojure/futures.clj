(ns finagle-clojure.futures
  "Functions for working with `com.twitter.util.Future` objects.
  Futures are used to represent asynchronous operations in Finagle.
  
  More info on the use of Futures (in Scala):
  * https://twitter.github.io/finagle/guide/Futures.html
  * https://twitter.github.io/scala_school/finagle.html"
  (:refer-clojure :exclude [await ensure for map])
  (:require [finagle-clojure.scala :as scala]
            [finagle-clojure.timer :as timer]
            [finagle-clojure.duration :refer [->Duration]])
  (:import [com.twitter.util Await Duration Future Timer]))

(def ^:no-doc default-timer (timer/java-timer true))

(defn ^Future value
  "Returns a defined Future with the constant Return value `v`.

  *Arguments*:

    * `v`: the value that the new Future should be defined with.

  *Returns*:

  A new Future that is defined with the value `v`."
  [v]
  (Future/value v))

(defn ^Future exception
  "Returns a defined Future with a Throw value of t.
  This can be used to create a Future that has failed immediately.

  *Arguments*:

    * `t`: an instance of Throwable that the new Future should be defined with.

  *Returns*:

  A new Future that is defined with the Throw value `t`."
  [^Throwable t]
  (Future/exception t))

(defn ^:no-doc defined?
  "Does Future `f` have a value?
  If `defined?` the value of `f` can be retrieved with the function [[await]] without blocking.
  In general you shouldn't check the state of a Future like this and should rely on transformers like
  [[map]] & [[rescue]] to respond to changes in state.

  *Arguments*:

    * `f`: a Future

  *Returns*:

    * `true` if f has been realized (has a Return or Throw value)
    * `false` if it's still waiting for a value."
  [^Future f]
  (.isDefined f))

(defn await
  "Block until Future f is defined and return its value.
  You probably don't want to use this in production code.
  This should *never* be used in code that runs on a Finagle thread as it will block the reactor.

  This should only be used to wait for an asynchronous operation in a synchronous environment.

  *Arguments*:

    * `f`: a Future

  *Returns*:
    The value of `f` if it was successful.
    Throws the contained Exception if `f` was unsuccessful."
  [^Future f]
  (Await/result f))

(defn ^Future flatmap*
  "Apply scala.Function1 `fn1` with the value of Future `f` when `f` is defined with a value (not an exception).
  `fn1` should return a Future.

  flatmap* is used to represent asynchronous transformations of the successful result of a Future.
  E.g. calling to a RPC service with the result of a previous call.
  [[map*]] or [[map]] can be used to run synchronous transformations of the successful value of a Future.

  *Arguments*:

    * `f`: a Future
    * `fn1`: a scala.Function1 or Clojure fn that will execute when the (non-error) value of `f` is defined.
      Its result should be a Future.

  *Returns*:

    A new Future that will be defined when `f` is defined and the Future returned by calling `fn1` is defined.
    If `f` results in an error then `fn1` won't run.

  See [[scala/Function]], [[flatmap]]"
  [^Future f fn1]
  (.flatMap f (scala/lift->fn1 fn1)))

(defmacro flatmap
  "Sugar for constructing a scala.Function1 & applying [[flatmap*]] with it.

  *Arguments*:

    * `f`: a Future
    * `arg-binding`: is a vector with 1 element, the name to bind the successful value of Future `f` to.
    * `body`: will execute when the (non-error) value of f is defined.
       Its result should be a Future.

  *Returns*:

    A new Future that will be defined when `f` is defined and the Future returned by executing `body` is defined.
    If `f` results in an error then `body` won't run.

  See [[flatmap*]] & [[scala/Function]]"
  [^Future f arg-binding & body]
  `(flatmap* ~f (scala/Function ~arg-binding ~@body)))

(defn map*
  "Apply scala.Function1 `fn1` with the value of Future `f` when `f` is defined with a value (not exception).

  map* is used to represent synchronous transformations of the successful result of a Future.
  E.g. some CPU bound calculation like computing the hash of the result.
  No blocking operations should be run with map.
  [[flatmap*]] or [[flatmap]] can be used to run asynchronous transformations of the successful value of a Future.

  *Arguments*:

    * `f`: a Future
    * `fn1`: a scala.Function1 or Clojure fn that will execute when the (non-error) value of `f` is defined.

  *Returns*:

    A new Future that will be defined when `f` is defined and `fn1` has been applied to its value.
    The value of the new Future will be the result of `fn1` if `f` is successful.
    If `f` results in an error then `fn1` won't run.

  See [[scala/Function]], [[map]]"
  [^Future f fn1]
  (.map f (scala/lift->fn1 fn1)))

(defmacro map
  "Sugar for constructing a scala.Function1 & applying [[map*]] with it.

  *Arguments*:

    * `f`: a Future
    * `arg-binding`: is a vector with 1 element, the name to bind to the successful value of Future `f`.
    * `body`: will execute when the (non-error) value of `f` is defined.

  *Returns*:

    A new Future that will be defined when `f` is defined and `body` has been applied to its value.
    The value of the new Future will be the result of `body` if `f` is successful.
    If `f` results in an error then `body` won't run.

  See [[map*]] & [[scala/Function]]"
  [^Future f  arg-binding & body]
  `(map* ~f (scala/Function ~arg-binding ~@body)))

;; TODO support non-future values in intermediate bindings
;; like require <- or :<- or something for future values to do flatmap
(defmacro for
  "Like a scala for comprehension with Futures.
  `bindings` are pairs of [name future].
  `body` is executed when all Futures are ready and should return a Future.
  Bindings can refer to Futures defined before them
  (which will be realized)."
  [bindings & body]
  {:pre [(coll? bindings) (even? (count bindings))]}
  (let [[name val] (take 2 bindings)]
    (if-not (or (nil? name) (nil? val))
      `(flatmap ~val [~name]
        (for ~(drop 2 bindings) ~@body))
      `(do ~@body))))

(defn ^Future collect
  "Takes a seq of Futures, returns a Future of a seq of their values.

  *Arguments*:

    * `future-seq`: a Clojure seq of Futures

  *Returns*:

    A new Future that will be defined when all Futures in `future-seq` have been defined.
    The value of that Future will be a Clojure seq of the values of the Futures in `future-seq`."
  [future-seq]
  {:pre [(not (nil? future-seq))]}
  (map (Future/collect (scala/seq->scala-buffer future-seq))
    [scala-seq]
    (scala/scala-seq->vec scala-seq)))

(defn ^Future rescue*
  "Apply scala.PartialFunction `pfn` with the value of Future `f` when `f` is defined with an unsuccessful value (a Throwable).
  This is like [[flatmap*]] for when a Future results in an error.

  rescue* is used to represent asynchronous transformations of the unsuccessful result of a Future.
  [[handle*]] or [[handle]] can be used to run synchronous transformations of the unsuccessful value of a Future.

  *Arguments*:

    * `f`: a Future
    * `pfn`: a scala.PartialFunction or Clojure fn that will execute when the value of `f` is defined with an unsuccessful value.
      The return value should be a Future.

  *Returns*:

    A new Future that will be defined when `f` is defined and the Future returned by calling `pfn` is defined.
    If `f` is successful then `pfn` won't run.

  See [[scala/Function]], [[rescue]]"
  [^Future f pfn]
  (.rescue f (scala/lift->fn1 pfn)))

(defmacro rescue
  "Sugar for constructing a scala.PartialFunction & applying [[rescue*]] with it.
  This is like [[flatmap]] for when a Future results in an error.

  *Arguments*:

    * `f`: a Future
    * `arg-binding`: is a vector with 1 element, the name to bind the unsuccessful value of Future `f`.
    * `body`: will execute when the (error) value of `f` is defined.
      The return value should be a Future.

  *Returns*:

    A new Future that will be defined when `f` is defined and the Future returned by calling `body` is defined.
    If `f` is successful then `body` won't run.

  See [[rescue*]] & [[scala/Function]]"
  [^Future f arg-binding & body]
  `(rescue* ~f (scala/Function ^Throwable ~arg-binding ~@body)))

(defn ^Future handle*
  "Apply scala.PartialFunction pfn with the value of Future f when f is defined with a Throw (a Throwable).
  This is like [[map*]] for when a Future results in an error.

  handle* is used to represent synchronous transformations of the unsuccessful result of a Future.
  E.g. some CPU bound calculation like computing the hash of the result.
  No blocking operations should be run with handle*.
  [[rescue*]] or [[rescue]] can be used to run asynchronous transformations of the unsuccessful value of a Future.

  *Arguments*:

    * `f`: a Future
    * `pfn`: a scala.PartialFunction or Clojure fn that will execute when the value of `f` is defined with an unsuccessful value.

  *Returns*:

    A new Future that will be defined when `f` is defined and the Future returned by calling `pfn` is defined.
    If `f` is successful then `pfn` won't run.

  See [[scala/Function]], [[handle]]"
  [^Future f pfn]
  (.handle f (scala/lift->fn1 pfn)))

(defmacro handle
  "Sugar for constructing a scala.PartialFunction & applying [[handle*]] with it.
  This is like [[map]] for when a Future results in an error.

  *Arguments*:

    * `f`: a Future
    * `arg-binding`: is a vector with 1 element, the name to bind the value of Future `f`.
    * `body`: will execute when the (error) value of `f` is defined.

  *Returns*:

    A new Future that will be defined when `f` is defined and the Future returned by calling `body` is defined.
    If `f` is successful then `body` won't run.

  See [[handle*]] & [[scala/Function]]"
  [^Future f arg-binding & body]
  `(handle* ~f (scala/Function ^Throwable ~arg-binding ~@body)))

(defn ^Future ensure*
  "Apply scala.Function0 `fn0` when Future `f` is defined whether it is successful or not.
  This is primarily used for side-effects, the return value of `fn0` is ignored.

  *Arguments*:

    * `f`: a Future
    * `fn0`: a scala.Function0 or Clojure fn that will execute when `f` is defined.

  *Returns*:

    A new Future that will be defined when `f` is defined and `fn0` has executed.

  See [[scala/Function0]], [[ensure]]"
  [^Future f fn0]
  (.ensure f (scala/lift->fn0 fn0)))

(defmacro ensure
  [^Future f & body]
  "Sugar for constructing a scala.Function0 & applying [[ensure*]] with it.
  This is primarily used for side-effects, the return value is ignored.

  *Arguments*:

    * `f`: a Future
    * `body`: will execute when the (error) value of `f` is defined.
      The return value will be ignored.

  *Returns*:

    A new Future that will be defined when `f` is defined and `body` has executed.

  See [[ensure*]] & [[scala/Function0]]"
  `(ensure* ~f (scala/Function0 ~@body)))

(defn select
  "*Arguments*:

    * `f1`: a `Future`
    * `f2`: a `Future`

  *Returns*:

    A new Future that will be defined with the value of the Future that returns first (between `f1` & `f2`)."
  [^Future f1 ^Future f2]
  (.select f1 f2))

(defn within*
  "Returns a new Future that will error if not complete within `timeout-duration`.

  *Arguments*:

    * `f`: a `Future`
    * `timeout-duration`: a com.twitter.util.Duration indicating how long to wait before erroring.
    * `timer` (optional): a com.twitter.util.Timer that schedules the check to see if `f` isn't defined after `timeout-duration`.
      When not specified a default Timer is used.

  *Returns*:

    A new Future that will be defined with a TimeoutException if it isn't otherwise defined within `timeout-duration`.

  See [[finagle-clojure.duration/->Duration]], [[within]]"
  ([^Future f timeout-duration]
   (within* f timeout-duration default-timer))
  ([^Future f ^Duration timeout-duration ^Timer timer]
   (.within f timeout-duration timer)))

(defn within
  "Returns a new Future that will error if not complete within `timeout-value` `timeout-unit`.

  *Arguments*:

    * `f`: a `Future`
    * `timeout-value`
    * `timeout-unit`: the unit for the timeout Duration (see [[finagle-clojure.duration/->Duration]]).
    * `timer` (optional): a com.twitter.util.Timer that schedules the check to see if `f` isn't defined after `timeout-value`.
      When not specified a default Timer is used.

  *Returns*:

    A new Future that will be defined with a TimeoutException if it isn't otherwise defined within `timeout-value` `timeout-unit`s.

  See [[within*]] & [[finagle-clojure.duration/->Duration]]"
  ([^Future f timeout-value timeout-unit]
   (within f timeout-value timeout-unit default-timer))
  ([^Future f timeout-value timeout-unit timer]
   (within* f (->Duration timeout-value timeout-unit) timer)))

(defn transform
  "Returns a new `Future` that will transform its value when defined using the supplied fns.
  This is sugar around chaining flatmap & rescue, so the supplied functions should return a `Future`.

  *Arguments*:

    * `f`: a Future
    * `success-fn`: a Clojure IFn that will be called with the successful value of `f` when it's defined.
                    `success-fn` should return a Future.
    * `failure-fn`: a Clojure IFn that will be called with the exception that defines `f`.
                    `failure-fn` should return a Future. Exceptions will not be [[rescue]] if this isn't passed.

  *Returns*:

    A new Future that will be defined once `f` has been defined and the Future returned by the transformation fns has been defined.

  See [[flatmap]] & [[rescue]]."
  ([^Future f success-fn]
   (transform f success-fn nil))
  ([^Future f success-fn failure-fn]
     ;; order is important here, rescue before flatmap would transform the error into a successful value
    (let [with-success (flatmap f [v] (success-fn v))]
      (if (fn? failure-fn)
        (rescue with-success [t] (failure-fn t))
        with-success))))

(defmacro match-class
  "Sugar for conditional execution based on the class of the first argument `value`.
  Useful for handling different types of Exceptions in error callbacks.

  *Arguments*:

    * `value`: the class of this argument will be used to dispatch with `body`.
    * `body`: like the body of a cond expression: expected-class expr

  e.g.

  ````
  (match-class (IllegalArgumentException.)
    IllegalArgumentException (value :illegal-argument)
    Exception (value :an-error))
  ````

  *Returns*:

    The result of the expression that matches the class of `value`.

  See [[handle]], [[rescue]], [[transform]]."
  [value & body]
  `(condp instance? ~value
     ~@body))

(defn on-success*
  "Apply scala.Function1 `fn1` with the value of Future `f` when `f` is defined with a value (not exception).
  The return value of `fn1` is ignored.
  While this is used for side effects it still should not block the thread on which it runs.

  *Arguments*:

    * `f`: a Future
    * `fn1`: a scala.Function1 or Clojure fn that will execute when the (non-error) value of `f` is defined.
      `fn1` should return `scala.Unit`.

  *Returns*:

  A Future that will run `fn1` when it is defined with a successful value.

  See [[scala/Function]], [[scala/unit]], [[on-success]]"
  [^Future f fn1]
  (.onSuccess f (scala/lift->fn1 fn1)))

(defmacro on-success
  "Sugar for constructing a scala.Function1 & applying [[on-success*]] with it.

  *Arguments*:

    * `f`: a Future.
    * `arg-binding`: is a vector with 1 element, the name to bind the successful value of Future `f`.
    * `body`: will execute when the (non-error) value of `f` is defined.
        `body` will be transformed to automatically return a scala.Unit value for compatibility.

  *Returns*:

  A Future that will run `body` when it is defined with a successful value.

  See [[on-success*]] & [[scala/Function]]"
  [^Future f arg-binding & body]
  `(on-success* ~f (scala/Function ~arg-binding (do ~@body) scala.runtime.BoxedUnit/UNIT)))

(defn on-failure*
  "Apply scala.Function1 `fn1` with the value of Future `f` when `f` is defined with an exception.
  The return value of `fn1` is ignored.
  While this is used for side effects it still should not block the thread on which it runs.

  *Arguments*:

    * `f`: a Future
    * `fn1`: a scala.Function1 or Clojure fn that will execute when the error value of `f` is defined.
      `fn1` should return scala.Unit.

  *Returns*:

  A `Future` that will run `fn1` when it is defined with an error value.

  See [[scala/Function]], [[scala/unit]], [[on-failure]]"
  [^Future f fn1]
  (.onFailure f (scala/lift->fn1 fn1)))

(defmacro on-failure
  "Sugar for constructing a `scala.Function1` & applying [[on-failure*]] with it.

  *Arguments*:

    * `f`: a `Future`.
    * `arg-binding`: is a vector with 1 element, the name to bind the value of Future `f`.
    * `body`: will execute when the error value of `f` is defined.
        `body` will be transformed to automatically return a scala.Unit value for compatibility.

  *Returns*:

  A `Future` that will run `body` when it is defined with an error value.

  See [[on-failure*]] & [[scala/Function]]"
  [^Future f arg-binding & body]
  `(on-failure* ~f (scala/Function ~arg-binding (do ~@body) scala.runtime.BoxedUnit/UNIT)))
