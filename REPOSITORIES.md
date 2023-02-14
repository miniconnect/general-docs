# Overview of the repositories

These repositories contain the source code of the application components:

![Repositories](repo-deps.svg)

[See also the version compatibility table.](VERSIONS.md)

All inter-repository dependencies between the subprojects are top-down in the figure.

## [miniconnect-api](https://github.com/miniconnect/miniconnect-api)

Minimalistic database API, an alternative to JDBC.
The philosophy is, that a minimalistic database access API should send SQL queries and input data to the server, and accept the results, nothing more.
That's exactly what MiniConnect session API provides.
No odd abstractions like `startTransaction()` or `setCatalog()`.
No JDBC freaks like `nativeSQL()` or `setTypeMap()`.
Just a lightweight, REPL-able SQL interpreter.

## [miniconnect](https://github.com/miniconnect/miniconnect)

Minimalistic database API, JDBC bridge, and related libraries.
The `record` project provides a higher level easy-to-use wrapper over MiniResultSet.

## [miniconnect-client](https://github.com/miniconnect/miniconnect-client)

Command line SQL REPL that uses the miniConnect API.
You can connect to a miniConnect server with the `micl` (or `miniconnect-client`) command:

## [minibase](https://github.com/miniconnect/minibase)

Java framework for building relational database engines.
It can be instantly used via the miniConnect API.
MiniBase has built-in support for the most important SQL queries (`SELECT`, `UPDATE`, `INSERT`, `REPLACE`, `DELETE` etc.).
It supports multiple schemas, multi-schema queries, user variables and more.

## [holodb](https://github.com/miniconnect/holodb)

Relational database, seemingly filled with random data that data does not actually take up any space in memory or on a volume.
An optional second layer is allowing write access.
So, you can start an arbitrarily large database in moments, with minimal effort; all you need is a configuration file.
