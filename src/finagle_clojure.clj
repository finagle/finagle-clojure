(ns finagle-clojure
  (:import [com.twitter.finagle Service]))

;; TODO: @samn 06/11/14 is this actually useful?
;;  thrift services should use serviceIface?
(defmacro ^:no-doc make-service
  [apply-arg-binding & body]
  `(proxy [Service] []
    (apply ~apply-arg-binding
      ~@body)))
