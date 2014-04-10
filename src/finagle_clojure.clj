(ns finagle-clojure
  (:import [com.twitter.finagle Service]))

(defmacro make-service
  [apply-arg-binding & body]
  `(proxy [Service] []
    (apply ~apply-arg-binding
      ~@body)))
