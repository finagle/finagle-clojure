# lein-finagle-clojure

A lein plugin for compiling Thrift definitions using Scrooge.
Generates Java classes that can be used with Finagle & finagle-clojure.


## Usage

* Add `[lein-finagle-clojure "0.1.0-SNAPSHOT"]` to `:plugins` in your project.clj
    * this will add a hook around `lein-javac` that will compile your Thrift definitions first.
* Run manually: `lein finagle-clojure scrooge`

## Configuration

    :finagle-clojure {:thrift-source-path "source-path" :thrift-output-path "output-path"}
