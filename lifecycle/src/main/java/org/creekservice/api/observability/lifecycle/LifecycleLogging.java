/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.observability.lifecycle;

/**
 * Util class for building standardized lifecycle log messages.
 *
 * <p>Useful if you need to extend {@link BasicLifecycle}.
 */
public final class LifecycleLogging {

    private LifecycleLogging() {}

    /**
     * Build a lifecycle log message
     *
     * @param targetType the type of thing that the lifecycle pertains to, e.g. a service.
     * @param event the event that has happened.
     * @return the log message
     */
    public static String lifecycleLogMessage(final String targetType, final Enum<?> event) {
        return "creek.lifecycle." + targetType.toLowerCase() + "." + event.name().toLowerCase();
    }
}
