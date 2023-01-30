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

package org.creekservice.api.observability.logging.structured;

import java.util.Optional;
import org.creekservice.internal.observability.logging.structured.Slf4jStructuredLogger;

/** Factory class for loggers. */
public final class StructuredLoggerFactory {

    private StructuredLoggerFactory() {}

    /**
     * Get a StructuredLogger for the given {@code clazz}.
     *
     * @param clazz the class requiring a logger
     * @return the class's logger
     */
    public static StructuredLogger logger(final Class<?> clazz) {
        return new Slf4jStructuredLogger(clazz, Optional.empty());
    }

    /**
     * Get a StructuredLogger for use by Creek's internal code.
     *
     * @param clazz the class requiring a logger
     * @return the class's logger
     */
    public static StructuredLogger internalLogger(final Class<?> clazz) {
        return new Slf4jStructuredLogger(clazz, Optional.of("creek"));
    }
}
