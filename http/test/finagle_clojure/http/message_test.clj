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

(fact "content type"
  (-> (response) (content-type))
  => nil

  (-> (response) (set-content-type "application/json") (content-type))
  => "application/json;charset=utf-8"

  (-> (response) (set-content-type "application/json" "us-ascii") (content-type))
  => "application/json;charset=us-ascii")

