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

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.creekservice.api.base.type.config.SystemProperties;
import org.creekservice.api.base.type.json.Json;

@SuppressWarnings("DuplicatedCode")
final class JsonLogEntryFormatter implements LogEntryFormatter {

    public static final String MAX_DEPTH_PROP =
            "org.creekservice.observability.logging.structured.depth.max";
    private static final char DOUBLE_QUOTE = '"';
    private static final char COMMA = ',';
    private static final char COLON = ':';
    private static final char OBJECT_START = '{';
    private static final char OBJECT_END = '}';
    private static final char ARRAY_START = '[';
    private static final char ARRAY_END = ']';

    private static final List<Handler> HANDLERS =
            List.of(
                    new NullHandler(),
                    new SimpleHandler<>(String.class, JsonLogEntryFormatter::formatString),
                    new SimpleHandler<>(BigDecimal.class, StringBuilder::append),
                    new SimpleHandler<>(Double.class, (sb, o) -> sb.append((double) o)),
                    new SimpleHandler<>(Float.class, (sb, o) -> sb.append((float) o)),
                    new SimpleHandler<>(Byte.class, (sb, o) -> sb.append((byte) o)),
                    new SimpleHandler<>(Short.class, (sb, o) -> sb.append((short) o)),
                    new SimpleHandler<>(Integer.class, (sb, o) -> sb.append((int) o)),
                    new SimpleHandler<>(Long.class, (sb, o) -> sb.append((long) o)),
                    new SimpleHandler<>(Number.class, JsonLogEntryFormatter::formatNumber),
                    new SimpleHandler<>(Boolean.class, (sb, o) -> sb.append((boolean) o)),
                    new ContainerHandler<>(
                            Collection.class, JsonLogEntryFormatter::formatCollection),
                    new ContainerHandler<>(Map.class, JsonLogEntryFormatter::formatMap),
                    new SimpleHandler<>(char[].class, JsonLogEntryFormatter::formatCharArray),
                    new SimpleHandler<>(boolean[].class, JsonLogEntryFormatter::formatBooleanArray),
                    new SimpleHandler<>(byte[].class, JsonLogEntryFormatter::formatByteArray),
                    new SimpleHandler<>(short[].class, JsonLogEntryFormatter::formatShortArray),
                    new SimpleHandler<>(int[].class, JsonLogEntryFormatter::formatIntArray),
                    new SimpleHandler<>(long[].class, JsonLogEntryFormatter::formatLongArray),
                    new SimpleHandler<>(float[].class, JsonLogEntryFormatter::formatFloatArray),
                    new SimpleHandler<>(double[].class, JsonLogEntryFormatter::formatDoubleArray),
                    new ContainerHandler<>(
                            Object[].class, JsonLogEntryFormatter::formatObjectArray),
                    new SimpleHandler<>(Object.class, JsonLogEntryFormatter::formatString));

    private final int maxDepth;

    JsonLogEntryFormatter() {
        this.maxDepth = SystemProperties.getInt(MAX_DEPTH_PROP, 8);
    }

    @Override
    public String format(final Object o) {
        final StringBuilder sb = new StringBuilder();
        format(sb, o, 0, maxDepth);
        return sb.toString();
    }

    private static void format(
            final StringBuilder sb, final Object object, final int depth, final int maxDepth) {
        if (depth > maxDepth) {
            throw new IllegalArgumentException("Max depth of " + maxDepth + " exceeded");
        }

        HANDLERS.stream()
                .filter(h -> h.handles(object))
                .findFirst()
                .orElseThrow(IllegalStateException::new)
                .handle(sb, object, depth, maxDepth);
    }

    private static void formatString(final StringBuilder sb, final Object value) {
        sb.append(DOUBLE_QUOTE);
        final int startIndex = sb.length();
        sb.append(value);
        Json.escapeJson(sb, startIndex);
        sb.append(DOUBLE_QUOTE);
    }

    private static void formatNumber(final StringBuilder sb, final Number number) {
        final long longNumber = number.longValue();
        final double doubleValue = number.doubleValue();
        if (Double.compare(longNumber, doubleValue) == 0) {
            sb.append(longNumber);
        } else {
            sb.append(doubleValue);
        }
    }

    private static void formatCollection(
            final StringBuilder sb,
            final Collection<?> items,
            final int depth,
            final int maxDepth) {
        sb.append(ARRAY_START);

        final boolean[] first = {true};
        items.forEach(
                (final Object item) -> {
                    if (first[0]) {
                        first[0] = false;
                    } else {
                        sb.append(COMMA);
                    }
                    format(sb, item, depth + 1, maxDepth);
                });

        sb.append(ARRAY_END);
    }

