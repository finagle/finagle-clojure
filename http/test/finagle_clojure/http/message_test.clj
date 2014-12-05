(ns finagle-clojure.http.message-test
  (:require [finagle-clojure.http.message :refer :all]
            [midje.sweet :refer :all]))

(fact "response status"
  (-> (->Response) (set-status-code 200) (status-code))
  => 200

  (-> (->Response 200) (status-code))
  => 200)

(fact "request method"
  (-> (->Request "/") (set-http-method :get) (http-method))
  => "GET"

  (-> (->Request "/" :get) (http-method))
  => "GET")

(fact "content string"
  (-> (->Response) (set-content-string "test") (content-string))
  => "test"

  (-> (->Request "/") (set-content-string "test") (content-string))
  => "test")

(fact "content type"
  (-> (->Response) (content-type))
  => nil

  (-> (->Response) (set-content-type "application/json") (content-type))
  => "application/json;charset=utf-8"

  (-> (->Response) (set-content-type "application/json" "us-ascii") (content-type))
  => "application/json;charset=us-ascii")

