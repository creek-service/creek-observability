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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonLogEntryFormatterTest {

    private static final BigDecimal DECIMAL = new BigDecimal("1.35e-40");
    private static final double DOUBLE = Double.MAX_VALUE;
    private static final float FLOAT = Float.MIN_VALUE;
    private static final byte BYTE = Byte.MAX_VALUE;
    private static final short SHORT = Short.MAX_VALUE;
    private static final int INT = Integer.MIN_VALUE;
    private static final long LONG = Long.MAX_VALUE;
    private static final boolean BOOLEAN = true;

    private JsonLogEntryFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new JsonLogEntryFormatter();
    }

    @Test
    void shouldNull() {
        assertThat(formatter.formatInternal(null), is("null"));
    }

    @Test
    void shouldFormatString() {
        assertThat(formatter.formatInternal("some\b\"\n\ttext"), is("\"some\\b\\\"\\n\\ttext\""));
    }

    @Test
    void shouldFormatBigDecimal() {
        assertThat(formatter.formatInternal(DECIMAL), is("1.35E-40"));
    }

    @Test
    void shouldFormatDouble() {
        assertThat(formatter.formatInternal(DOUBLE), is("1.7976931348623157E308"));
    }

    @Test
    void shouldFormatFloat() {
        assertThat(formatter.formatInternal(FLOAT), is("1.4E-45"));
    }

    @Test
    void shouldFormatByte() {
        assertThat(formatter.formatInternal(BYTE), is("127"));
    }

    @Test
    void shouldFormatShort() {
        assertThat(formatter.formatInternal(SHORT), is("32767"));
    }

    @Test
    void shouldFormatInt() {
        assertThat(formatter.formatInternal(INT), is("-2147483648"));
    }

    @Test
    void shouldFormatLong() {
        assertThat(formatter.formatInternal(LONG), is("9223372036854775807"));
    }

    @Test
    void shouldFormatOtherIntegerNumber() {
        assertThat(formatter.formatInternal(new AtomicInteger(INT)), is("-2147483648"));
    }

    @Test
    void shouldFormatOtherDoubleNumber() {
        // Given:
        final DoubleAdder val = new DoubleAdder();
        val.add(DOUBLE);

        // Then:
        assertThat(formatter.formatInternal(val), is("1.7976931348623157E308"));
    }

    @Test
    void shouldFormatThrowable() {
        // Given:
        final Throwable e = new OutOfMemoryError("some message");

        // When:
        final String actual = formatter.formatInternal(e);

        // Then:
        assertThat(
                actual,
                startsWith(
                        "\"java.lang.OutOfMemoryError: some message\\n"
                                + "\\tat creek.observability.logging@"));
        assertThat(
                actual,
                containsString(
                        "/org.creekservice.internal.observability.logging.structured"
                            + ".JsonLogEntryFormatterTest.shouldFormatThrowable(JsonLogEntryFormatterTest.java:"));
        assertThat(actual, endsWith("\""));
    }

    @Test
    void shouldEscapeQuotesInThrowable() {
        // Given:
        final Throwable e = new OutOfMemoryError("some message with \"quoted text\"");

        // When:
        final String actual = formatter.formatInternal(e);

        // Then:
        assertThat(
                actual,
                startsWith("\"java.lang.OutOfMemoryError: some message with \\\"quoted text\\\""));
    }

    @Test
    void shouldFormatBoolean() {
        assertThat(formatter.formatInternal(BOOLEAN), is("true"));
    }

    @Test
    void shouldFormatCollection() {
        assertThat(formatter.formatInternal(List.of(BOOLEAN, BYTE)), is("[true,127]"));
    }

    @Test
    void shouldFormatNestedWithInCollection() {
        assertThat(formatter.formatInternal(List.of(Map.of("a", BOOLEAN))), is("[{\"a\":true}]"));
    }

    @Test
    void shouldFormatMap() {
        assertThat(
                formatter.formatInternal(new TreeMap<>(Map.of("a", BOOLEAN, "b", BYTE))),
                is("{\"a\":true,\"b\":127}"));
    }

    @Test
    void shouldEscapeMapKeys() {
        assertThat(formatter.format(Map.of("a\nb", BOOLEAN)), is("{\"a\\nb\":true}"));
    }

    @Test
    void shouldConvertMapKeysToStrings() {
        assertThat(formatter.formatInternal(Map.of(10, BOOLEAN)), is("{\"10\":true}"));
    }

    @Test
    void shouldFormatNestedWithInMap() {
        assertThat(formatter.format(Map.of("a", List.of(BYTE))), is("{\"a\":[127]}"));
    }

    @Test
    void shouldThrowOnNullMapKey() {
        // Given:
        final Map<String, Object> nullKeyMap = new HashMap<>();
        nullKeyMap.put(null, 10);

        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> formatter.format(nullKeyMap));

        // Then:
        assertThat(e.getMessage(), is("null key in {null=10} at depth 0"));
    }

    @Test
    void shouldFormatCharArray() {
        assertThat(formatter.formatInternal(new char[] {}), is("[]"));
        assertThat(formatter.formatInternal(new char[] {'a'}), is("[\"a\"]"));
        assertThat(formatter.formatInternal(new char[] {'a', 'b'}), is("[\"a\",\"b\"]"));
        assertThat(formatter.formatInternal(new char[] {'a', '\b'}), is("[\"a\",\"\\b\"]"));
    }

    @Test
    void shouldFormatBooleanArray() {
        assertThat(formatter.formatInternal(new boolean[] {}), is("[]"));
        assertThat(formatter.formatInternal(new boolean[] {true}), is("[true]"));
        assertThat(formatter.formatInternal(new boolean[] {true, false}), is("[true,false]"));
    }

    @Test
    void shouldFormatByteArray() {
        assertThat(formatter.formatInternal(new byte[] {}), is("[]"));
        assertThat(formatter.formatInternal(new byte[] {10}), is("[10]"));
        assertThat(
                formatter.formatInternal(new byte[] {Byte.MIN_VALUE, Byte.MAX_VALUE}),
                is("[-128,127]"));
    }

    @Test
    void shouldFormatShortArray() {
        assertThat(formatter.formatInternal(new short[] {}), is("[]"));
        assertThat(formatter.formatInternal(new short[] {10}), is("[10]"));
        assertThat(
                formatter.formatInternal(new short[] {Short.MIN_VALUE, Short.MAX_VALUE}),
                is("[-32768,32767]"));
    }

    @Test
    void shouldFormatIntArray() {
        assertThat(formatter.formatInternal(new int[] {}), is("[]"));
        assertThat(formatter.formatInternal(new int[] {10}), is("[10]"));
        assertThat(
                formatter.formatInternal(new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE}),
                is("[-2147483648,2147483647]"));
    }

    @Test
    void shouldFormatLongArray() {
        assertThat(formatter.formatInternal(new long[] {}), is("[]"));
        assertThat(formatter.formatInternal(new long[] {10}), is("[10]"));
        assertThat(
                formatter.formatInternal(new long[] {Long.MIN_VALUE, Long.MAX_VALUE}),
                is("[-9223372036854775808,9223372036854775807]"));
    }

    @Test
    void shouldFormatFloatArray() {
        assertThat(formatter.formatInternal(new float[] {}), is("[]"));
        assertThat(formatter.formatInternal(new float[] {10}), is("[10.0]"));
        assertThat(
                formatter.formatInternal(new float[] {Float.MIN_VALUE, Float.MAX_VALUE}),
                is("[1.4E-45,3.4028235E38]"));
    }

    @Test
    void shouldFormatDoubleArray() {
        assertThat(formatter.formatInternal(new double[] {}), is("[]"));
        assertThat(formatter.formatInternal(new double[] {10}), is("[10.0]"));
        assertThat(
                formatter.formatInternal(new double[] {Double.MIN_VALUE, Double.MAX_VALUE}),
                is("[4.9E-324,1.7976931348623157E308]"));
    }

    @Test
    void shouldFormatObjectArray() {
        assertThat(formatter.formatInternal(new Object[] {}), is("[]"));
        assertThat(formatter.formatInternal(new Object[] {10}), is("[10]"));
        assertThat(
                formatter.formatInternal(new Object[] {new AtomicInteger(10), "hello\n"}),
                is("[10,\"hello\\n\"]"));
    }

    @Test
    void shouldThrowIfMaxDepthExceeded() {
        // Given:
        final Map<String, ?> tooDeeplyNested =
                Map.of(
                        "1",
                        Map.of(
                                "2",
                                Map.of(
                                        "3",
                                        Map.of(
                                                "4",
                                                Map.of(
                                                        "5",
                                                        Map.of(
                                                                "6",
                                                                Map.of(
                                                                        "7",
                                                                        Map.of(
                                                                                "8",
                                                                                Map.of(
                                                                                        "9",
                                                                                        1)))))))));

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class, () -> formatter.format(tooDeeplyNested));

        // Then:
        assertThat(e.getMessage(), is("Max depth of 8 exceeded"));
    }

    @Test
    void shouldNotThrowIfMaxDepthMatched() {
        // Given:
        final Map<String, ?> notTooDeeplyNested =
                Map.of(
                        "1",
                        Map.of(
                                "2",
                                Map.of(
                                        "3",
                                        Map.of(
                                                "4",
                                                Map.of(
                                                        "5",
                                                        Map.of(
                                                                "6",
                                                                Map.of("7", Map.of("8", 1))))))));

        // When:
        formatter.formatInternal(notTooDeeplyNested);

        // Then: did not throw
    }
}
