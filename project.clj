(defproject crashlytics/finagle-clojure "0.1.0"
  :description "A light wrapper around Finagle for Clojure"
  :url "https://github.com/crashlytics/finagle-clojure"
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.6.0"]
                                  [midje "1.5.1" :exclusions [org.clojure/clojure]]]
                   :plugins [[lein-midje "3.1.1"]]}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}}
  :repositories [["twitter" "http://maven.twttr.com/"]]
  :dependencies [[com.twitter/finagle-core "6.6.2"]])
