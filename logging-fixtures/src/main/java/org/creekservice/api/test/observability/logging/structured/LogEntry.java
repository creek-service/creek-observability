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

package org.creekservice.api.test.observability.logging.structured;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.creekservice.api.observability.logging.structured.Level;
import org.creekservice.internal.observability.logging.structured.DefaultLogEntryCustomizer;

/** A single log entry */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class LogEntry {

    private final Level level;
    private final Map<String, Object> message;
    private final Optional<Throwable> cause;

    /**
     * Factory method
     *
     * @param level the log level
     * @param message the log message
     * @return the entry
     */
    public static LogEntry logEntry(final Level level, final String message) {
        return logEntry(level, message, Map.of(), Optional.empty());
    }

    /**
     * Factory method
     *
     * @param level the log level
     * @param message the log message
     * @param cause the cause
     * @return the entry
     */
    public static LogEntry logEntry(
            final Level level, final String message, final Throwable cause) {
        return logEntry(level, message, Map.of(), Optional.of(cause));
    }

    /**
     * Factory method
     *
     * @param level the log level
     * @param message the log message
     * @param additional additional key-value entries to include
     * @return the entry
     */
    public static LogEntry logEntry(
            final Level level, final String message, final Map<?, ?> additional) {
        return logEntry(level, message, additional, Optional.empty());
    }

    /**
     * Factory method
     *
     * @param level the log level
     * @param message the log message
     * @param additional additional key-value entries to include
     * @param cause the cause
     * @return the entry
     */
    public static LogEntry logEntry(
            final Level level,
            final String message,
            final Map<?, ?> additional,
            final Throwable cause) {
        return logEntry(level, message, additional, Optional.of(cause));
    }

    /**
     * Factory method
     *
     * @param level the log level
     * @param message the log message
     * @param additional additional key-value entries to include
     * @param cause the cause
     * @return the entry
     */
    public static LogEntry logEntry(
            final Level level,
            final String message,
            final Map<?, ?> additional,
            final Optional<Throwable> cause) {
        final Map<Object, Object> all = new HashMap<>(additional);
        all.put(DefaultLogEntryCustomizer.Field.message.name(), message);
        return logEntry(level, all, cause);
    }

    /**
     * Factory method
     *
     * @param level the log level
     * @param message the structured log message
     * @param cause the cause
     * @return the entry
     */
    public static LogEntry logEntry(
            final Level level, final Map<?, ?> message, final Optional<Throwable> cause) {
        final Map<String, Object> converted =
                requireNonNull(message, "message").entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        e -> String.valueOf(e.getKey()),
                                        Map.Entry::getValue,
                                        (v0, v1) -> v0,
                                        TreeMap::new));
        return new LogEntry(level, converted, cause);
    }

    private LogEntry(
            final Level level, final Map<String, Object> message, final Optional<Throwable> cause) {
        this.level = requireNonNull(level, "level");
        this.message = requireNonNull(message, "message");
        this.cause = requireNonNull(cause, "cause");
    }

    /** @return the log level. */
    public Level level() {
        return level;
    }

    /** @return the structured log message. */
    public Map<String, ?> message() {
        return new TreeMap<>(message);
    }

    /** @return any throwable cause set. */
    public Optional<Throwable> cause() {
        return cause;
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
                && Objects.equals(cause, logEntry.cause);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, message, cause);
    }

    @Override
    public String toString() {
        final String causeText = cause.map(Throwable::toString).map(txt -> " " + txt).orElse("");
        return level + ": " + message + causeText;
    }
}
