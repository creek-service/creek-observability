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

import static org.creekservice.api.observability.logging.structured.Level.DEBUG;
import static org.creekservice.api.observability.logging.structured.Level.ERROR;
import static org.creekservice.api.observability.logging.structured.Level.INFO;
import static org.creekservice.api.observability.logging.structured.Level.TRACE;
import static org.creekservice.api.observability.logging.structured.Level.WARN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.creekservice.api.observability.logging.structured.Level;
import org.creekservice.api.observability.logging.structured.LogEntryCustomizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Slf4jStructuredLoggerTest {

    private static final Consumer<LogEntryCustomizer> NO_CUSTOMIZATION = entry -> {};
    private static final Throwable NO_THROWABLE = null;
    private static final Throwable THROWABLE = new OutOfMemoryError();
    private static final Map<String, Object> LOG_STATE = Map.of("log", "state");
    private static final String LOG_LINE = "some log line";

    @Mock private Logger slf4jLogger;
    @Mock private Function<String, DefaultLogEntryCustomizer> customizerFactory;
    @Mock private DefaultLogEntryCustomizer customizer;
    @Mock private LogEntryFormatter formatter;
    private Slf4jStructuredLogger logger;

    @BeforeEach
    void setUp() {
        logger =
                new Slf4jStructuredLogger(
                        slf4jLogger, Optional.empty(), customizerFactory, formatter);

        when(customizerFactory.apply(any())).thenReturn(customizer);
        when(customizer.build(anyBoolean())).thenReturn(LOG_STATE);
        when(formatter.format(any())).thenReturn(LOG_LINE);
    }

    @ParameterizedTest
    @EnumSource(Level.class)
    void shouldNotLogIfDisabled(final Level level) {
        // Given:
        when(customizer.throwable()).thenReturn(Optional.of(THROWABLE));

        // When:
        logger.log(level, "msg");

        // Then:
        verify(slf4jLogger, never()).trace(any(String.class), any(Throwable.class));
        verify(slf4jLogger, never()).debug(any(String.class), any(Throwable.class));
        verify(slf4jLogger, never()).info(any(String.class), any(Throwable.class));
        verify(slf4jLogger, never()).warn(any(String.class), any(Throwable.class));
        verify(slf4jLogger, never()).error(any(String.class), any(Throwable.class));
    }

    @ParameterizedTest
    @EnumSource(Level.class)
    void shouldNotCreateCustomizerIfNotEnabled(final Level level) {
        // When:
        logger.log(level, "msg", NO_CUSTOMIZATION);

        // Then:
        verify(customizerFactory, never()).apply(any());
    }

    @ParameterizedTest
    @EnumSource(Level.class)
    void shouldNotInvokeFormatterIfNotEnabled(final Level level) {
        // When:
        logger.log(level, "msg", NO_CUSTOMIZATION);

        // Then:
        verify(formatter, never()).format(any());
    }

    @ParameterizedTest
    @EnumSource(Level.class)
    void shouldCreateCustomizerIfEnabled(final Level level) {
        // Given:
        givenEnabled(level);

        // When:
        logger.log(level, "msg", NO_CUSTOMIZATION);

        // Then:
        verify(customizerFactory).apply("msg");
    }

    @ParameterizedTest
    @EnumSource(Level.class)
    void shouldInvokeCustomizerIfEnabled(final Level level) {
        // Given:
        givenEnabled(level);

        // When:
        logger.log(level, "msg", entry -> entry.withThrowable(THROWABLE));

        // Then:
        verify(customizer).withThrowable(THROWABLE);
    }

    @ParameterizedTest
    @EnumSource(Level.class)
    void shouldInvokeFormatterIfEnabled(final Level level) {
        // Given:
        givenEnabled(level);

        // When:
        logger.log(level, "msg", entry -> entry.withThrowable(THROWABLE));

        // Then:
        verify(formatter).format(LOG_STATE);
    }

    @Test
    void shouldLogTraceIfEnabled() {
        // Given:
        when(slf4jLogger.isTraceEnabled()).thenReturn(true);

        // When:
        logger.log(TRACE, "msg");

        // Then:
        verify(slf4jLogger).trace(LOG_LINE, NO_THROWABLE);
    }

    @Test
    void shouldLogTraceWithThrowableIfEnabled() {
        // Given:
        when(slf4jLogger.isTraceEnabled()).thenReturn(true);
        when(customizer.throwable()).thenReturn(Optional.of(THROWABLE));

        // When:
        logger.log(TRACE, "msg");

        // Then:
        verify(slf4jLogger).trace(LOG_LINE, THROWABLE);
    }

    @Test
    void shouldLogDebugIfEnabled() {
        // Given:
        when(slf4jLogger.isDebugEnabled()).thenReturn(true);

        // When:
        logger.log(DEBUG, "msg");

        // Then:
        verify(slf4jLogger).debug(LOG_LINE, NO_THROWABLE);
    }

    @Test
    void shouldLogDebugWithThrowableIfEnabled() {
        // Given:
        when(slf4jLogger.isDebugEnabled()).thenReturn(true);
        when(customizer.throwable()).thenReturn(Optional.of(THROWABLE));

        // When:
        logger.log(DEBUG, "msg");

        // Then:
        verify(slf4jLogger).debug(LOG_LINE, THROWABLE);
    }

    @Test
    void shouldLogInfoIfEnabled() {
        // Given:
        when(slf4jLogger.isInfoEnabled()).thenReturn(true);

        // When:
        logger.log(INFO, "msg");

        // Then:
        verify(slf4jLogger).info(LOG_LINE, NO_THROWABLE);
    }

    @Test
    void shouldLogInfoWithThrowableIfEnabled() {
        // Given:
        when(slf4jLogger.isInfoEnabled()).thenReturn(true);
        when(customizer.throwable()).thenReturn(Optional.of(THROWABLE));

        // When:
        logger.log(INFO, "msg");

        // Then:
        verify(slf4jLogger).info(LOG_LINE, THROWABLE);
    }

    @Test
    void shouldLogWarnIfEnabled() {
        // Given:
        when(slf4jLogger.isWarnEnabled()).thenReturn(true);

        // When:
        logger.log(WARN, "msg");

        // Then:
        verify(slf4jLogger).warn(LOG_LINE, NO_THROWABLE);
    }

    @Test
    void shouldLogWarnWithThrowableIfEnabled() {
        // Given:
        when(slf4jLogger.isWarnEnabled()).thenReturn(true);
        when(customizer.throwable()).thenReturn(Optional.of(THROWABLE));

        // When:
        logger.log(WARN, "msg");

        // Then:
        verify(slf4jLogger).warn(LOG_LINE, THROWABLE);
    }

    @Test
    void shouldLogErrorIfEnabled() {
        // Given:
        when(slf4jLogger.isErrorEnabled()).thenReturn(true);

        // When:
        logger.log(ERROR, "msg");

        // Then:
        verify(slf4jLogger).error(LOG_LINE, NO_THROWABLE);
    }

    @Test
    void shouldLogErrorWithThrowableIfEnabled() {
        // Given:
        when(slf4jLogger.isErrorEnabled()).thenReturn(true);
        when(customizer.throwable()).thenReturn(Optional.of(THROWABLE));

        // When:
        logger.log(ERROR, "msg");

        // Then:
        verify(slf4jLogger).error(LOG_LINE, THROWABLE);
    }

    @Test
    void shouldAddRootNs() {
        // Given:
        logger =
                new Slf4jStructuredLogger(
                        slf4jLogger, Optional.of("rootNs"), customizerFactory, formatter);
        givenEnabled(Level.INFO);
        final DefaultLogEntryCustomizer nestedCustomizer = mock(DefaultLogEntryCustomizer.class);
        when(customizer.ns("rootNs")).thenReturn(nestedCustomizer);

        // When:
        logger.log(Level.INFO, "msg", customizer -> customizer.with("a", 1));

        // Then:
        verify(customizer).ns("rootNs");
        verify(nestedCustomizer).with("a", 1);
    }

    private void givenEnabled(final Level level) {
        switch (level) {
            case TRACE:
                when(slf4jLogger.isTraceEnabled()).thenReturn(true);
                break;
            case DEBUG:
                when(slf4jLogger.isDebugEnabled()).thenReturn(true);
                break;
            case INFO:
                when(slf4jLogger.isInfoEnabled()).thenReturn(true);
                break;
            case WARN:
                when(slf4jLogger.isWarnEnabled()).thenReturn(true);
                break;
            case ERROR:
                when(slf4jLogger.isErrorEnabled()).thenReturn(true);
                break;
            default:
                throw new IllegalArgumentException("Unknown level: " + level);
        }
    }
}
