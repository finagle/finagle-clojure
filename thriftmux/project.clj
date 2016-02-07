(defproject finagle-clojure/thriftmux "0.4.2-SNAPSHOT"
  :description "A light wrapper around finagle-thriftmux for Clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "http://github.com/finagle/finagle-clojure"}
  :plugins [[lein-midje "3.1.3"]
            [lein-finagle-clojure "0.4.2-SNAPSHOT" :hooks false]]
  :profiles {:test {:dependencies [[midje "1.7.0" :exclusions [org.clojure/clojure]]]}
             :dev [:test {:dependencies [[org.clojure/clojure "1.8.0"]]}]
             :1.7 [:test {:dependencies [[org.clojure/clojure "1.7.0"]]}]
             :1.6 [:test {:dependencies [[org.clojure/clojure "1.6.0"]]}]
             :1.5 [:test {:dependencies [[org.clojure/clojure "1.5.1"]]}]
             :1.4 [:test {:dependencies [[org.clojure/clojure "1.4.0"]]}]}
  :finagle-clojure {:thrift-source-path "test/resources" :thrift-output-path "test/java"}
  :java-source-paths ["test/java"]
  :jar-exclusions [#"test"]
  :test-paths ["test/clj/"]
  :repositories [["twitter" {:url "https://maven.twttr.com/"}]]
  :dependencies [[finagle-clojure/thrift "0.4.2-SNAPSHOT"]
                 [com.twitter/finagle-thriftmux_2.11 "6.33.0"]])
