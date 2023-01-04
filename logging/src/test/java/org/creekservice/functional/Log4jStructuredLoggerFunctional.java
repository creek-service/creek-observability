/*
 * Copyright 2022 Creek Contributors (https://github.com/creek-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.creekservice.functional;

import java.time.Duration;
import java.time.Instant;
import org.creekservice.api.observability.logging.structured.StructuredLogger;
import org.creekservice.api.observability.logging.structured.StructuredLoggerFactory;

/** Run me to view generated log output. */
public final class Log4jStructuredLoggerFunctional {

    private Log4jStructuredLoggerFunctional() {}

    public static void main(final String... args) {
        // Uncomment to try out  XML:
        // System.setProperty("log-format", "xml");
        System.setProperty("AGGREGATE", "PRICING");
        System.setProperty("SERVICE", "BLOOMBERG");

        final StructuredLogger logger =
                StructuredLoggerFactory.internalLogger(Log4jStructuredLoggerFunctional.class);

        final Instant start = Instant.now();

        logger.info("a simple message");
        logger.info("the message", metrics -> metrics.with("a", 10));
        logger.info("the message", metrics -> metrics.with("a", new CustomType("Bob")));
        logger.error(
                "the message",
                metrics -> metrics.with("a", 10).withThrowable(new OutOfMemoryError()));
        logger.info(
                "message", metrics -> metrics.with("took", Duration.between(start, Instant.now())));

        System.out.println();
        System.out.println();

        someMethod(logger, 1);
        someMethod(logger, 2);
    }

    private static void someMethod(final StructuredLogger logger, final int attempt) {
        final Instant start = Instant.now();

        logger.trace("Starting", log -> log.ns("my.app").with("attempt", attempt));
        doStuff();
        logger.info(
                "Did stuff",
                log ->
                        log.ns("my.app")
                                .with("attempt", attempt)
                                .with("took", Duration.between(start, Instant.now())));
    }

    private static void doStuff() {
        try {
            Thread.sleep(1, 12);
        } catch (InterruptedException e) {
            // meh.
        }
    }

    private static class CustomType {
        private final String text;

        CustomType(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "CustomType(" + text + ")";
        }
    }
}
