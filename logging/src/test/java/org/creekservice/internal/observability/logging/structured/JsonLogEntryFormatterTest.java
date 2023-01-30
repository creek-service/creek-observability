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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

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

    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    @BeforeEach
    void setUp() {
        JsonLogEntryFormatter.maxDepth = -1;
        formatter = new JsonLogEntryFormatter();
    }

    @Test
    void shouldNull() {
        assertThat(formatter.format(null), is("null"));
    }

    @Test
    void shouldFormatString() {
        assertThat(formatter.format("some\btext"), is("\"some\\btext\""));
    }

    @Test
    void shouldFormatBigDecimal() {
        assertThat(formatter.format(DECIMAL), is("1.35E-40"));
    }

    @Test
    void shouldFormatDouble() {
        assertThat(formatter.format(DOUBLE), is("1.7976931348623157E308"));
    }

    @Test
    void shouldFormatFloat() {
        assertThat(formatter.format(FLOAT), is("1.4E-45"));
    }

    @Test
    void shouldFormatByte() {
        assertThat(formatter.format(BYTE), is("127"));
    }

    @Test
    void shouldFormatShort() {
        assertThat(formatter.format(SHORT), is("32767"));
    }

    @Test
    void shouldFormatInt() {
        assertThat(formatter.format(INT), is("-2147483648"));
    }

    @Test
    void shouldFormatLong() {
        assertThat(formatter.format(LONG), is("9223372036854775807"));
    }

    @Test
    void shouldFormatOtherIntegerNumber() {
        assertThat(formatter.format(new AtomicInteger(INT)), is("-2147483648"));
    }

    @Test
    void shouldFormatOtherDoubleNumber() {
        // Given:
        final DoubleAdder val = new DoubleAdder();
        val.add(DOUBLE);

        // Then:
        assertThat(formatter.format(val), is("1.7976931348623157E308"));
    }

    @Test
    void shouldFormatBoolean() {
        assertThat(formatter.format(BOOLEAN), is("true"));
    }

    @Test
    void shouldFormatCollection() {
        assertThat(formatter.format(List.of(BOOLEAN, BYTE)), is("[true,127]"));
    }

    @Test
    void shouldFormatNestedWithInCollection() {
        assertThat(formatter.format(List.of(Map.of("a", BOOLEAN))), is("[{\"a\":true}]"));
    }

    @Test
    void shouldFormatMap() {
        assertThat(
                formatter.format(new TreeMap<>(Map.of("a", BOOLEAN, "b", BYTE))),
                is("{\"a\":true,\"b\":127}"));
    }

    @Test
    void shouldEscapeMapKeys() {
        assertThat(formatter.format(Map.of("a\nb", BOOLEAN)), is("{\"a\\nb\":true}"));
    }

    @Test
    void shouldConvertMapKeysToStrings() {
        assertThat(formatter.format(Map.of(10, BOOLEAN)), is("{\"10\":true}"));
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
        assertThat(formatter.format(new char[] {}), is("[]"));
        assertThat(formatter.format(new char[] {'a'}), is("[\"a\"]"));
        assertThat(formatter.format(new char[] {'a', 'b'}), is("[\"a\",\"b\"]"));
        assertThat(formatter.format(new char[] {'a', '\b'}), is("[\"a\",\"\\b\"]"));
    }

    @Test
    void shouldFormatBooleanArray() {
        assertThat(formatter.format(new boolean[] {}), is("[]"));
        assertThat(formatter.format(new boolean[] {true}), is("[true]"));
        assertThat(formatter.format(new boolean[] {true, false}), is("[true,false]"));
    }

    @Test
    void shouldFormatByteArray() {
        assertThat(formatter.format(new byte[] {}), is("[]"));
        assertThat(formatter.format(new byte[] {10}), is("[10]"));
        assertThat(formatter.format(new byte[] {Byte.MIN_VALUE, Byte.MAX_VALUE}), is("[-128,127]"));
    }

    @Test
    void shouldFormatShortArray() {
        assertThat(formatter.format(new short[] {}), is("[]"));
        assertThat(formatter.format(new short[] {10}), is("[10]"));
        assertThat(
                formatter.format(new short[] {Short.MIN_VALUE, Short.MAX_VALUE}),
                is("[-32768,32767]"));
    }

    @Test
    void shouldFormatIntArray() {
        assertThat(formatter.format(new int[] {}), is("[]"));
        assertThat(formatter.format(new int[] {10}), is("[10]"));
        assertThat(
                formatter.format(new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE}),
                is("[-2147483648,2147483647]"));
    }

    @Test
    void shouldFormatLongArray() {
        assertThat(formatter.format(new long[] {}), is("[]"));
        assertThat(formatter.format(new long[] {10}), is("[10]"));
        assertThat(
                formatter.format(new long[] {Long.MIN_VALUE, Long.MAX_VALUE}),
                is("[-9223372036854775808,9223372036854775807]"));
    }

    @Test
    void shouldFormatFloatArray() {
        assertThat(formatter.format(new float[] {}), is("[]"));
        assertThat(formatter.format(new float[] {10}), is("[10.0]"));
        assertThat(
                formatter.format(new float[] {Float.MIN_VALUE, Float.MAX_VALUE}),
                is("[1.4E-45,3.4028235E38]"));
    }

    @Test
    void shouldFormatDoubleArray() {
        assertThat(formatter.format(new double[] {}), is("[]"));
        assertThat(formatter.format(new double[] {10}), is("[10.0]"));
        assertThat(
                formatter.format(new double[] {Double.MIN_VALUE, Double.MAX_VALUE}),
                is("[4.9E-324,1.7976931348623157E308]"));
    }

    @Test
    void shouldFormatObjectArray() {
        assertThat(formatter.format(new Object[] {}), is("[]"));
        assertThat(formatter.format(new Object[] {10}), is("[10]"));
        assertThat(
                formatter.format(new Object[] {new AtomicInteger(10), "hello\n"}),
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
                                                                Map.of("7", Map.of("8", 1))))))));

        // When:
        formatter.format(tooDeeplyNested);

        // Then: did not throw
    }

    @SetSystemProperty(key = JsonLogEntryFormatter.MAX_DEPTH_PROP, value = "3")
    @Test
    void shouldSupportCustomMaxDepth() {
        // Given:
        final Map<String, ?> tooDeeplyNested =
                Map.of("1", Map.of("2", Map.of("3", Map.of("4", 1))));

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class, () -> formatter.format(tooDeeplyNested));

        // Then:
        assertThat(e.getMessage(), is("Max depth of 3 exceeded"));
    }
}
