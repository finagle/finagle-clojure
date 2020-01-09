(defproject finagle-clojure/thrift "0.8.0-SNAPSHOT"
  :description "A light wrapper around finagle-thrift for Clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "https://github.com/finagle/finagle-clojure"}
  :plugins [[s3-wagon-private "1.3.1"]
            [lein-midje "3.2"]]
  :profiles {:test {:dependencies [[midje "1.8.3" :exclusions [org.clojure/clojure]]]
                    :resource-paths ["test/resources"]}
             :dev {:test {:dependencies [[org.clojure/clojure "1.8.0"]]}
                   :plugins [[lein-finagle-clojure "0.8.0-SNAPSHOT"]]}
             :1.7 [:test {:dependencies [[org.clojure/clojure "1.7.0"]]}]
             :1.6 [:test {:dependencies [[org.clojure/clojure "1.6.0"]]}]
             :1.5 [:test {:dependencies [[org.clojure/clojure "1.5.1"]]}]}
  :finagle-clojure {:thrift-source-path "test/resources" :thrift-output-path "test/java"}
  :java-source-paths ["test/java"]
  :jar-exclusions [#"test"]
  :test-paths ["test/clj/"]
  ;; TODO there's no checksum for libthrift-0.5.0.pom, set checksum to warn for now
  :repositories [["nu-maven" {:url "s3p://nu-maven/releases/"}]
                 ["twitter" {:url "https://maven.twttr.com/" :checksum :warn}]]
  :deploy-repositories [["releases" {:url "s3p://nu-maven/releases/" :no-auth true}]]
  ;; the dependency on finagle-clojure/core is required for tests
  ;; but also to require fewer dependencies in projects that use thrift.
  ;; this is akin to Finagle itself, where depending on finagle-thrift
  ;; pulls in finagle-core as well.
  :dependencies [[finagle-clojure/core "0.8.0-SNAPSHOT"]
                 [com.twitter/finagle-thrift_2.11 "19.12.0"]
                 [org.apache.thrift/libthrift "0.10.0"]
                 [org.apache.tomcat/tomcat-jni "8.5.0"]])
