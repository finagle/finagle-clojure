(defproject finagle-clojure/http "0.8.0-NUBANK"
  :description "A light wrapper around Finagle HTTP for Clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url  "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "https://github.com/finagle/finagle-clojure"}
  :plugins [[s3-wagon-private "1.3.1"]
            [lein-midje "3.2"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.0"]
                                  [midje "1.9.9" :exclusions [org.clojure/clojure]]]}}
  :repositories [["nu-maven" {:url "s3p://nu-maven/releases/"}]]
  :deploy-repositories [["releases" {:url "s3p://nu-maven/releases/" :no-auth true}]]
  :dependencies [[finagle-clojure/core "0.8.0-SNAPSHOT"]
                 [com.twitter/finagle-http_2.11 "19.12.0"]
                 [com.twitter/finagle-stats_2.11 "19.12.0"]])
