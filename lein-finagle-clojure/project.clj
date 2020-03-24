(defproject lein-finagle-clojure "0.8.0-NUBANK"
  :description "A lein plugin for working with finagle-clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "https://github.com/finagle/finagle-clojure"}
  :min-lein-version "2.0.0"
  :plugins [[s3-wagon-private "1.3.1"]
            [lein-modules "0.3.11"]]
  :repositories [["nu-maven" {:url "s3p://nu-maven/releases/"}]
                 ["sonatype" "https://oss.sonatype.org/content/groups/public/"]
                 ["twitter" {:url "https://maven.twttr.com/" :checksum :warn}]]
  :deploy-repositories [["releases" {:url "s3p://nu-maven/releases/" :no-auth true}]]
  :dependencies [[com.twitter/scrooge-generator_2.11 "19.12.0"]
                 [com.twitter/scrooge-linter_2.11 "19.12.0"]]
  :eval-in-leiningen true)
