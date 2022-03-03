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

package org.creek.api.observability.logging.structured;


import java.util.function.Consumer;

/** Logger interface that encourages more structured logging. */
public interface StructuredLogger {

    Consumer<LogEntryCustomizer> NO_OP_CONSUMER = customizer -> {};

    /**
     * Log a trace level message, if level is enabled.
     *
     * @param message the message to log
     */
    default void trace(String message) {
        trace(message, NO_OP_CONSUMER);
    }

    /**
     * Log a trace level message, if level is enabled.
     *
     * @param message the message to log
     * @param customizer consumer called, if level is enabled, to allow customization of the log
     *     entry
     */
    default void trace(String message, Consumer<LogEntryCustomizer> customizer) {
        log(Level.TRACE, message, customizer);
    }

    /**
     * Log a debug level message, if level is enabled.
     *
     * @param message the message to log
     */
    default void debug(String message) {
        debug(message, NO_OP_CONSUMER);
    }

    /**
     * Log a debug level message, if level is enabled.
     *
     * @param message the message to log
     * @param customizer consumer called, if level is enabled, to allow customization of the log
     *     entry
     */
    default void debug(String message, Consumer<LogEntryCustomizer> customizer) {
        log(Level.DEBUG, message, customizer);
    }

    /**
     * Log a info level message, if level is enabled.
     *
     * @param message the message to log
     */
    default void info(String message) {
        info(message, NO_OP_CONSUMER);
    }

    /**
     * Log a info level message, if level is enabled.
     *
     * @param message the message to log
     * @param customizer consumer called, if level is enabled, to allow customization of the log
     *     entry
     */
    default void info(String message, Consumer<LogEntryCustomizer> customizer) {
        log(Level.INFO, message, customizer);
    }

    /**
     * Log a warn level message, if level is enabled.
     *
     * @param message the message to log
     */
    default void warn(String message) {
        warn(message, NO_OP_CONSUMER);
    }

    /**
     * Log a warn level message, if level is enabled.
     *
     * @param message the message to log
     * @param customizer consumer called, if level is enabled, to allow customization of the log
     *     entry
     */
    default void warn(String message, Consumer<LogEntryCustomizer> customizer) {
        log(Level.WARN, message, customizer);
    }

    /**
     * Log a error level message, if level is enabled
     *
     * @param message the message to log
     */
    default void error(String message) {
        error(message, NO_OP_CONSUMER);
    }

    /**
     * Log a error level message, if level is enabled.
     *
     * @param message the message to log
     * @param customizer consumer called, if level is enabled, to allow customization of the log
     *     entry
     */
    default void error(String message, Consumer<LogEntryCustomizer> customizer) {
        log(Level.ERROR, message, customizer);
    }

    /**
     * Log a {@code message} at the supplied {@code level}, if level is enabled.
     *
     * @param level the level to log at
     * @param message the message to log
     */
    default void log(Level level, String message) {
        log(level, message, NO_OP_CONSUMER);
    }

    /**
     * Log a fatal level message, if fatal level is enabled.
     *
     * @param level the level to log at
     * @param message the message to log
     * @param customizer consumer called, if level is enabled, to allow customization of the log
     *     entry
     */
    void log(Level level, String message, Consumer<LogEntryCustomizer> customizer);
}
