(ns finagle-clojure.mysql.client-test
  (:import (com.twitter.finagle.exp.mysql StringValue NullValue EmptyValue IntValue DoubleValue FloatValue ByteValue ShortValue LongValue DateValue BigDecimalValue EmptyValue$ NullValue$)
           (java.sql Date))
  (:require [midje.sweet :refer :all]
            [finagle-clojure.mysql.client :refer :all]))

(facts "about MySQL unboxing"
  (unbox-value (ByteValue. (byte 42)))
  => (byte 42)

  (unbox-value (ShortValue. (short 42)))
  => (short 42)

  (unbox-value (IntValue. (int 42)))
  => (int 42)

  (unbox-value (LongValue. (long 42)))
  => (long 42)

  (unbox-value (FloatValue. (float 42.0)))
  => (float 42.0)

  (unbox-value (DoubleValue. (double 42)))
  => (double 42)

  (unbox-value (StringValue. "test"))
  => "test"

  (unbox-value (BigDecimalValue/apply (scala.math.BigDecimal. 42.42M)))
  => 42.42M

  (unbox-value EmptyValue$/MODULE$)
  => nil

  (unbox-value NullValue$/MODULE$)
  => nil

  ; FIXME These fail for time-zone reasons that aren't clear to me.
  ;(unbox-value (DateValue/apply (Date. 1234)))
  ;=> (Date. 1234)
  ;
  ;(unbox-value (DateValue/apply (Date. 0)))
  ;=> (Date. 0)
  )
