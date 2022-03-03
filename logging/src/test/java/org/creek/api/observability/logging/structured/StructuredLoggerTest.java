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

import static org.creek.api.observability.logging.structured.Level.DEBUG;
import static org.creek.api.observability.logging.structured.Level.ERROR;
import static org.creek.api.observability.logging.structured.Level.INFO;
import static org.creek.api.observability.logging.structured.Level.TRACE;
import static org.creek.api.observability.logging.structured.Level.WARN;
import static org.creek.api.observability.logging.structured.StructuredLogger.NO_OP_CONSUMER;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StructuredLoggerTest {

    @Mock private LogMethod logMethod;
    @Mock private Consumer<LogEntryCustomizer> customizer;
    private final TestLogger logger = new TestLogger();

    @Test
    void shouldLogAtTraceWithoutCustomization() {
        // When:
        logger.trace("msg");

        // Then:
        verify(logMethod).log(TRACE, "msg", NO_OP_CONSUMER);
    }

    @Test
    void shouldLogAtTraceWithCustomization() {
        // When:
        logger.trace("msg", customizer);

        // Then:
        verify(logMethod).log(TRACE, "msg", customizer);
    }

    @Test
    void shouldLogAtDebugWithoutCustomization() {
        // When:
        logger.debug("msg");

        // Then:
        verify(logMethod).log(DEBUG, "msg", NO_OP_CONSUMER);
    }

    @Test
    void shouldLogAtDebugWithCustomization() {
        // When:
        logger.debug("msg", customizer);

        // Then:
        verify(logMethod).log(DEBUG, "msg", customizer);
    }

    @Test
    void shouldLogAtInfoWithoutCustomization() {
        // When:
        logger.info("msg");

        // Then:
        verify(logMethod).log(INFO, "msg", NO_OP_CONSUMER);
    }

    @Test
    void shouldLogAtInfoWithCustomization() {
        // When:
        logger.info("msg", customizer);

        // Then:
        verify(logMethod).log(INFO, "msg", customizer);
    }

    @Test
    void shouldLogAtWarnWithoutCustomization() {
        // When:
        logger.warn("msg");

        // Then:
        verify(logMethod).log(WARN, "msg", NO_OP_CONSUMER);
    }

    @Test
    void shouldLogAtWarnWithCustomization() {
        // When:
        logger.warn("msg", customizer);

        // Then:
        verify(logMethod).log(WARN, "msg", customizer);
    }

    @Test
    void shouldLogAtErrorWithoutCustomization() {
        // When:
        logger.error("msg");

        // Then:
        verify(logMethod).log(ERROR, "msg", NO_OP_CONSUMER);
    }

    @Test
    void shouldLogAtErrorWithCustomization() {
        // When:
        logger.error("msg", customizer);

        // Then:
        verify(logMethod).log(ERROR, "msg", customizer);
    }

    @Test
    void shouldLogAtLevelWithoutCustomization() {
        // When:
        logger.log(INFO, "msg");

        // Then:
        verify(logMethod).log(INFO, "msg", NO_OP_CONSUMER);
    }

    private interface LogMethod {
        void log(Level level, String message, Consumer<LogEntryCustomizer> customizer);
    }

    private final class TestLogger implements StructuredLogger {

        @Override
        public void log(
                final Level level,
                final String message,
                final Consumer<LogEntryCustomizer> customizer) {
            logMethod.log(level, message, customizer);
        }
    }
}
