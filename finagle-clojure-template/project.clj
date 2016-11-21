(defproject finagle-clojure/lein-template "0.7.1-SNAPSHOT"
  :description "A lein template for creating a new finagle-clojure Thrift project."
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "http://github.com/finagle/finagle-clojure"}
  :min-lein-version "2.0.0"
  :dependencies [[camel-snake-kebab "0.4.0" :exclusions [org.clojure/clojure]]]
  :eval-in-leiningen true)
