# finagle-clojure [![Build Status](https://travis-ci.org/finagle/finagle-clojure.svg?branch=master)](https://travis-ci.org/finagle/finagle-clojure)

A thin wrapper around Finagle & Twitter Future.
This library assumes you are familiar with Finagle.
If not, check out its [docs](https://twitter.github.io/finagle/guide/).

## Building

    lein sub -s "lein-finagle-clojure:finagle-clojure-template:core:thrift" install

## Running Tests

    lein sub midje


## Libraries

The readmes in each sub-library have more information.

* `core`: convenience fns for interacting with Futures.
* `thrift`: create Thrift clients & servers.
* `lein-finagle-clojure`: a lein plugin for automatically compiling Thrift definitions using [Scrooge](https://twitter.github.io/scrooge/index.html).
* `finagle-clojure-template`: a lein template for creating new projects using finagle-clojure & Thrift.

## Create a new project with Thrift

    lein new finagle-clojure $PROJECT-NAME

Then check out the readmes in the generated project.


## Documentation

* [API Docs](doc/codox/index.html)
  * run `lein doc` from this directory to generate
* [Overview](doc/intro.md)
