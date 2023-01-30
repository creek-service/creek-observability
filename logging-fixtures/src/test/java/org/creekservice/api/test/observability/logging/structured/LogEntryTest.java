/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.test.observability.logging.structured;

import static org.creekservice.api.test.observability.logging.structured.LogEntry.logEntry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import com.google.common.testing.EqualsTester;
import java.util.Map;
import java.util.Optional;
import org.creekservice.api.observability.logging.structured.Level;
import org.junit.jupiter.api.Test;

class LogEntryTest {

    private static final Throwable T = mock(Throwable.class, "Exception-text");

    @Test
    void shouldImplementHashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(
                        logEntry(Level.INFO, "msg", Map.of("k", "v"), T),
                        logEntry(Level.INFO, "msg", Map.of("k", "v"), Optional.of(T)),
                        logEntry(Level.INFO, Map.of("message", "msg", "k", "v"), Optional.of(T)))
                .addEqualityGroup(logEntry(Level.ERROR, "msg", Map.of("k", "v"), Optional.of(T)))
                .addEqualityGroup(logEntry(Level.ERROR, "diff", Map.of("k", "v"), Optional.of(T)))
                .addEqualityGroup(
                        logEntry(Level.INFO, "msg", Map.of(), Optional.of(T)),
                        logEntry(Level.INFO, "msg", T))
                .addEqualityGroup(logEntry(Level.INFO, "msg", Map.of("k", "v")))
                .addEqualityGroup(logEntry(Level.INFO, "msg"))
                .testEquals();
    }

    @Test
    void shouldConvertKeysToStrings() {
        // When:
        final LogEntry entry = logEntry(Level.INFO, "msg", Map.of(10, 12));

        // Then:
        assertThat(entry.message(), is(Map.of("message", "msg", "10", 12)));
    }

    @Test
    void shouldSortKeys() {
        // When:
        final LogEntry entry = logEntry(Level.INFO, "msg", Map.of("a", 0, "z", 1));

        // Then:
        assertThat(entry.message().toString(), is("{a=0, message=msg, z=1}"));
        assertThat(entry.toString(), is("INFO: {a=0, message=msg, z=1}"));
    }

    @Test
    void shouldConvertToTextWithException() {
        // When:
        final LogEntry entry = logEntry(Level.INFO, "msg", Map.of("a", "b"), T);

        // Then:
        assertThat(entry.toString(), is("INFO: {a=b, message=msg} Exception-text"));
    }

    @Test
    void shouldConvertToTextWithoutException() {
        // When:
        final LogEntry entry = logEntry(Level.WARN, "msg", Map.of("a", "b"));

        // Then:
        assertThat(entry.toString(), is("WARN: {a=b, message=msg}"));
    }
}
