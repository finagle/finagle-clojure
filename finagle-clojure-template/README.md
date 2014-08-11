# finagle-clojure template

A Leiningen template for services with finagle-clojure using Thrift.

Create a new finagle-clojure project like this:

    lein new finagle-clojure project-name


The generated project will contain 3 modules:

* `core`: the Thrift definition & compiled Java classes.
* `service`: the service implementation.
* `client`: the client for the service.

See the READMEs in each generated module for more information.
