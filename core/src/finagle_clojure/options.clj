(ns finagle-clojure.options
  (:import (scala Option)))

(defn ^Option option
  "Returns an Option with the given value `v`.

  *Arguments*:

    * `v`: the value that the new Option should be defined with.

  *Returns*:

    `Some(v)` if `v` is present and non-null, `None` otherwise"
  ([]
   (Option/empty))
  ([v]
   (Option/apply v)))

(defn empty? [^Option o]
  "Does Option `o` have a value? Returns true if so, false otherwise.

  *Arguments*:

    * `o`: an Option

  *Returns*:

    true if `v` is None, false otherwise"
  (.isEmpty o))

(defn get [^Option o]
  "Returns the value wrapped by `o`.
  Although the Scala implementation throws a `Predef.NoSuchElementException` if called
  on an empty Option, Clojure generally avoids throwing on empty gets, instead preferring to return nil.
  This function adopts the Clojure behavior, choosing to treat this as a call to `getOrNull`.

  *Arguments*:

    * `o`: an Option

  *Returns*:

    the Option's value if non-empty, nil otherwise"
  (when-not (empty? o) (.get o)))

