(defproject lein-finagle-clojure "0.4.1"
  :description "A lein plugin for working with finagle-clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "https://github.com/finagle/finagle-clojure"}
  :min-lein-version "2.0.0"
  :repositories [["sonatype" "https://oss.sonatype.org/content/groups/public/"]
                 ["twitter" {:url "https://maven.twttr.com/" :checksum :warn}]]
  :dependencies [[com.twitter/scrooge-generator_2.11 "3.20.0"]
                 [com.twitter/scrooge-linter_2.11 "3.20.0"]]
  :eval-in-leiningen true)
