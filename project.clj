(defproject finagle-clojure "0.10.0"
  :description "A light wrapper around Finagle for Clojure"
  :url "https://github.com/twitter/finagle-clojure"
  :license {:name "Apache License, Version 2.0"
            :url  "https://www.apache.org/licenses/LICENSE-2.0"}
  :scm {:name "git" :url "https://github.com/nubank/finagle-clojure"}
  :plugins [[lein-modules "0.3.11"]
            [codox "0.8.10"]]
  :profiles {:inherited  {:repositories [["nu-maven" {:url "s3p://nu-maven/releases/"}]
                                        ["sonatype" "https://oss.sonatype.org/content/groups/public/"]
                                        ["twitter" {:url "https://maven.twttr.com/" :checksum :warn}]]
                          :deploy-repositories [["releases" {:url "s3p://nu-maven/releases/" :no-auth true}]]
                          :aliases      {"bump-release"  ["do" ["change" "version" "leiningen.release/bump-version" "release"]
                                                               ["change" "version" "str" "\"-NUBANK\""]]}}
             :unit       {:modules {:dirs ^:replace ["core" "thrift" "http"]}}
             :ci         {:release-tasks [["vcs" "assert-committed"]
                                          ["bump-release"]
                                          ["modules" "bump-release"]
                                          ["vcs" "commit"]
                                          ["vcs" "tag"]
                                          ["modules" "deploy"]
                                          ["change" "version" "leiningen.release/bump-version" "patch"]
                                          ["modules" "change" "version" "leiningen.release/bump-version" "patch"]
                                          ["vcs" "commit"]
                                          ["vcs" "push"]]}}
  :modules {:subprocess nil
            :dirs       ["lein-finagle-clojure" "core" "thrift" "http"]
            :versions   {org.clojure/clojure    "1.10.0"
                         finagle-clojure        :version}}
  :codox {:sources                   ["core/src" "thrift/src" "http/src"]
          :defaults                  {:doc/format :markdown}
          :output-dir                "doc/codox"
          :src-dir-uri               "https://github.com/finagle/finagle-clojure/blob/master/"
          :src-linenum-anchor-prefix "L"})
