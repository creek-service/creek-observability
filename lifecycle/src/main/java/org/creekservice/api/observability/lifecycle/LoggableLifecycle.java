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
 * A lifecycle that can be logged.
 *
 * <p>The main purpose of this type is to provide the common glue between what a service/application
 * should log to indicate it has started and the Creek system tests, which must wait for the log
 * message to know the service is ready.
 *
 * <p>The system tests look for a log line containing {@code creek.lifecycle.service.started} to
 * determine a service has started. This can be obtained via from {@link BasicLifecycle} via {@code
 * BasicLifecycle.started.logMessage(LoggableLifecycle.SERVICE_TYPE)}
 */
public interface LoggableLifecycle {

    /**
     * A common identifier for service instance lifecycle events, as expected by the system tests.
     */
    String SERVICE_TYPE = "service";

    /**
     * Obtain a standardized log message for the lifecycle event.
     *
     * @param targetType the type of the target, i.e. the type of the object that is going through
     *     the lifecycle, e.g. {@code service}.
     * @return the standardized message to log.
     */
    String logMessage(String targetType);
}
