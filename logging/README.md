# Creek Observability Logging

Wrapper around [Slf4J][1] aimed at encouraging more structured logging.

More structured logging is more easily ingested by log aggregation and visualization tools, such as Elasticsearch or Splunk.

For example, the code snippet below logs events with structured metrics:

```java
class SomeType {

    private static final StructuredLogger LOGGER = StructuredLoggerFactory.logger(SomeType.class);
    
    void someMethod(int attempt) {
        final Instant start = Instant.now();
        
        LOGGER.trace("Starting", log -> log.ns("my.app").with("attempt", attempt));
        doStuff();
        LOGGER.info("Did stuff", log -> log.ns("my.app").with("attempt", attempt).with("took", Duration.between(start, Instant.now())));
    }
}
```

Depending on the logging framework used and how it is configured, this might result in a log line such as:

```json
{
  "timestamp" : "2022-03-03T14:20:37.414+0000",
  "level" : "INFO",
  "loggerName" : "org.acme.SomeType",
  "message" : "{\"message\":\"Did stuff\",\"my.app\":{\"took\":\"PT0.003938S\",\"attempt\":2}}",
  "threadId" : 1,
  "thread" : "main",
  "env" : "PROD",
  "aggregate" : "PRICING",
  "service" : "BLOOMBERG",
  "version" : "2.1.9",
  "host" : "aws.server1"
}
```

The `message` field contains a nested JSON document containing the message and metrics being logged.

[1]: https://www.slf4j.org