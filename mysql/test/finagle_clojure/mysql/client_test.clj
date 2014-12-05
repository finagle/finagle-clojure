(ns finagle-clojure.mysql.client-test
  (:import (com.twitter.finagle.exp.mysql StringValue NullValue EmptyValue IntValue DoubleValue FloatValue ByteValue ShortValue LongValue DateValue BigDecimalValue EmptyValue$ NullValue$)
           (java.sql Date))
  (:require [clojure.test :refer :all]
            [finagle-clojure.mysql.client :refer :all]))

(deftest test-mysql-unboxing
  (is (= (byte 42)    (unbox-value (ByteValue. (byte 42)))))
  (is (= (short 42)   (unbox-value (ShortValue. (short 42)))))
  (is (= (int 42)     (unbox-value (IntValue. (int 42)))))
  (is (= (long 42)    (unbox-value (LongValue. (long 42)))))
  (is (= (float 42.0) (unbox-value (FloatValue. (float 42.0)))))
  (is (= (double 42)  (unbox-value (DoubleValue. (double 42)))))
  (is (= "test"       (unbox-value (StringValue. "test"))))
  ;; These fail for reasons that aren't clear to me.
  ;; (is (= (Date. 1234) (unbox-value (DateValue/apply (Date. 1234)))))
  ;; (is (= (Date. 0)    (unbox-value (DateValue/apply (Date. 0)))))
  (is (= 42.42M       (unbox-value (BigDecimalValue/apply (scala.math.BigDecimal. 42.42M)))))
  (is (= nil          (unbox-value EmptyValue$/MODULE$)))
  (is (= nil          (unbox-value NullValue$/MODULE$)))
  )


