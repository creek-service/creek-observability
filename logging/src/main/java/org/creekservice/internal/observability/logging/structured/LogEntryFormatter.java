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

package org.creekservice.internal.observability.logging.structured;

import java.util.Map;

/** A formatter of log entries. */
interface LogEntryFormatter {

    /**
     * @return {@code true} if any throwable should be included in the message payload in a `cause`
     *     field, rather than passed to the underlying logging framework.
     */
    boolean causeInMessage();

    /**
     * Called to format a log entry.
     *
     * @param logEntry the log entry to format.
     * @return the formatted data.
     */
    String format(Map<String, ?> logEntry);
}
