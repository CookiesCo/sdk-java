
## Cookies SDK for Java

This repository supplies tools to interact with Cookies APIs from Java or other JDK-based languages. Client SDKs provided by Cookies are thin facades on top of generated gRPC and Protobuf classes, which each SDK pulls in and keeps in lock-step.


### Services

The Java SDK supports the following APIs so far:

- [ ] **App API**: Fetch lists of stores, strains, etc. Provides endpoints that roughly map to user application expectations.
- [x] **Catalog API**: Fetch a set of canonical Cookies product content, given a locale and scope.
- [ ] **Inventory API**: Fetch current on-hand inventory for a given supply chain locale, anywhere in the Cookies network.


### Authentication

All Cookies APIs require some form of authentication. Sensitive APIs require multiple layers of authentication. These SDKs are designed for private use only: for access details and credentials, please file an issue with the [Cookies Helpdesk](https://go.cookies.co/helpdesk).


### Legal

This code is owned by Cookies Creative Consulting & Promotions, Inc. All rights reserved, authorized use only.

