(defproject lein-finagle-clojure "0.2.1-SNAPSHOT"
  :description "A lein plugin for working with finagle-clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "http://github.com/finagle/finagle-clojure"}
  :min-lein-version "2.0.0"
  :repositories [["sonatype" "https://oss.sonatype.org/content/groups/public/"]]
  :dependencies [[com.twitter/scrooge-generator_2.10 "3.17.0"]]
  :eval-in-leiningen true)
