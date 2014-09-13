(ns finagle-clojure.duration
  "Functions for creating com.twitter.util.Duration and com.twitter.util.Time.

  Duration represents a length of time.
  It can be use to express timeouts.
  Functions such as [[finagle-clojure.futures/within*]] accept an explicit Duration object, while others
  such as [[finagle-clojure.futures/within]] create one for you."
  (:import [com.twitter.util Duration Time]
           [java.util.concurrent TimeUnit]))

;; TODO is it confusing for Time & Duration to be in the same ns?

(def ^:no-doc ->Duration-units {:s TimeUnit/SECONDS
                                TimeUnit/SECONDS TimeUnit/SECONDS
                                :ms TimeUnit/MILLISECONDS
                                TimeUnit/MILLISECONDS TimeUnit/MILLISECONDS
                                :us TimeUnit/MICROSECONDS
                                TimeUnit/MICROSECONDS TimeUnit/MICROSECONDS
                                :ns TimeUnit/NANOSECONDS
                                TimeUnit/NANOSECONDS TimeUnit/NANOSECONDS})

(defn ^Duration ->Duration
  "Create a new Duration.

  *Arguments*:

    * `value`: the value of this Duration.
    * `unit`: seconds or milliseconds, represented as `:s`, `:ms`, `:us`, `:ns`, or the corresponding `java.util.concurrent.TimeUnit`.

  *Returns*:

    A `com.twitter.util.Duration`."
  [value unit]
  (if-let [time-unit (get ->Duration-units unit)]
    (Duration/fromTimeUnit value time-unit)
    (throw (IllegalArgumentException. (str "Unit " unit " not found in " (keys ->Duration-units))))))

(defn ^Time ns->Time
  "Create a new Time from `value` nanoseconds.

  *Arguments*:

    * `value`: how many nanoseconds since epoch.

  *Returns*:

    A `com.twitter.util.Time`."
  [nanoseconds]
  (Time/fromNanoseconds nanoseconds))

(defn ^Time us->Time
  "Create a new Time from `value` microseconds.

  *Arguments*:

    * `value`: how many microseconds since epoch.

  *Returns*:

    A Time."
  [microseconds]
  (Time/fromMicroseconds microseconds))

(defn ^Time ms->Time
  "Create a new Time from `value` milliseconds.

  *Arguments*:

    * `value`: how many milliseconds since epoch.

  *Returns*:

    A Time."
  [milliseconds]
  (Time/fromMilliseconds milliseconds))

(defn ^Time s->Time
  "Create a new Time from `value` seconds.

  *Arguments*:

    * `value`: how many seconds since epoch.

  *Returns*:

    A Time."
  [seconds]
  (Time/fromSeconds seconds))

(def ^:no-doc ->Time-units {:us us->Time
                            TimeUnit/MICROSECONDS us->Time
                            :ns ns->Time
                            TimeUnit/NANOSECONDS ns->Time
                            :ms ms->Time
                            TimeUnit/MILLISECONDS ms->Time
                            :s s->Time
                            TimeUnit/SECONDS s->Time})

(defn ^Time ->Time
  "Create a new Time.

  *Arguments*:

    * `value`: the value of this Time.
    * `unit`: seconds, milliseconds, or nanoseconds, represented as `:s`, `:ms`, `:us`, `:ns`,
              or the corresponding `java.util.concurrent.TimeUnit`.

  *Returns*:

    A Time."
  [value unit]
  (if-let [f (get ->Time-units unit)]
    (f value)
    (throw (IllegalArgumentException. (str "Unit " unit " not found in " (keys ->Time-units))))))
