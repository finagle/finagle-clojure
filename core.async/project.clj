(defproject finagle-clojure/core.async "0.4.2-SNAPSHOT"
  :description "Clojure core.async integration with finagle-clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "http://github.com/finagle/finagle-clojure"}
  :plugins [[lein-midje "3.2"]]
  :profiles {:test {:dependencies [[midje "1.8.3" :exclusions [org.clojure/clojure]]]}
             :dev [:test {:dependencies [[org.clojure/clojure "1.8.0"]]}]
             :1.7 [:test {:dependencies [[org.clojure/clojure "1.7.0"]]}]
             :1.6 [:test {:dependencies [[org.clojure/clojure "1.6.0"]]}]
             :1.5 [:test {:dependencies [[org.clojure/clojure "1.5.1"]]}]
             :1.4 [:test {:dependencies [[org.clojure/clojure "1.4.0"]]}]}
  :dependencies [[finagle-clojure/core "0.4.2-SNAPSHOT"]
                 [org.clojure/core.async "0.2.374"]])
