(ns leiningen.new.finagle-clojure
  (:require [leiningen.new.templates :refer [renderer name-to-path ->files project-name sanitize sanitize-ns *dir*]]
            [leiningen.core.main :as main]
            [clojure.java.io :as io]
            [camel-snake-kebab.core :refer [->CamelCase]]))

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

(defn core-files
  [name]
  (let [module-name (module-name name "core")
        java-source-paths ["src/java"]
        ;; TODO add comment explaining why checksum warn
        repositories (str '[["twitter" {:url "http://maven.twttr.com/" :checksum :warn}]])
        dependencies '[{:dependency [finagle-clojure/thrift "0.1.0"]}
                       {:dependency [com.twitter/scrooge-core_2.10 "3.16.3"]}]
        data {:name name
              :project-name (str name "-core")
              :module-name module-name
              :misc-config [{:key :plugins :value '[[lein-finagle-clojure "0.1.0-SNAPSHOT"]]}
                            {:key :java-source-paths :value java-source-paths}
                            {:key :repositories :value repositories}
                            {:key :finagle-clojure :value {:thrift-source-path "src/thrift" :thrift-output-path "src/java"}}]
              :dependencies dependencies
              :service-name (service-name name)
              :thrift-ns (thrift-namespace name)
              :description (str "Core data types & service definition for " name)
              :sanitized (name-to-path module-name)}]
    (main/info "Generating" module-name)
    (->files data
             "{{module-name}}/src/java"
             ["{{module-name}}/README.md" (render "README.md-core" data)]
             ["{{module-name}}/project.clj" (render "project.clj" data)]
             ["{{module-name}}/src/thrift/schema.thrift" (render "schema.thrift" data)])))

(defn client-files
  [name]
  (let [module-name (module-name name "client")
        core-dependency (symbol (str name "-core"))
        dependencies `[{:dependency [~core-dependency "0.1.0-SNAPSHOT"]}
                       {:dependency [finagle-clojure/thrift "0.1.0"]}]
        data {:name name
              :project-name (str name "-client")
              :module-name module-name
              :misc-config [{:key :profiles :value '{:dev {:dependencies [[org.clojure/clojure "1.6.0"]]}}}]
              :dependencies dependencies
              :service-name (service-name name)
              :thrift-ns (thrift-namespace name)
              :description (str "Thrift client for " name)
              :ns (str (sanitize-ns name) ".client")
              :sanitized (name-to-path name)}]
    (main/info "Generating" module-name)
    (->files data
             ["{{module-name}}/README.md" (render "README.md-client" data)]
             ["{{module-name}}/test/{{sanitized}}/client_test.clj" (render "test.clj" data)]
             ["{{module-name}}/project.clj" (render "project.clj" data)]
             ["{{module-name}}/src/{{sanitized}}/client.clj" (render "client.clj" data)])))

(defn service-files
  [name]
  (let [module-name (module-name name "service")
        core-dependency (symbol (str name "-core"))
        dependencies `[{:dependency [org.clojure/clojure "1.6.0"]}
                       {:dependency [~core-dependency "0.1.0-SNAPSHOT"]}
                       {:dependency [finagle-clojure/thrift "0.1.0"]}]
        service-ns (str (sanitize-ns name) ".service")
        data {:name name
              :project-name (str name "-service")
              :module-name module-name
              :misc-config [{:key :main :value service-ns}]
              :dependencies dependencies
              :service-name (service-name name)
              :thrift-ns (thrift-namespace name)
              :description (str "Thrift service implementation for " name)
              :ns service-ns 
              :sanitized (name-to-path name)}]
    (main/info "Generating" module-name)
    (->files data
             ["{{module-name}}/README.md" (render "README.md-service" data)]
             ["{{module-name}}/test/{{sanitized}}/service_test.clj" (render "test.clj" data)]
             ["{{module-name}}/project.clj" (render "project.clj" data)]
             ["{{module-name}}/src/{{sanitized}}/service.clj" (render "service.clj" data)])))

(defn finagle-clojure
  "Create a new finagle-clojure project using Thrift."
  [name]
  (main/info "Generating fresh 'lein new' finagle-clojure thrift project.")
  (let [dir (or *dir*
                (-> (System/getProperty "leiningen.original.pwd")
                    (io/file (project-name name)) (.getPath)))
        main-project-data {:project-name name 
                           :description (str "meta-project for " name ". Run lein sub install to build all modules")
                           :misc-config [{:key :plugins :value '[[lein-sub "0.3.0"]]} 
                                         {:key :sub :value (mapv (partial module-name name) ["core" "service" "client"])}]}]
    (if (or *dir* (.mkdir (io/file dir)))
      (binding [*dir* dir]
        (core-files name)
        (client-files name)
        (service-files name)
        (->files {} 
                 ["project.clj" (render "project.clj" main-project-data)]
                 [".gitignore" (render "gitignore" {})]
                 ["README.md" (render "README.md" {:name (name-from-package name)})]))
      (main/info (str "Could not create directory " dir ". Maybe it already exists?")))))
