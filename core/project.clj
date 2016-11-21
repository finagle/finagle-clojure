(defproject finagle-clojure/core "0.7.0"
  :description "A light wrapper around Finagle & Twitter Util for Clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "http://github.com/finagle/finagle-clojure"}
  :plugins [[lein-midje "3.2"]]
  :profiles {:test {:dependencies [[midje "1.8.3" :exclusions [org.clojure/clojure]]
                                   [criterium "0.4.4"]]}
             :dev [:test {:dependencies [[org.clojure/clojure "1.8.0"]]}]
             :1.7 [:test {:dependencies [[org.clojure/clojure "1.7.0"]]}]
             :1.6 [:test {:dependencies [[org.clojure/clojure "1.6.0"]]}]
             :1.5 [:test {:dependencies [[org.clojure/clojure "1.5.1"]]}]}
  :dependencies [[com.twitter/finagle-core_2.11 "6.39.0"]
                 [org.clojure/algo.monads "0.1.6"]])
