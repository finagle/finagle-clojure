(defproject finagle-clojure "0.1.0"
  :description "A light wrapper around Finagle for Clojure"
  :url "https://github.com/crashlytics/finagle-clojure"
  :dependencies [[finagle-clojure/core "0.1.0"]]
  :plugins [[lein-sub "0.3.0"]
            [codox "0.8.10"]
            [lein-midje "3.1.1"]]
  :sub ["core"]
  :codox {:sources ["core/src"]
          :defaults {:doc/format :markdown}
          :output-dir "doc/codox"
          :src-dir-uri "http://github.com/samn/finagle-clojure/blob/master/"
          :src-linenum-anchor-prefix "L"})
