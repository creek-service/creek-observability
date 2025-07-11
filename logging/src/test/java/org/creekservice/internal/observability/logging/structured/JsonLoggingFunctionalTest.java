/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.internal.observability.logging.structured;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
public class JsonLoggingFunctionalTest {

    @Mock private Logger slf4jLogger;
    private Slf4jStructuredLogger logger;

    @BeforeEach
    void setUp() {
        logger =
                new Slf4jStructuredLogger(
                        slf4jLogger,
                        Optional.empty(),
                        DefaultLogEntryCustomizer::create,
                        new JsonLogEntryFormatter());
    }

    @Test
    void shouldLogToValidJson() {
        // Given:
        when(slf4jLogger.isErrorEnabled()).thenReturn(true);
        final Throwable cause = new RuntimeException("some\t\n\"cause");

        // When:
        logger.error(
                "some\t\n\"message",
                line -> line.with("a", "some\t\n\"value").ns("some\t\n\"ns").withThrowable(cause));

        // Then:
        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(slf4jLogger).error(captor.capture(), isNull(Throwable.class));
        final JsonNode root = toJson(captor.getValue());

        assertThat(String.valueOf(root.get("message")), is("\"some\\t\\n\\\"message\""));
        assertThat(String.valueOf(root.get("a")), is("\"some\\t\\n\\\"value\""));

        final JsonNode ns = root.get("some\t\n\"ns");
        assertThat(
                String.valueOf(ns.get("cause")),
                startsWith("\"java.lang.RuntimeException: some\\t\\n\\\"cause"));
        assertThat(
                String.valueOf(ns.get("cause")),
                containsString("\\n\\tat creek.observability.logging@"));
        assertThat(
                String.valueOf(ns.get("cause")),
                containsString(
                        "/org.creekservice.internal.observability.logging.structured"
                            + ".JsonLoggingFunctionalTest.shouldLogToValidJson(JsonLoggingFunctionalTest.java:"));
    }

    private JsonNode toJson(final String value) {
        try {
            final JsonMapper mapper = JsonMapper.builder().build();
            return mapper.readTree(value);
        } catch (final Exception e) {
            throw new AssertionError("Invalid JSON: " + value, e);
        }
    }
}
