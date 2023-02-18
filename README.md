[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Coverage Status](https://coveralls.io/repos/github/creek-service/creek-observability/badge.svg?branch=main)](https://coveralls.io/github/creek-service/creek-observability?branch=main)
[![Build](https://github.com/creek-service/creek-observability/actions/workflows/build.yml/badge.svg)](https://github.com/creek-service/creek-observability/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.creekservice/creek-observability-logging.svg)](https://central.sonatype.dev/search?q=creek-observability-*)
[![CodeQL](https://github.com/creek-service/creek-observability/actions/workflows/codeql.yml/badge.svg)](https://github.com/creek-service/creek-observability/actions/workflows/codeql.yml)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-observability/badge)](https://api.securityscorecards.dev/projects/github.com/creek-service/creek-observability)
[![OpenSSF Best Practices](https://bestpractices.coreinfrastructure.org/projects/6899/badge)](https://bestpractices.coreinfrastructure.org/projects/6899)

# Creek Observability

Libraries to help with observing running services.

The libraries published by this repository are intended to be used by those writing microservices using Creek.
Their intent is to provide a logging abstraction on top of [Slf4J][slf4j] that promotes more structured logs.
Structured logs are more easily ingested, indexed and searched in modern log aggregate, search & visualisation tools, 
such as [Splunk][splunk], [the ELK stack][elk], etc.  With structured logging, these tools can be leveraged to build
sophisticated dashboards and alerts of log messages. 

See [CreekService.org](https://www.creekservice.org) for info on Creek Service.

## Modules

The repo contains the following modules:

* **[lifecycle](lifecycle)** [[JavaDocs](https://javadoc.io/doc/org.creekservice/creek-observability-lifecycle)]: a common model of lifecycle events.
* **[logging](logging)** [[JavaDocs](https://javadoc.io/doc/org.creekservice/creek-observability-logging)]: handles the logging of _structured_ events via [Slf4J][slf4j].
* **[logging fixtures](logging-fixtures)** [[JavaDocs](https://javadoc.io/doc/org.creekservice/creek-observability-logging-fixtures)]: test fixtures for testing logging output.

[slf4j]: https://www.slf4j.org
[splunk]: https://www.splunk.com
[elk]: https://www.elastic.co/what-is/elk-stack