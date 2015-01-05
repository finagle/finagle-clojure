(ns finagle-clojure.mysql.value-test
  (:import (com.twitter.finagle.exp.mysql LongValue FloatValue IntValue ShortValue ByteValue DoubleValue StringValue
                                          BigDecimalValue EmptyValue$ NullValue$ DateValue))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.mysql.value :refer :all]
            [finagle-clojure.options :as opt]))

(facts "MySQL client"
  (facts "when unboxing values"
    (unbox (ByteValue. (byte 42)))
    => (byte 42)

    (unbox (ShortValue. (short 42)))
    => (short 42)

    (unbox (IntValue. (int 42)))
    => (int 42)

    (unbox (LongValue. (long 42)))
    => (long 42)

    (unbox (FloatValue. (float 42.0)))
    => (float 42.0)

    (unbox (DoubleValue. (double 42)))
    => (double 42)

    (unbox (StringValue. "test"))
    => "test"

    (unbox (BigDecimalValue/apply (scala.math.BigDecimal. 42.42M)))
    => 42.42M

    (unbox EmptyValue$/MODULE$)
    => nil

    (unbox NullValue$/MODULE$)
    => nil

    (let [dv (DateValue/apply (java.sql.Date/valueOf "2014-12-23"))]
      (-> dv (unbox) (.getYear))
      => 114

      (-> dv (unbox) (.getMonth))
      => 11

      (-> dv (unbox) (.getDate))
      => 23)
    )

  (facts "when boxing values"
    (box (byte 42))
    => (ByteValue. (byte 42))

    (box (short 42))
    => (ShortValue. (short 42))

    (box (int 42))
    => (IntValue. (int 42))

    (box (long 42))
    => (LongValue. (long 42))

    (box (double 42))
    => (DoubleValue. (double 42))

    (box "test")
    => (StringValue. "test")

    42.42M
    => (-> (box 42.42M)
           (BigDecimalValue/unapply)
           (opt/get)
           (.underlying))

    (box nil)
    => NullValue$/MODULE$

    114
    => (-> (box (java.sql.Date/valueOf "2014-12-23"))
           (DateValue/unapply)
           (opt/get)
           (.getYear)))
  )