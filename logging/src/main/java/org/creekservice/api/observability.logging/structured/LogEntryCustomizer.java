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

package org.creekservice.api.observability.logging.structured;

/**
 * Log entry customization.
 *
 * <p>The methods on this type can be used to customize a log entry.
 */
public interface LogEntryCustomizer {

    /**
     * Nest the log entry within a namespace.
     *
     * <p>Namespacing log entries can make it easier to filter logs.
     *
     * <p>The method can be called multiple times to nest within multiple namespace.
     *
     * @param namespace the name of the namespace.
     * @return self.
     */
    LogEntryCustomizer ns(String namespace);

    /**
     * Nest the log entry with a namespace
     *
     * @param namespace the namespace
     * @return self
     * @see #ns(String)
     */
    default LogEntryCustomizer ns(final Enum<?> namespace) {
        return ns(namespace.name());
    }

    /**
     * Attach a key-value pair to the log entry.
     *
     * <p>Use this method to attach metrics or values to the log entry. Such metrics will be output
     * in a standard format, making them much more machine-readable.
     *
     * @param key the name of the metric / value.
     * @param value the value to log, converted to a string via {@code toString()}. Null values are
     *     ignored.
     * @return self
     */
    LogEntryCustomizer with(String key, Object value);

    /**
     * Attach a key-value pair to the log entry.
     *
     * @param key the entry key
     * @param value the entry value
     * @return self
     * @see #with(String, Object)
     */
    default LogEntryCustomizer with(final Enum<?> key, final Object value) {
        return with(key.name(), value);
    }

    /**
     * Attach an exception or error to the log entry.
     *
     * <p>The throwable will be passed down to the underlying logging system. This normally results
     * in the stack trace being included in the log entry.
     *
     * @param t the throwable to attach.
     * @return self
     */
    LogEntryCustomizer withThrowable(Throwable t);
}
