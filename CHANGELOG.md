# Changelog

## 0.10.0
We are bumping `org.apache.thrift/libthrift` on `finagle-cloure/thrift` due to security issues with the current version.
The nearest version without known vulnerabilities is `0.13.0`.

## Breaking changes from 0.10.0 to 0.13.0

### 0.11.0 -> 0.12.0

- THRIFT-4529 - Rust enum variants are now camel-cased instead of uppercased to conform to Rust naming conventions
- THRIFT-4448 - Support for golang 1.6 and earlier has been dropped.
- THRIFT-4474 - PHP now uses the PSR-4 loader by default instead of class maps.
- THRIFT-4532 - method signatures changed in the compiler's t_oop_generator.
- THRIFT-4648 - The C (GLib) compiler's handling of namespaces has been improved.

### 0.12.0 -> 0.13.0

- THRIFT-4743 - compiler: removed the plug-in mechanism
- THRIFT-4720 - cpp: C++03/C++98 support has been removed; also removed boost as a runtime dependency
- THRIFT-4730 - cpp: BoostThreadFactory, PosixThreadFactory, StdThreadFactory removed
- THRIFT-4732 - cpp: CMake build changed to use BUILD_SHARED_LIBS
- THRIFT-4735 - cpp: Removed Qt4 support
- THRIFT-4740 - cpp: Use std::chrono::duration for timeouts
- THRIFT-4762 - cpp: TTransport::getOrigin() is now const
- THRIFT-4702 - java: class org.apache.thrift.AutoExpandingBuffer is no longer public
- THRIFT-4709 - java: changes to UTF-8 handling require JDK 1.7 at a minimum
- THRIFT-4712 - java: class org.apache.thrift.ShortStack is no longer public
- THRIFT-4725 - java: change return type signature of 'process' methods
- THRIFT-4805 - java: replaced TSaslTransportException with TTransportException
- THRIFT-2530 - java: TIOStreamTransport's "isOpen" now returns false after "close" is called
- THRIFT-4675 - js: now uses node-int64 for 64 bit integer constants
- THRIFT-4841 - delphi: old THTTPTransport is now TMsxmlHTTPTransport
- THRIFT-4536 - rust: convert from try-from crate to rust stable (1.34+), re-export ordered-float

## [0.9.0-NUBANK]
-Bump Finagle version to 20.8.1

## [0.8.0-NUBANK]
-Bump Finagle version to 19.12.0
-Remove MySql and ThriftMux modules

