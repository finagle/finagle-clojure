(ns finagle-clojure.duration
  "Functions for creating `com.twitter.util.Duration`s and `com.twitter.util.Time`s."
  (:import [com.twitter.util Duration Time]
           [java.util.concurrent TimeUnit]))

;; TODO is it confusing for Time & Duration to be in the same ns?

(def ->Duration-units {:ms TimeUnit/MILLISECONDS
                       TimeUnit/MILLISECONDS TimeUnit/MILLISECONDS
                       :s TimeUnit/SECONDS
                       TimeUnit/SECONDS TimeUnit/SECONDS})

(defn ->Duration
  "Create a new `com.twitter.util.Duration`.

  *Arguments*:

    * `value`: the value of this `Duration`.
    * `unit`: seconds or milliseconds, represented as :s, :ms, or the corresponding `java.util.concurrent.TimeUnit`.

  *Returns*:

    A `com.twitter.util.Duration`."
  [value unit]
  (if-let [time-unit (get ->Duration-units unit)]
    (Duration/fromTimeUnit value time-unit)
    (throw (IllegalArgumentException. (str "Unit " unit " not found in " (keys ->Duration-units))))))

(defn ns->Time
  "Create a new `com.twitter.util.Time` from `value` nanoseconds.

  *Arguments*:

    * `value`: how many nanoseconds since epoch.

  *Returns*:

    A `com.twitter.util.Time`."
  [nanoseconds]
  (Time/fromNanoseconds nanoseconds))

(defn ms->Time
  "Create a new `com.twitter.util.Time` from `value` milliseconds.

  *Arguments*:

    * `value`: how many milliseconds since epoch.

  *Returns*:

    A `com.twitter.util.Time`."
  [milliseconds]
  (Time/fromMilliseconds milliseconds))

(defn s->Time
  "Create a new `com.twitter.util.Time` from `value` seconds.

  *Arguments*:

    * `value`: how many seconds since epoch.

  *Returns*:

    A `com.twitter.util.Time`."
  [seconds]
  (Time/fromSeconds seconds))

(def ->Time-units {:ns ns->Time
                   TimeUnit/NANOSECONDS ns->Time
                   :ms ms->Time
                   TimeUnit/MILLISECONDS ms->Time
                   :s s->Time
                   TimeUnit/SECONDS s->Time})

(defn ->Time
  "Create a new `com.twitter.util.Time`.

  *Arguments*:

    * `value`: the value of this `Time`.
    * `unit`: seconds, milliseconds, or nanoseconds, represented as :s, :ms, :ns, or the corresponding `java.util.concurrent.TimeUnit`.

  *Returns*:

    A `com.twitter.util.Time`."
  [value unit]
  (if-let [f (get ->Time-units unit)]
    (f value)
    (throw (IllegalArgumentException. (str "Unit " unit " not found in " (keys ->Time-units))))))
