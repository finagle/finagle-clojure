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

* [Quick Start with finagle-clojure & Thrift](doc/quick-start.md)
* [API Docs](https://finagle.github.io/finagle-clojure/)
  * run `lein doc` from this directory to generate
* Finagle Docs
  * [User's Guide](https://twitter.github.io/finagle/guide/)
  * [API Docs (scala)](https://twitter.github.io/finagle/docs/#com.twitter.finagle.package)
  * [GitHub](https://github.com/twitter/finagle)
