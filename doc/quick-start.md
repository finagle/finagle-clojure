# A Quick Start with finagle-clojure & Thrift

This guide will walk you through creating a Thrift service with finagle-clojure.
You'll create a Thrift definition, implement the server, and set up a client.

Thrift is a binary serialization format that can be used with Finagle to do RPC.
A file written in the Thrift Interface Definition Language can be used to generate code in several different languages.
This lets much of the boilerplate (like de/serialization & RPC) of our service be handled by Thrift and finagle-clojure.

As an example we'll build a simple service to tell you whether or not a breed of dog is beautiful (this is hard because dogs are so beautiful).
Let's get started!

## Table of Contents

* [Prerequisites](#prerequisites)
* [Concepts](#finagle-concepts)
    * [Futures](#futures)
    * [Asynchronicity & Future Transformations](#asynchronicity--future-transformations)
    * [Services](#services)
* [Create a New Project](#create-a-new-project)
* [Edit the Thrift Definition](#edit-the-thrift-definition)
* [Implement the Server](#implement-the-server)
* [Using the Client](#using-the-client)
* [Transforming Futures](#transforming-futures)


## Prerequisites

This guide assumes you have a JDK available (Oracle or OpenJDK will work) and [leiningen](http://leiningen.org/) installed.

## Finagle Concepts

There are a couple of important Finagle concepts to introduce before we continue.
If you're already familiar with Finagle you can skip to the [next section](#create-a-new-project).

### Futures

Futures represent the results of asynchronous operations in Finagle.
A Future can be in one of three states: undefined, successful, and unsuccessful.
Successful Futures have been defined with a value that we'd expect to get back from the operation (e.g. the result of an RPC call).
Unsuccessful Futures represent errors that occured during the operation (exceptions).

The undefined state of a Future is very important.
Modeling a first class unknown value in a distributed system is crucial for build robust software.
In a networked environment we can't assume anything from absence of a response, only that the response is missing.

### Asynchronicity & Future Transformations

Finagle runs in an asynchronous environment using [Netty](http://netty.io).
Operations that run on a Finagle thread (like the implementation of a service) cannot block by performing synchronous IO, waiting for a lock, or by waiting for a Future to be defined.
Since we can't wait for a Future to be defined we instead need to register functions to transform the result in the future when it's available.
There will be examples of transforming Futures later on in this document.

### Services

Services describe the asynchronous processing of a request into a Future of a response.
The Service abstraction describes both Clients & Servers in Finagle.
This lets us use asynchronous Finagle Clients in the implementation of our Servers, and lets us share the definition of our RPC operations between them.


## Create a New Project

First we'll use the finagle-clojure lein template to generate a new project.
The template is automatically retrieved by lein so you won't need to install it.

Run this in your console:

    lein new finagle-clojure my-project

The generated project will have three submodules:

* `my-project-core`: This is where you'll put the Thrift definition for the service and compile it to Java.
* `my-project-service`: This is where the server is implemented.
* `my-project-client` This is where the client for your service is implemented.

Separating the project into submodules lets the client & server share the same Thrift definition as a versioned artifact.
You'll only need to compile the Thrift definition (and the generated Java source) when you change the definition.

Here's what the directory layout of my-project should look like:

````
.
|____.gitignore
|____my-project-client
| |____project.clj
| |____README.md
| |____src
| | |____my_project
| | | |____client.clj
| |____test
| | |____my_project
| | | |____client_test.clj
|____my-project-core
| |____project.clj
| |____README.md
| |____src
| | |____java
| | |____thrift
| | | |____schema.thrift
|____my-project-service
| |____project.clj
| |____README.md
| |____src
| | |____my_project
| | | |____service.clj
| |____test
| | |____my_project
| | | |____service_test.clj
|____project.clj
|____README.md
````

## Edit the Thrift Definition

Now we'll edit the Thrift definition and compile it into Java classes using [Scrooge](https://twitter.github.io/scrooge/).
Those Java classes will be used with finagle-clojure to implement the service & client.

The Thrift definition for your project lives in the my-project-core folder, so first `cd my-project-core`.
Then open `src/thrift/schema.thrift` in your editor.

schema.thrift should look like this:

````thrift
namespace java my_project.thrift

service MyProject {
    // TODO
}
````

The Java classes described by this definition will have the package `my_project.thrift`.
'thrift' was appended to your project's name to indicate that these classes are generated.

If you're not familiar with Thrift [here's a good introduction](https://diwakergupta.github.io/thrift-missing-guide/).
We're just going to add some basic structs and a service in this guide, you can copy and paste code from here to keep moving.

We'll use two types of objects that Thrift can generate: structs (a domain object) and services (used to model RPC).
It's a good practice to create Thrift structs for the request and response types of your service.
This makes it easier to change what gets accepted or returned by a service method.

First we'll define a couple of structs for the request and response objects.
Add the following to your schema.thrift:

````thrift
struct BeautifulDogRequest {
    1: string breed
    2: string name
}

struct BeautifulDogResponse {
    1: string name
    2: bool beautiful
}
````

Then run `lein javac` to make sure you wrote valid Thrift.
The lein-finagle-clojure plugin has been set up in my-project-core to automatically run Scrooge when `lein javac` does (look at the `:plugins` in `my-project-core/project.clj`).
You could also compile the Thrift definition (but not the generated Java) by running `lein finagle-clojure scrooge`.

After the Thrift definition has been compiled there should be some Java files in `src/java`.
These are the classes that we'll use with finagle-clojure.

Now let's define some methods on the MyProject service:

````thrift
service MyProject {
    BeautifulDogResponse isBreedBeautiful(1: BeautifulDogRequest request)
}

````

And then run `lein javac` again to make sure everything still compiles.

The entire `schema.thrift` file should look like this:

````thrift
namespace java my_project.thrift

struct BeautifulDogRequest {
    1: string breed
    2: string name
}

struct BeautifulDogResponse {
    1: string name
    2: bool beautiful
}

service MyProject {
    BeautifulDogResponse isBreedBeautiful(1: BeautifulDogRequest request)
}
````

Make sure to run `lein install` before moving on so the `my-project-core` library is available to the other subprojects.
Now that we've defined our service's interface let's implement it!


## Implement The Server

Now that we've defined our service's definition and compiled it to Java let's implement it.
First `cd` to `my-project-service` (on level up from my-project-core).

If you look in the `project.clj` there's a dependency on my-project-core: `[my-project-core "0.1.0-SNAPSHOT"]`.

Now let's edit the main ns for this project: `src/my_project/service.clj`
It should look like this:

````clojure
(ns my-project.service
  (:import [my_project.thrift MyProject])
  (:require [finagle-clojure.futures :as f]
            [finagle-clojure.thrift :as thrift])
  (:gen-class))

(defn make-service
  []
  (thrift/service MyProject
    ;; TODO implement service methods
    ))

(defn -main
  [& args]
  (f/await (thrift/serve ":9999" (make-service))))
````

The Java class for your service (`MyProject`) has been imported & two finagle-clojure namespaces have been required.

The `finagle-clojure.futures` namespace (aliased as `f`) contains helpers to compose Futures together.
Futures will either come from operations on a Service (which could be the Client to a Service running on another machine), returned from other asynchronous libraries, or created directly.
Successful Futures can be created with `f/value`, it will be immediately defined.
Failed Futures can be defined with `f/exception`.
We'll only create Futures that are immediately defined when their value is computed without requiring the value of another Future (e.g. synchronously on the CPU without any IO).

`finagle-clojure.thrift` (aliased as `thrift`) lets us create Finagle servers & clients from a compiled Thrift service definition.
`thrift/service` is used here to provide an implementation for `MyProject`.

Before we can provide an implementation for `isBreedBeautiful` let's import the struct types from our Thrift definition.
Add `BeautifulDogResponse` to the `:import` line in the `ns` definition.
It should now look like this: `(:import [my_project.thrift BeautifulDogResponse MyProject])`

Now we can implement `isBreedBeautiful`.

````clojure
(defn make-service
  []
  (thrift/service MyProject
    (isBreedBeautiful [req]
      (let [breed (.getBreed req)
            name (.getName req)
            beautiful? (not= breed "pomeranian")]
        (f/value (BeautifulDogResponse. name beautiful?))))))
````

The implementation first takes the breed & name off the `BeautifulDogRequest` and determines if it's beautiful (this advanced algorithm has been open sourced with finagle-clojure).
A Future is returned using `f/value` (since the methods of a Service return Futures).

`thrift/serve` takes a port to bind to (as a String) and a Service implementation.
It will listen on that port for requests using the Thrift binary protocol.
`f/await` is called with the result of `thrift/serve` to prevent the program from terminating until the server shuts down.
`f/await` is used to synchronously wait for a Future to be defined.
It should never be used in a Service implementation since it will block Finagle and dramatically reduce its performance.

We can now run our service by executing `lein run`.
Now that we have the server running, let's open a new console and use the Client to communicate with it.


## Using the Client

First `cd` into `my-project-client`.
If you look in its `project.clj` you'll see that it also has a dependency on my-project-core.
Sharing the service definition like this makes sure that the client and server are expecting the same operations.

Open up `src/my_project/client.clj`.
It should look like this:

````clojure
(ns my-project.client
  (:import [my_project.thrift MyProject])
  (:require [finagle-clojure.futures :as f]
            [finagle-clojure.thrift :as thrift]))

(defn make-client
  [address]
  (thrift/client address MyProject))
````

The generated class for `MyProject` has been imported (just like in my-project-service).
The function `make-client` creates a client for the MyProject service located at address (a String).
The function `thrift/client` is used to contruct a Thrift client for the service defined by MyService.

Now let's open up a REPL (`lein repl`) and play around with this.

First require the client ns and make a client that uses the server we just ran:

````clojure
(require '[my-project.client :refer :all])
(require '[finagle-clojure.futures :as f])
(import '[my_project.thrift BeautifulDogRequest])

(def client (make-client "localhost:9999"))
````

Now let's execute `isBreedBeautiful` against our server:

````clojure
(.isBreedBeautiful client (BeautifulDogRequest. "pit bull" "spike"))
; => <Promise ....>
````

This returned a Future (well technically a Promise, a type of Future) that represents the response of this RPC call.
Since Futures are asynchronous we don't know the value of the response unless we wait for it.
Let's try it again using `f/await` to block until the Future is defined:

````clojure
(f/await (.isBreedBeautiful client (BeautifulDogRequest. "pit bull" "spike")))
; => #<BeautifulDogResponse BeautifulDogResponse(name:spike, beautiful:true)>
````

Yay! Spike is beautiful :)

We wouldn't use `f/await` unless we were integrating Finagle into a synchronous environment (like the REPL) since it is a blocking operation.
Let's explore some ways to transform futures in an asynchronous way.

## Transforming Futures

We can use the `f/map` function to do something when the Future returns a successful value.
E.g. to return just the beautiful boolean from the BeautifulDogResponse instead of the whole object:

````clojure
(-> (.isBreedBeautiful client (BeautifulDogRequest. "pit bull" "spike"))
    (f/map [response] 
      (.isBeautiful response))
    f/await) ; => true
````

This should return true.

Let's try it with an ugly dog:

````clojure
(-> (.isBreedBeautiful client (BeautifulDogRequest. "pomeranian" "spike"))
    (f/map [response] 
      (.isBeautiful response))
    f/await) ; => false
````

This should return false.

`f/map` is used to when the transformation returns a raw value (not a Future, i.e. no other RPC calls) synchronously (e.g. hashing the response).
`f/flatmap` is used when the transformation returns a Future (e.g. it makes a request to another service).
E.g.:

````clojure
(-> (.isBreedBeautiful client (BeautifulDogRequest. "pomeranian" "spike"))
    (f/flatmap [response]
      (.isBreedBeautiful (BeautifulDogRequest. "hound dog" "socks"))
    f/await) ; => #<BeautifulDogResponse BeautifulDogResponse(name:socks, beautiful:true)>
````

Notice how the value of the first Future (for the pomeranian named spike) has been supplanted by the request for a hound named socks.
`f/flatmap` is typically used when your transforming the successful value of a Future with another asynchronous operation (e.g. a call to a different service).

What if a Future is unsuccesful and returns an error? We can transform errored futures using `f/handle` & `f/rescue`.
We can create Futures that are immediately defined with an unsuccessful value like this: `(f/exception (Exception.))`.

`f/handle` is like `f/map` but for errors.
You can use it to transform an failed Future into a successful one:

````clojure
(-> (f/exception (Exception.))
    (f/handle [t] false)
    f/await) ; => false
````

Likewise `f/rescue` is like `f/flatmap` for unsuccessful Futures:

````clojure
(-> (f/exception (Exception.))
    (f/rescue [t] (f/value false)) ; note the explicit Future that gets returned
    f/await) ; => false
````

We can also match on the type of exception that gets thrown using `f/match-class`:

````clojure
(-> (f/exception (IllegalArgumentException.))
    (f/handle [t]
      (f/match-class t
        MethodNotFoundException :method-not-found
        IllegalArgumentException :illegal-argument
        Exception :exception))
    f/await) ; => :illegal-argument
````

`f/map` & `f/flatmap` won't run if a Future is unsuccessful, and `f/handle` & `f/rescue` won't run if a Future is successful:

````clojure
(-> (f/exception (IllegalArgumentException.))
    (f/map [v] :map)
    (f/handle [t] :handle)
    f/await) ; => :handle

(-> (f/value true)
    (f/map [v] :map)
    (f/handle [t] :handle)
    f/await) ; => :map
````

Note that in the previous example if `(f/handle [t] :handle)` were above `f/map` then `f/map` would also run since the `f/handle` call transformed the failed Future into a successful one.

