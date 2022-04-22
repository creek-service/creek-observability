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

package org.creek.api.test.observability.logging.structured;

import static java.util.Objects.requireNonNull;
import static org.creek.api.observability.logging.structured.Level.TRACE;
import static org.creek.api.test.observability.logging.structured.LogEntry.logEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.creek.api.observability.logging.structured.Level;
import org.creek.api.observability.logging.structured.LogEntryCustomizer;
import org.creek.api.observability.logging.structured.StructuredLogger;
import org.creek.internal.observability.logging.structured.DefaultLogEntryCustomizer;

public final class TestStructuredLogger implements StructuredLogger {

    private final Level minLevel;
    private final List<LogEntry> entries = new ArrayList<>();

    public static TestStructuredLogger create() {
        return create(TRACE);
    }

    public static TestStructuredLogger create(final Level level) {
        return new TestStructuredLogger(level);
    }

    private TestStructuredLogger(final Level level) {
        this.minLevel = requireNonNull(level, "level");
    }

    @Override
    public void log(
            final Level level,
            final String message,
            final Consumer<LogEntryCustomizer> customizeConsumer) {
        if (level.ordinal() < minLevel.ordinal()) {
            return;
        }

        final DefaultLogEntryCustomizer customizer = DefaultLogEntryCustomizer.create(message);
        customizeConsumer.accept(customizer);

        entries.add(logEntry(level, customizer.build(), customizer.throwable()));
    }

    public List<LogEntry> entries() {
        return List.copyOf(entries);
    }

    public List<String> textEntries() {
        return entries.stream().map(LogEntry::toString).collect(Collectors.toUnmodifiableList());
    }

    public void clear() {
        entries.clear();
    }
}
