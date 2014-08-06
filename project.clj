(defproject finagle-clojure "0.1.0"
  :description "A light wrapper around Finagle for Clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[finagle-clojure/core "0.1.0"]
                [finagle-clojure/thrift "0.1.0"]]
  :plugins [[lein-sub "0.3.0"]
            [codox "0.8.10"]
            [lein-midje "3.1.3"]]
  :sub ["core" "thrift"]
  :codox {:sources ["core/src"]
          :defaults {:doc/format :markdown}
          :output-dir "doc/codox"
          :src-dir-uri "http://github.com/samn/finagle-clojure/blob/master/"
          :src-linenum-anchor-prefix "L"})
