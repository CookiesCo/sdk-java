# Cookies SDK for Java  ![beta](https://img.shields.io/badge/-beta-blue)<br />[![Build status](https://badge.buildkite.com/ad6c581743117b64949a555e15a8daac93ac33138c7043542b.svg)](https://buildkite.com/cookies/java-sdk-java)  [![codecov](https://codecov.io/gh/CookiesCo/sdk-java/branch/main/graph/badge.svg?token=2NzdJGd8ks)](https://codecov.io/gh/CookiesCo/sdk-java)  [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=CookiesCo_sdk-java&metric=alert_status)](https://sonarcloud.io/dashboard?id=CookiesCo_sdk-java)  [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=CookiesCo_sdk-java&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=CookiesCo_sdk-java)  [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=CookiesCo_sdk-java&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=CookiesCo_sdk-java)  [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=CookiesCo_sdk-java&metric=security_rating)](https://sonarcloud.io/dashboard?id=CookiesCo_sdk-java)

This repository supplies tools to interact with Cookies APIs from Java or other JDK-based languages. Client SDKs
provided by Cookies are thin facades on top of generated gRPC and Protobuf classes, which each SDK pulls in and keeps in
lock-step.


### Services

The Java SDK supports the following APIs so far:

- [ ] **App API**: Fetch lists of stores, strains, etc. Provides methods that roughly map to user application features.
- [x] **Catalog API**: Fetch a set of canonical Cookies product content, given a locale and scope.
- [ ] **Inventory API**: Fetch on-hand inventory for a given supply chain locale, anywhere in the Cookies network.


### Installing the SDK

The SDK can be pulled in via Maven Coordinates in any Maven, Gradle, Bazel, or other Java project that can use Maven
dependencies. The following information can be added to each major Java toolchain -- **make sure you add the
repository** because we are not yet publishing to Maven Central:

#### Maven

In your **`pom.xml`**:
```xml
<dependencies>
    <dependency>
        <groupId>co.cookies.sdk</groupId>
        <artifactId>sdk-java</artifactId>
        <version>v1.2021r1-beta1</version>
    </dependency>
</dependencies>

<repositories>
    <repository>
        <repository>
            <url>https://maven.pkg.github.com/CookiesCo/sdk-java</url>
        </repository>
    </repository>
</repositories>
```

#### Gradle

Groovy syntax:
```groovy
dependencies {
    implementation 'co.cookies.sdk:sdk-java:v1.2021r1-beta1'
}

repositories {
    maven {
        url "https://maven.pkg.github.com/CookiesCo/sdk-java"
    }
}
```

Kotlin syntax:
```kotlin
dependencies {
    implementation("co.cookies.sdk.sdk-java:v1.2021r1-beta1")
}

maven {
    url = uri("https://maven.pkg.github.com/CookiesCo/sdk-java")
}
```

#### Bazel

In your **`WORKSPACE`**:
```starlark
maven_jar(
  name = "co_cookies_sdk",
  artifact = "co.cookies.sdk.sdk-java:v1.2021r1-beta1",
  sha256 = "104329b140bfccaaf77bf0aeff38922c84d8f700f6116e369f6bf2c4ab4ac33f",
)
```


------------


### Getting started with the SDK

