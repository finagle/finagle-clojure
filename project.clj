(defproject finagle-clojure "0.2.1-SNAPSHOT"
  :description "A light wrapper around Finagle for Clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "http://github.com/finagle/finagle-clojure"}
  :dependencies [[finagle-clojure/core "0.2.1-SNAPSHOT"]
                 [finagle-clojure/thrift "0.2.1-SNAPSHOT"]
                 [finagle-clojure/thriftmux "0.2.1-SNAPSHOT"]
                 [finagle-clojure/http "0.2.1-SNAPSHOT"]]
  :plugins [[lein-sub "0.3.0"]
            [codox "0.8.10"]
            [lein-midje "3.1.3"]]
  :sub ["core" "thrift" "http"]
  :codox {:sources ["core/src" "thrift/src" "thriftmux/src" "http/src"]
          :defaults {:doc/format :markdown}
          :output-dir "doc/codox"
          :src-dir-uri "https://github.com/finagle/finagle-clojure/blob/master/"
          :src-linenum-anchor-prefix "L"})
