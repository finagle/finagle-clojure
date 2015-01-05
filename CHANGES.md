# Changes

Minor release before 1.0.0 may include breaking changes and will explicitly mark them as such.

## Next Release

* [core] Add support for automatically lifting Clojure fns to Scala Functions. This allows wrappers like `finagle-clojure.futures/flatmap*` to accept either Clojure or Scala functions.
Ordinary Clojure fns can be used with any desugared wrapper over a method that ordinarily accepts a Scala function.

### Version 0.2.0

There are no breaking changes in this release.

* lein-finagle-clojure now correctly ignores Emacs temp files in the thrift directory (thanks @derekslager!). [PR](https://github.com/finagle/finagle-clojure/pull/2)
* Add support for finagle-http (thanks @bguthrie!). [PR](https://github.com/finagle/finagle-clojure/pull/4)
  * finagle-clojure/http can be used to make asynchronous HTTP requests or to build HTTP servers.
  * Check out the [integration test](https://github.com/finagle/finagle-clojure/blob/8d8fd428c24bfcb5d8dab37fb42be6cba6d8f7dd/http/test/finagle_clojure/http/integration_test.clj) for example usage.
* Add helpers in finagle-clojure/core for working with `scala.Option` objects (in the `finagle-clojure.options` ns, thanks @bguthrie!).
* New Scala interop helper `tuple->vec`, converts Scala Tuples to Clojure Vectors (thanks @bguthrie!).
* Add [ThriftMux](http://twitter.github.io/finagle/docs/index.html#com.twitter.finagle.mux.package) support in finagle-clojure/thriftmux
  * thriftmux projects can be generated using the finagle-clojure-template lein template by passing the template arg `project-type thriftmux`
  * e.g. `lein new finagle-clojure dogs -- project-type thriftmux`
  * the default for finagle-clojure projects remains thrift
* Upgrade Finagle to version 6.24.0 (from 6.18.0). See the Finagle [release notes](https://github.com/twitter/finagle/blob/finagle-6.24.0/CHANGES)
* Upgrade scrooge-generator in lein-finagle-clojure to version 3.17.0 (from 3.16.3). See the Scrooge [release notes](https://github.com/twitter/scrooge/blob/870e03227d1ab52c37f323118561ad4b79485a0d/CHANGES).

### Version 0.1.1

* Initial release!
* Releasing version 0.1.0 was aborted as missing scm info in project.cljs prevented promition on clojars.
