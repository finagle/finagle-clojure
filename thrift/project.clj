(defproject finagle-clojure/thrift "0.1.0"
  :description "A light wrapper around finagle-thrift for Clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :plugins [[lein-midje "3.1.3"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]
                                  [midje "1.6.3" :exclusions [org.clojure/clojure]]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}
  :test-paths ["test/clj/"]
  :java-source-paths ["test/java"]
  :jar-exclusions [#"test"]
  :repositories [["twitter" {:url "http://maven.twttr.com/" :checksum :warn}]]
  ;; the dependency on finagle-clojure/core is required for tests
  ;; but also to require fewer dependencies in projects that use thrift.
  ;; this is akin to Finagle itself, where depending on finagle-thrift
  ;; pulls in finagle-core as well.
  :dependencies [[finagle-clojure/core "0.1.0"]
                 [com.twitter/finagle-thrift_2.10 "6.18.0"]
                 [org.apache.thrift/libthrift "0.5.0-1"]])
