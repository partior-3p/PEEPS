# PegaSys End to End Product test Suite

[![CircleCI](https://circleci.com/gh/Consensys/PEEPS.svg?style=svg&circle-token=9bb4214a9d8baeee39bc1fbce181179460b414f5)](https://circleci.com/gh/PegaSysEng/PEEPS)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/PEEPS/blob/master/LICENSE)

[Governance](GOVERNANCE.md)

## ⚠️ Project Deprecation ⚠️
This project is deprecated. Besu testing is done within Hive and Besu testing suites.

## Purpose
PEEPS is a product integration test suite. Tests are written levering a custom DSL that provides concise fluent language at an appropriate level of abstraction from the implementation details.
Main purpose is to test interop between Besu and GoQuorum.

Conceptually PEEPS consists of two parts:
- A DSL (domain specific language) to easily author end to end tests. This layer providing the binding between the abstract language and specific implementations for each product.  
- End to End suite. Test cases that leverage the DSL to produce concise tests, at the appropriate level of abstraction for easy understanding by most.

## Architecture

### End To End Tests
The module `end-to-end-tests` contains the product end to end tests and their resources.

`NetworkTest` is intended as the abstract base class for all tests that require the Network primative as part of their testing, where a Network is connection between products (Ethereum nodes, Privacy Managers and Signers ...which should be every end to end test in PEEPS).

### DSL
The module `dsl` contains the domain specific language syntax and semantic objects, in addition to the binding code the underlying products.

When writing tests, the only reason to extend the DSL would be when adding a new product, or exposing a new service of the product, yet unexposed.

The point of orchestration for the DSL is the `Network` class. 
 
