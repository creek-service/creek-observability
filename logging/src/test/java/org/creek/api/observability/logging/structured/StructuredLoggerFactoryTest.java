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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import org.creek.internal.observability.logging.structured.Slf4jStructuredLogger;
import org.junit.jupiter.api.Test;

class StructuredLoggerFactoryTest {

    @Test
    void shouldConstructStructuredLog4jLogger() {
        // When:
        final StructuredLogger logger =
                StructuredLoggerFactory.logger(StructuredLoggerFactoryTest.class);

        // Then:
        assertThat(logger, is(instanceOf(Slf4jStructuredLogger.class)));
    }

    @Test
    void shouldConstructInternalLog4jLogger() {
        // When:
        final StructuredLogger logger =
                StructuredLoggerFactory.internalLogger(StructuredLoggerFactoryTest.class);

        // Then:
        assertThat(logger, is(instanceOf(Slf4jStructuredLogger.class)));
    }
}