The SDK is based on [Protocol Buffers](https://developers.google.com/protocol-buffers) and [gRPC](https://grpc.io), both
from Google. Logging is implemented via [SLF4J](http://www.slf4j.org/). You will need peer dependencies on these in your
own codebase:

#### Peer dependencies

| Dependency          | Coordinates                         | Version      |
| ------------------- | ----------------------------------- | ------------ |
| Protocol Buffers    | `com.google.protobuf:protobuf-java` | ``           |
| gRPC Java           | `io.grpc:grpc-bom`                  |              |
| _Any SLF4J library_ | _N/A_                               | _N/A_        |

#### A note about logging

Since the SDK logs via [SLF4J](http://www.slf4j.org/), any compliant logging implementation can be used. At Cookies we
use [Logback](http://logback.qos.ch/), which is great, or you can opt for [Log4J](https://logging.apache.org/log4j/2.x/)
([adapter](http://www.slf4j.org/api/org/slf4j/impl/Log4jLoggerAdapter.html)),
[SLF4J's stub](http://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html) or, if you're on a new enough JDK, the
[native logging available in JDK14+](http://www.slf4j.org/api/org/slf4j/impl/JDK14LoggerAdapter.html).

If you want to use Logback and get it over with:
- Add a dependency on `ch.qos.logback:logback-classic:1.2.5`
- Configure your logging in `logback.xml` at the root of the classpath (see
  [example here](src/test/resources/logback.xml))

##### Enabling logging for specific services

Unless instructed otherwise by the developer, the SDK will create loggers at a specific calculable path for each service
client. The service tag is `co.cookies.services:<service name>:<service version>` (for example,
`co.cookies.services:catalog:v1`).

You can enable `DEBUG` logging for the `catalog:v1` service client like this using Logback:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <withJansi>true</withJansi>
    <encoder>
      <pattern>%cyan(%d{HH:mm:ss.SSS}) %gray([%thread]) %highlight(%-5level) %magenta(%logger{36}) - %msg%n</pattern>
    </encoder>
  </appender>
  <root level="info">
    <appender-ref ref="STDOUT" />
  </root>

  <logger name="co.cookies.services:catalog:v1" level="DEBUG" />
</configuration>
```

_(Colorized logging added because why not.)_


### Credentials

All Cookies APIs require some form of authentication. Sensitive APIs require multiple layers of authentication. These
SDKs are designed for private use only: for access details and credentials, please file an issue with the
[Cookies Helpdesk](https://go.cookies.co/helpdesk).


### API Resources

There are some additional API-level resources which are available behind varying degrees of credentials. For testing API
traffic, the _API console_ is a great place to start. You can also find object and interface reference documentation
there, and guides detailing how to acquire keys and diagnose issues.

Additionally, Cookies produces [OpenAPI](https://www.openapis.org/) specifications that correspond with the binary
service descriptors generated by `protoc`. The OpenAPI spec describes the REST API for each service and can be loaded
into tools like [Postman](https://www.postman.com/).

#### API Console

Access the API Console at:
[https://console.api.cloud.cookies.co/](https://console.api.cloud.cookies.co)

#### OpenAPI/Postman Spec

_Coming soon._


------------


### Using the SDK to call Cookies APIs

Once you have the SDK set up, authorizing your calls and issuing them to production is easy. Here are some examples to
get you started. **Any method can be executed in async or sync mode**, and support is also built-in for server-side
streaming:

#### _Synchronous:_ Fetch a list of Cookies brands

_**TIP:**_ Use the `.CookiesSDK.sync` factory, imported statically, to leverage blocking RPCs.

```java
package com.my.cool.app;
// ...
import co.cookies.sdk.CookiesSDK;
import co.cookies.schema.catalog.BrandsRequest;
import static co.cookies.sdk.CookiesSDK.sync;

// ... later ...

// acquire an instance of the SDK, configured with our API key.
var sdk = CookiesSDK.builder()
           .setApiKey(Optional.of("<your-api-key>")
           .build();

var brands = sdk.catalog().brands(sync(BrandsRequest.getDefaultInstance());

// brands is now a List<Brand>:
// [
//   {id: "C6BI-00001", brand: {/* ... */}},
//   {id: "C6BI-00002", brand: {/* ... */}},
//   ...
// ]
```

#### _Asynchronous:_ Fetch a list of Cookies strains

_**TIP:**_ Use the `.CookiesSDK.async` factory, imported statically, to leverage non-blocking RPCs.

```java
package com.my.cool.app;
// ...
import co.cookies.sdk.CookiesSDK;
import co.cookies.schema.catalog.StrainsRequest;
import static co.cookies.sdk.CookiesSDK.async;

// ... later ...

// acquire an instance of the SDK, configured with our API key.
var sdk = CookiesSDK.builder()
           .setApiKey(Optional.of("<your-api-key>")
           .build();

var strains = sdk.catalog().strains(async(StrainsRequest.getDefaultInstance());

// because we used `async`, strains is now a ListenableFuture<List<Strain>>.
// so, if we block on a result:

var strainsList = strains.get();

// `strainsList` will now be a List<Strain>:
// [
//   {id: "C6C20-0001", slug: "cereal-milk", name: {primary: "Cereal Milk"}, brand: {/* ... */}},
//   {id: "C6V20-0002", slug: "pink-rozay", name: {primary: "Pink Rozay"}, brand: {/* ... */}},
//   ...
// ]
```

------------


### Legal stuff

This code is owned by Cookies Creative Consulting & Promotions, Inc. All rights reserved, authorized use only. Please
see license header at the top of all Java source files. Use of this computer code in source or object form constitutes
binding agreement with the embedded license provisions.
