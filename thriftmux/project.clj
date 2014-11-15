(defproject finagle-clojure/thriftmux "0.1.2-SNAPSHOT"
  :description "A light wrapper around finagle-thriftmux for Clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "http://github.com/finagle/finagle-clojure"}
  :plugins [[lein-midje "3.1.3"]
            [lein-finagle-clojure "0.1.2-SNAPSHOT" :hooks false]]
  :profiles {:test {:dependencies [[midje "1.6.3" :exclusions [org.clojure/clojure]]]}
             :dev [:test {:dependencies [[org.clojure/clojure "1.6.0"]]}]
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}
  :finagle-clojure {:thrift-source-path "test/resources" :thrift-output-path "test/java"}
  :java-source-paths ["test/java"]
  :jar-exclusions [#"test"]
  :test-paths ["test/clj/"]
  ;; TODO there's no checksum for libthrift-0.5.0.pom, set checksum to warn for now
  :repositories [["twitter" {:url "http://maven.twttr.com/" :checksum :warn}]]
  ;; the dependency on finagle-clojure/core is required for tests
  ;; but also to require fewer dependencies in projects that use thrift.
  ;; this is akin to Finagle itself, where depending on finagle-thriftmux
  ;; pulls in finagle-core as well.
  :dependencies [[finagle-clojure/core "0.1.2-SNAPSHOT"]
                 [com.twitter/finagle-thriftmux_2.10 "6.18.0"]
                 [org.apache.thrift/libthrift "0.5.0-1"]])
