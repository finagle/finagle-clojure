# lein-finagle-clojure

A lein plugin for compiling Thrift definitions using Scrooge.
Generates Java classes that can be used with Finagle & finagle-clojure.


## Usage

* Add `[lein-finagle-clojure "0.1.0"]` to `:plugins` in your project.clj
    * this will add a hook around `lein-javac` that will compile your Thrift definitions first.
    * to not add the hook: `[lein-finagle-clojure "0.1.0" :hooks false]`
* Run manually: `lein finagle-clojure scrooge`

## Configuration

The following must be added to the `project.clj` of a projec that wants to use this plugin.
The plugin will not compile any Thrift files if this config is missing.

    :finagle-clojure {:thrift-source-path "source-path" :thrift-output-path "output-path"}
