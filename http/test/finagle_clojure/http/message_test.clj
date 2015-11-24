(ns finagle-clojure.http.message-test
  (:require [finagle-clojure.http.message :refer :all]
            [midje.sweet :refer :all]))

(fact "response status"
  (-> (response) (set-status-code 200) (status-code))
  => 200

  (-> (response 200) (status-code))
  => 200)

(fact "request method"
  (-> (request "/") (set-http-method :get) (http-method))
  => "GET"

  (-> (request "/" :get) (http-method))
  => "GET")

(fact "content string"
  (-> (response) (set-content-string "test") (content-string))
  => "test"

  (-> (request "/") (set-content-string "test") (content-string))
  => "test")

(fact "content type and charset"
  (-> (response) (content-type))
  => nil

  (-> (response) (set-content-type "application/json") (content-type))
  => "application/json;charset=utf-8"

  (-> (response) (set-content-type "application/json" "us-ascii") (content-type))
  => "application/json;charset=us-ascii"

  (-> (request "/") (set-charset "utf-8") (set-content-type "application/json") (charset))
  => "utf-8")

(fact "headers"
  (-> (response) (headers))
  => {}

  (-> (response) (set-header "X-Test" "test") (header "X-Test"))
  => "test"

  (-> (response) (set-header "X-Test" "test") (headers))
  => {"X-Test" "test"})

(fact "params"
  (-> (request "/foo") (params))
  => {}

  (-> (request "/foo?bar=baz") (params))
  => {"bar" "baz"}

  (-> (request "/foo?bar=baz") (param "bar"))
  => "baz"

  (-> (request "/foo?bar=baz") (param "quux"))
  => nil)
