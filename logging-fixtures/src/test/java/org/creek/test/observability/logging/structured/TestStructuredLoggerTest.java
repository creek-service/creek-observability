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

package org.creek.test.observability.logging.structured;

import static org.creek.test.observability.logging.structured.TestStructuredLogger.LogEntry.logEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import com.google.common.testing.EqualsTester;
import java.util.Map;
import java.util.Optional;
import org.creek.api.observability.logging.structured.Level;
import org.junit.jupiter.api.Test;

class TestStructuredLoggerTest {

    private static final Throwable T = mock(Throwable.class);

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void shouldImplementHashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(
                        logEntry(Level.INFO, Map.of("k", "v"), Optional.of(T)),
                        logEntry(Level.INFO, Map.of("k", "v"), Optional.of(T)))
                .addEqualityGroup(logEntry(Level.ERROR, Map.of("k", "v"), Optional.of(T)))
                .addEqualityGroup(logEntry(Level.INFO, Map.of(), Optional.of(T)))
                .addEqualityGroup(logEntry(Level.INFO, Map.of("k", "v"), Optional.empty()))
                .testEquals();
    }

    @Test
    void shouldCaptureLogs() {
        // Given:
        final TestStructuredLogger logger = TestStructuredLogger.create();

        // When:
        logger.trace("message-text", log -> log.ns("ns").with("k", "v").withThrowable(T));

        // Then:
        assertThat(
                logger.entries(),
                contains(
                        logEntry(
                                Level.TRACE,
                                Map.of("message", "message-text", "ns", Map.of("k", "v")),
                                Optional.of(T))));
    }

    @Test
    void shouldCaptureLogsIfLevelHighEnough() {
        // Given:
        final TestStructuredLogger logger = TestStructuredLogger.create(Level.INFO);

        // When:
        logger.info("message-text", log -> log.ns("ns").with("k", "v").withThrowable(T));

        // Then:
        assertThat(logger.entries(), hasSize(1));
        assertThat(logger.entries().get(0).level(), is(Level.INFO));
        assertThat(
                logger.entries().get(0).message(),
                is(Map.of("message", "message-text", "ns", Map.of("k", "v"))));
        assertThat(logger.entries().get(0).throwable(), is(Optional.of(T)));
    }

    @Test
    void shouldNotCaptureLogsIfLevelTooLow() {
        // Given:
        final TestStructuredLogger logger = TestStructuredLogger.create(Level.INFO);

        // When:
        logger.debug("message-text", log -> log.ns("ns").with("k", "v").withThrowable(T));

        // Then:
        assertThat(logger.entries(), is(empty()));
    }

    @Test
    void shouldAllowEntriesToBeCleared() {
        // Given:
        final TestStructuredLogger logger = TestStructuredLogger.create();
        logger.trace("message-text", log -> log.ns("ns").with("k", "v").withThrowable(T));

        // When:
        logger.entries().clear();

        // Then:
        assertThat(logger.entries(), is(empty()));
    }
}
