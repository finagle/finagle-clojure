(defproject finagle-clojure/thriftmux "0.3.0"
  :description "A light wrapper around finagle-thriftmux for Clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "http://github.com/finagle/finagle-clojure"}
  :plugins [[lein-midje "3.1.3"]
            [lein-finagle-clojure "0.3.0" :hooks false]]
  :profiles {:test {:dependencies [[midje "1.6.3" :exclusions [org.clojure/clojure]]]}
             :dev [:test {:dependencies [[org.clojure/clojure "1.6.0"]]}]
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0-alpha5"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}
  :finagle-clojure {:thrift-source-path "test/resources" :thrift-output-path "test/java"}
  :java-source-paths ["test/java"]
  :jar-exclusions [#"test"]
  :test-paths ["test/clj/"]
  :repositories [["twitter" {:url "http://maven.twttr.com/"}]]
  :dependencies [[finagle-clojure/thrift "0.3.0"]
                 [com.twitter/finagle-thriftmux_2.10 "6.24.0"]])
