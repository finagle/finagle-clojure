(defproject finagle-clojure/mysql "0.6.1-SNAPSHOT"
  :description "A light wrapper around Finagle MySQL support"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "https://github.com/finagle/finagle-clojure"}
  :plugins [[lein-midje "3.2"]]
  :profiles {:test {:dependencies [[midje "1.8.3" :exclusions [org.clojure/clojure]]]}
             :dev [:test {:dependencies [[org.clojure/clojure "1.8.0"]]}]
             :1.7 [:test {:dependencies [[org.clojure/clojure "1.7.0"]]}]
             :1.6 [:test {:dependencies [[org.clojure/clojure "1.6.0"]]}]
             :1.5 [:test {:dependencies [[org.clojure/clojure "1.5.1"]]}]}
  :dependencies [[finagle-clojure/core "0.6.1-SNAPSHOT"]
                 [com.twitter/finagle-mysql_2.11 "6.35.0"]])
