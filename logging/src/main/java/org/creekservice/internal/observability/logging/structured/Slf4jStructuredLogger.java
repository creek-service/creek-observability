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

package org.creekservice.internal.observability.logging.structured;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.observability.logging.structured.Level;
import org.creekservice.api.observability.logging.structured.LogEntryCustomizer;
import org.creekservice.api.observability.logging.structured.StructuredLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Structured logger implementation for slf4j. */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class Slf4jStructuredLogger implements StructuredLogger {

    private static final Slf4JLLevel[] SLF4J_LEVELS = {
        new Slf4JLLevel(Logger::isTraceEnabled, Logger::trace),
        new Slf4JLLevel(Logger::isDebugEnabled, Logger::debug),
        new Slf4JLLevel(Logger::isInfoEnabled, Logger::info),
        new Slf4JLLevel(Logger::isWarnEnabled, Logger::warn),
        new Slf4JLLevel(Logger::isErrorEnabled, Logger::error)
    };

    private final Logger logger;
    private final Optional<String> rootNs;
    private final Function<String, DefaultLogEntryCustomizer> customizerFactory;
    private final LogEntryFormatter formatter;

    public Slf4jStructuredLogger(final Class<?> clazz, final Optional<String> rootNs) {
        this(
                LoggerFactory.getLogger(clazz),
                rootNs,
                DefaultLogEntryCustomizer::create,
                new JsonLogEntryFormatter());
    }

    @VisibleForTesting
    Slf4jStructuredLogger(
            final Logger logger,
            final Optional<String> rootNs,
            final Function<String, DefaultLogEntryCustomizer> customizerFactory,
            final LogEntryFormatter formatter) {
        this.logger = requireNonNull(logger, "logger");
        this.rootNs = requireNonNull(rootNs, "rootNs");
        this.customizerFactory = requireNonNull(customizerFactory, "customizerFactory");
        this.formatter = requireNonNull(formatter, "formatter");
    }

    @Override
    public void log(
            final Level level,
            final String message,
            final Consumer<LogEntryCustomizer> customizeConsumer) {
        final Slf4JLLevel slf4jLevel = SLF4J_LEVELS[level.ordinal()];
        if (slf4jLevel.disabled(logger)) {
            return;
        }

        final DefaultLogEntryCustomizer customizer = customizerFactory.apply(message);
        customizeConsumer.accept(rootNs.map(customizer::ns).orElse(customizer));

        slf4jLevel.log(
                logger, formatter.format(customizer.build()), customizer.throwable().orElse(null));
    }

    private interface EnabledMethod {
        boolean enabled(Logger logger);
    }

    private interface LogMethod {
        void log(Logger logger, String message, Throwable throwable);
    }

    private static final class Slf4JLLevel {

        final EnabledMethod enabledMethod;
        final LogMethod logMethod;

        Slf4JLLevel(final EnabledMethod enabledMethod, final LogMethod logMethod) {
            this.enabledMethod = requireNonNull(enabledMethod, "enabledMethod");
            this.logMethod = requireNonNull(logMethod, "logMethod");
        }

        boolean disabled(final Logger logger) {
            return !enabledMethod.enabled(logger);
        }

        void log(final Logger logger, final String message, final Throwable throwable) {
            logMethod.log(logger, message, throwable);
        }
    }
}
