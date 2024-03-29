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

package org.creekservice.internal.observability.logging.structured;

import static java.util.Objects.requireNonNull;
import static org.creekservice.api.base.type.Preconditions.requireNonBlank;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.creekservice.api.observability.logging.structured.LogEntryCustomizer;

/** Default impl of the {@link LogEntryCustomizer} type. */
public final class DefaultLogEntryCustomizer implements LogEntryCustomizer {

    /** Standard fields */
    public enum Field {
        /** The text message field. */
        message
    }

    private final Map<String, Object> metrics = new HashMap<>();
    private final Map<String, DefaultLogEntryCustomizer> namespaces = new HashMap<>();
    private final Throwable[] throwable;

    /**
     * Factory method
     *
     * @param messageText the text message to log
     * @return the log customizer.
     */
    public static DefaultLogEntryCustomizer create(final String messageText) {
        return (DefaultLogEntryCustomizer)
                new DefaultLogEntryCustomizer(new Throwable[1]).with(Field.message, messageText);
    }

    private DefaultLogEntryCustomizer(final Throwable[] throwable) {
        this.throwable = requireNonNull(throwable, "throwable");
    }

    @Override
    public DefaultLogEntryCustomizer ns(final String namespace) {
        if (metrics.containsKey(namespace)) {
            throw new IllegalArgumentException(
                    "Namespace name clashes with existing metric name: " + namespace);
        }
        return namespaces.computeIfAbsent(
                requireNonBlank(namespace, "namespace"),
                k -> new DefaultLogEntryCustomizer(throwable));
    }

    @Override
    public LogEntryCustomizer with(final String key, final Object value) {
        requireNonBlank(key, "key");
        if (namespaces.containsKey(key)) {
            throw new IllegalArgumentException(
                    "Metric name clashes with existing namespace name: " + key);
        }

        if (metrics.containsKey(key)) {
            throw new IllegalArgumentException("Metric key already set: " + key);
        }

        metrics.put(key, value);
        return this;
    }

    @Override
    public LogEntryCustomizer withThrowable(final Throwable t) {
        if (throwable[0] != null) {
            throw new UnsupportedOperationException("Exception already set", throwable[0]);
        }

        throwable[0] = t;
        return this;
    }

    /**
     * Build the log entry
     *
     * @return the structured log entry
     */
    public Map<String, Object> build() {
        final Map<String, Object> result = new HashMap<>(metrics);
        namespaces.forEach((name, customizer) -> result.put(name, customizer.build()));
        removeNullValues(result);
        return result.isEmpty() ? null : result;
    }

    /**
     * @return any throwable set.
     */
    public Optional<Throwable> throwable() {
        return Optional.ofNullable(throwable[0]);
    }

    private static void removeNullValues(final Map<String, Object> m) {
        m.values().removeAll(Collections.singleton(null));
    }
}