    private static void formatMap(
            final StringBuilder sb, final Map<?, ?> map, final int depth, final int maxDepth) {
        sb.append(OBJECT_START);

        final boolean[] first = {true};
        map.forEach(
                (final Object key, final Object value) -> {
                    if (key == null) {
                        throw new IllegalArgumentException(
                                "null key in " + map + " at depth " + depth);
                    }

                    if (first[0]) {
                        first[0] = false;
                    } else {
                        sb.append(COMMA);
                    }

                    appendKeyAndColon(sb, key);

                    format(sb, value, depth + 1, maxDepth);
                });

        sb.append(OBJECT_END);
    }

    private static void formatCharArray(final StringBuilder sb, final char[] items) {
        sb.append(ARRAY_START);
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(COMMA);
            }
            sb.append(DOUBLE_QUOTE);
            final int startIndex = sb.length();
            sb.append(items[i]);
            Json.escapeJson(sb, startIndex);
            sb.append(DOUBLE_QUOTE);
        }
        sb.append(ARRAY_END);
    }

    private static void formatBooleanArray(final StringBuilder sb, final boolean[] items) {
        sb.append(ARRAY_START);
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(COMMA);
            }
            sb.append(items[i]);
        }
        sb.append(ARRAY_END);
    }

    private static void formatByteArray(final StringBuilder sb, final byte[] items) {
        sb.append(ARRAY_START);
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(COMMA);
            }
            sb.append(items[i]);
        }
        sb.append(ARRAY_END);
    }

    private static void formatShortArray(final StringBuilder sb, final short[] items) {
        sb.append(ARRAY_START);
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(COMMA);
            }
            sb.append(items[i]);
        }
        sb.append(ARRAY_END);
    }

    private static void formatIntArray(final StringBuilder sb, final int[] items) {
        sb.append(ARRAY_START);
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(COMMA);
            }
            sb.append(items[i]);
        }
        sb.append(ARRAY_END);
    }

    private static void formatLongArray(final StringBuilder sb, final long[] items) {
        sb.append(ARRAY_START);
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(COMMA);
            }
            sb.append(items[i]);
        }
        sb.append(ARRAY_END);
    }

    private static void formatFloatArray(final StringBuilder sb, final float[] items) {
        sb.append(ARRAY_START);
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(COMMA);
            }
            sb.append(items[i]);
        }
        sb.append(ARRAY_END);
    }

    private static void formatDoubleArray(final StringBuilder sb, final double[] items) {
        sb.append(ARRAY_START);
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(COMMA);
            }
            sb.append(items[i]);
        }
        sb.append(ARRAY_END);
    }

    private static void formatObjectArray(
            final StringBuilder sb, final Object[] items, final int depth, final int maxDepth) {
        sb.append(ARRAY_START);
        final int nextDepth = depth + 1;
        for (int i = 0; i < items.length; i++) {
            if (i > 0) {
                sb.append(COMMA);
            }
            format(sb, items[i], nextDepth, maxDepth);
        }
        sb.append(ARRAY_END);
    }

    private static void appendKeyAndColon(final StringBuilder sb, final Object key) {
        sb.append(DOUBLE_QUOTE);
        final String text = String.valueOf(key);
        final int keyStartIndex = sb.length();
        sb.append(text);

        Json.escapeJson(sb, keyStartIndex);
        sb.append(DOUBLE_QUOTE).append(COLON);
    }

    private interface Handler {
        boolean handles(Object object);

        void handle(StringBuilder sb, Object object, int depth, int maxDepth);
    }

    private static class NullHandler implements Handler {
        @Override
        public boolean handles(final Object object) {
            return object == null;
        }

        @Override
        public void handle(
                final StringBuilder sb, final Object object, final int depth, final int maxDepth) {
            sb.append("null");
        }
    }

    private static final class SimpleHandler<T> implements Handler {

        private final Class<T> type;
        private final BiConsumer<StringBuilder, T> handle;

        SimpleHandler(final Class<T> type, final BiConsumer<StringBuilder, T> handle) {
            this.type = requireNonNull(type, "type");
            this.handle = requireNonNull(handle, "handle");
        }

        @Override
        public boolean handles(final Object object) {
            return type.isInstance(object);
        }

        @Override
        public void handle(
                final StringBuilder sb, final Object object, final int depth, final int maxDepth) {
            final T t = type.cast(object);
            handle.accept(sb, t);
        }
    }

    private static final class ContainerHandler<T> implements Handler {

        private final Class<T> type;
        private final HandleFunc<T> handle;

        ContainerHandler(final Class<T> type, final HandleFunc<T> handle) {
            this.type = requireNonNull(type, "type");
            this.handle = requireNonNull(handle, "handle");
        }

        @Override
        public boolean handles(final Object object) {
            return type.isInstance(object);
        }

        @Override
        public void handle(
                final StringBuilder sb, final Object object, final int depth, final int maxDepth) {
            final T t = type.cast(object);
            handle.accept(sb, t, depth, maxDepth);
        }

        private interface HandleFunc<T> {
            void accept(StringBuilder sb, T object, int depth, int maxDepth);
        }
    }
}
