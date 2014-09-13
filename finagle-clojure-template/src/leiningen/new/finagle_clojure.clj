(ns leiningen.new.finagle-clojure
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files project-name sanitize sanitize-ns]]
            [leiningen.core.main :as main]
            [camel-snake-kebab.core :refer [->CamelCase]]))

(def finagle-clojure-version "0.1.1")

(def render (renderer "finagle-clojure"))

(defn thrift-namespace
  [name]
  (-> name
      sanitize
      (clojure.string/replace "/" ".")
      (str ".thrift")))

(defn name-from-package
  [name]
  (-> name
      project-name
      (clojure.string/split #"\.")
      last))

(defn service-name
  [name]
  (-> name
      name-from-package
      ->CamelCase))

(defn module-name
  [name suffix]
  (-> name
      name-from-package
      (str "-" suffix)))

(defn core-data
  [name]
  (let [module-name (module-name name "core")
        java-source-paths ["src/java"]
        dependencies `[{:dependency [finagle-clojure/thrift ~finagle-clojure-version]}
                       {:dependency [com.twitter/scrooge-core_2.10 "3.16.3"]}]]
    {:name name
     :project-name (str name "-core")
     :module-name module-name
     :misc-config [{:key :plugins :value [['lein-finagle-clojure finagle-clojure-version]]}
                   {:key :java-source-paths :value java-source-paths}
                   {:key :finagle-clojure :value {:thrift-source-path "src/thrift" :thrift-output-path "src/java"}}
                   {:key :profiles :value '{:dev {:dependencies [[org.clojure/clojure "1.6.0"]]}}}]
     :dependencies dependencies
     :service-name (service-name name)
     :thrift-ns (thrift-namespace name)
     :description (str "Core data types & service definition for " name)
     :sanitized (name-to-path module-name)}))

(defn client-data
  [name]
  (let [module-name (module-name name "client")
        core-dependency (symbol (str name "-core"))
        dependencies `[{:dependency [~core-dependency "0.1.0-SNAPSHOT"]}
                       {:dependency [finagle-clojure/thrift ~finagle-clojure-version]}]]
    {:name name
     :project-name (str name "-client")
     :module-name module-name
     :misc-config [{:key :profiles :value '{:dev {:dependencies [[org.clojure/clojure "1.6.0"]]}}}]
     :dependencies dependencies
     :service-name (service-name name)
     :thrift-ns (thrift-namespace name)
     :description (str "Thrift client for " name)
     :ns (str (sanitize-ns name) ".client")
     :sanitized (name-to-path name)}))

(defn service-data
  [name]
  (let [module-name (module-name name "service")
        core-dependency (symbol (str name "-core"))
        dependencies `[{:dependency [org.clojure/clojure "1.6.0"]}
                       {:dependency [~core-dependency "0.1.0-SNAPSHOT"]}
                       {:dependency [finagle-clojure/thrift ~finagle-clojure-version]}]
        service-ns (str (sanitize-ns name) ".service")]
    {:name name
     :project-name (str name "-service")
     :module-name module-name
     :misc-config [{:key :main :value service-ns}]
     :dependencies dependencies
     :service-name (service-name name)
     :thrift-ns (thrift-namespace name)
     :description (str "Thrift service implementation for " name)
     :ns service-ns
     :sanitized (name-to-path name)}))

(defn finagle-clojure
  "Create a new finagle-clojure project using Thrift."
  [name]
  (main/info "Generating fresh 'lein new' finagle-clojure thrift project.")
  (let [main-project-data {:project-name name
                           :description (str "meta-project for " name ". Run lein sub install to build all modules")
                           :misc-config [{:key :plugins :value '[[lein-sub "0.3.0"]]}
                                         {:key :sub :value (mapv (partial module-name name) ["core" "service" "client"])}]}
        core-data (core-data name)
        core-module-name (module-name name "core")
        client-data (client-data name)
        client-module-name (module-name name "client")
        service-data (service-data name)
        service-module-name (module-name name "service")
        data {:name name
              :core-module-name core-module-name
              :client-module-name client-module-name
              :service-module-name service-module-name
              :sanitized (name-to-path name)}]
    (->files data
             ;; root files
             ["project.clj" (render "project.clj" main-project-data)]
             [".gitignore" (render "gitignore" {})]
             ["README.md" (render "README.md" {:name (name-from-package name)})]
             ;; core files
             "{{core-module-name}}/src/java"
             ["{{core-module-name}}/README.md" (render "README.md-core" core-data)]
             ["{{core-module-name}}/project.clj" (render "project.clj" core-data)]
             ["{{core-module-name}}/src/thrift/schema.thrift" (render "schema.thrift" core-data)]
             ;; client files
             ["{{client-module-name}}/README.md" (render "README.md-client" client-data)]
             ["{{client-module-name}}/test/{{sanitized}}/client_test.clj" (render "test.clj" client-data)]
             ["{{client-module-name}}/project.clj" (render "project.clj" client-data)]
             ["{{client-module-name}}/src/{{sanitized}}/client.clj" (render "client.clj" client-data)]
             ;; service files
             ["{{service-module-name}}/README.md" (render "README.md-service" service-data)]
             ["{{service-module-name}}/test/{{sanitized}}/service_test.clj" (render "test.clj" service-data)]
             ["{{service-module-name}}/project.clj" (render "project.clj" service-data)]
             ["{{service-module-name}}/src/{{sanitized}}/service.clj" (render "service.clj" service-data)])))
