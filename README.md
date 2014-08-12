# finagle-clojure

A thin wrapper around Finagle & Twitter Future

## Building

    lein sub install
    lein sub -s "lein-finagle-clojure:finagle-clojure-template" install

## Create a new project

    lein new finagle-clojure project-name

## Libraries

* `core`: convenience fns for interacting with Futures.
* `thrift`: create Thrift clients & servers.

## Documentation

* [Overview](doc/intro.md)
* [API Docs](doc/codox/index.html)
  * run `lein doc` to generate
