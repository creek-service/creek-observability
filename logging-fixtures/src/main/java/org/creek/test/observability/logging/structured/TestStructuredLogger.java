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

import static java.util.Objects.requireNonNull;
import static org.creek.api.observability.logging.structured.Level.TRACE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
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

        entries.add(new LogEntry(level, customizer.build(), customizer.throwable()));
    }

    public List<LogEntry> entries() {
        return entries;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static final class LogEntry {

        private final Level level;
        private final Map<String, Object> message;
        private final Optional<Throwable> throwable;

        public static LogEntry logEntry(
                final Level level,
                final Map<String, Object> message,
                final Optional<Throwable> throwable) {
            return new LogEntry(level, message, throwable);
        }

        private LogEntry(
                final Level level,
                final Map<String, Object> message,
                final Optional<Throwable> throwable) {
            this.level = requireNonNull(level, "level");
            this.message = requireNonNull(message, "message");
            this.throwable = requireNonNull(throwable, "throwable");
        }

        public Level level() {
            return level;
        }

        public Map<String, ?> message() {
            return message;
        }

        public Optional<Throwable> throwable() {
            return throwable;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final LogEntry logEntry = (LogEntry) o;
            return level == logEntry.level
                    && Objects.equals(message, logEntry.message)
                    && Objects.equals(throwable, logEntry.throwable);
        }

        @Override
        public int hashCode() {
            return Objects.hash(level, message, throwable);
        }

        @Override
        public String toString() {
            return "LogEntry{"
                    + "level="
                    + level
                    + ", message="
                    + message
                    + ", throwable="
                    + throwable
                    + '}';
        }
    }
}
