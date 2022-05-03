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

package org.creekservice.internal.observability.logging.structured;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultLogEntryCustomizerTest {

    private static final Throwable THROWABLE = new OutOfMemoryError();
    private DefaultLogEntryCustomizer customizer;

    @BeforeEach
    void setUp() {
        customizer = DefaultLogEntryCustomizer.create("log message");
    }

    @Test
    void shouldSetMessage() {
        assertThat(customizer.build(), is(Map.of("message", "log message")));
    }

    @Test
    void shouldAddMetricByName() {
        // When:
        customizer.with("key", 10);

        // Then:
        assertThat(customizer.build(), hasEntry("key", 10));
    }

    @Test
    void shouldAddMetricByEnum() {
        // When:
        customizer.with(MetricName.someMetric, 10);

        // Then:
        assertThat(customizer.build(), hasEntry("someMetric", 10));
    }

    @Test
    void shouldThrowOnInvalidKey() {
        assertThrows(NullPointerException.class, () -> customizer.with((String) null, 10));
        assertThrows(IllegalArgumentException.class, () -> customizer.with(" ", 10));
    }

    @Test
    void shouldThrowOnDuplicateKey() {
        // Given:
        customizer.with("duplicate", 10);

        // Then:
        assertThrows(IllegalArgumentException.class, () -> customizer.with("duplicate", 10));
    }

    @Test
    void shouldIgnoreNullValueByName() {
        // When:
        customizer.with("key", null);

        // Then:
        assertThat(customizer.build(), not(hasKey("key")));
    }

    @Test
    void shouldIgnoreNullValueByEnum() {
        // When:
        customizer.with(MetricName.someMetric, null);

        // Then:
        assertThat(customizer.build(), not(hasKey("testKey")));
    }

    @Test
    void shouldSetThrowable() {
        // When:
        customizer.withThrowable(THROWABLE);

        // Then:
        assertThat(customizer.throwable().isPresent(), is(true));
        assertThat(customizer.throwable().get(), is(sameInstance(THROWABLE)));
    }

    @Test
    void shouldThrowIfThrowableAlreadySet() {
        // Given:
        customizer.withThrowable(THROWABLE);

        // When:
        final Exception e =
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> customizer.withThrowable(THROWABLE));

        // Then:
        assertThat(e.getMessage(), is("Exception already set"));
    }

    @Test
    void shouldSetThrowableOnNested() {
        // When:
        customizer.ns("ns").withThrowable(THROWABLE);

        // Then:
        assertThat(customizer.throwable().isPresent(), is(true));
        assertThat(customizer.throwable().get(), is(sameInstance(THROWABLE)));
    }

    @Test
    void shouldThrowIfThrowableAlreadySetOnNested() {
        // Given:
        customizer.ns("ns").withThrowable(THROWABLE);

        // When:
        final Exception e =
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> customizer.withThrowable(THROWABLE));

        // Then:
        assertThat(e.getMessage(), is("Exception already set"));
    }

    @Test
    void shouldThrowOnNestedIfThrowableAlreadySet() {
        // Given:
        customizer.withThrowable(THROWABLE);

        // When:
        final Exception e =
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> customizer.ns("ns").withThrowable(THROWABLE));

        // Then:
        assertThat(e.getMessage(), is("Exception already set"));
    }

    @Test
    void shouldThrowOnInvalidNamespace() {
        assertThrows(NullPointerException.class, () -> customizer.ns((String) null));
        assertThrows(IllegalArgumentException.class, () -> customizer.ns("\t"));
    }

    @Test
    void shouldThrowIfNamespaceClashesWithMetricName() {
        // Given:
        customizer.with("clash", 22);

        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> customizer.ns("clash"));

        // Then:
        assertThat(e.getMessage(), is("Namespace name clashes with existing metric name: clash"));
    }

    @Test
    void shouldThrowIfMetricNameClashesWithNamespace() {
        // Given:
        customizer.ns("clash");

        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> customizer.with("clash", 22));

        // Then:
        assertThat(e.getMessage(), is("Metric name clashes with existing namespace name: clash"));
    }

    @Test
    void shouldThrowIfNamespaceClashesWithMetricNameEvenIfMetricValueNull() {
        // Fail even on null value (which is ignored) to avoid latent bugs:
        // Given:
        customizer.with("clash", null);

        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> customizer.ns("clash"));

        // Then:
        assertThat(e.getMessage(), is("Namespace name clashes with existing metric name: clash"));
    }

    @Test
    void shouldThrowIfMetricNameClashesWithNamespaceEvenIfMetricValueNull() {
        // Fail even on null value (which is ignored) to avoid latent bugs:
        // Given:
        customizer.ns("clash");

        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> customizer.with("clash", null));

        // Then:
        assertThat(e.getMessage(), is("Metric name clashes with existing namespace name: clash"));
    }

    @Test
    void shouldAllowDuplicateNamesInDifferentNamespaces() {
        // When:
        customizer.with("a", 10).ns("b").with("a", 22);

        // Then:
        assertThat(customizer.build(), hasEntry("a", 10));
        assertThat(customizer.build(), hasEntry("b", Map.of("a", 22)));
    }

    @Test
    void shouldIgnoreEmptyNamespaces() {
        // When:
        customizer.ns("empty");

        // Then:
        assertThat(customizer.build(), not(hasKey("empty")));
    }

    @Test
    void shouldCollapseDuplicateNamespaces() {
        // Given:
        customizer.ns("ns").with("a", 10);

        // When:
        customizer.ns("ns").with("b", 20);

        // Then:
        assertThat(customizer.build(), hasEntry("ns", Map.of("a", 10, "b", 20)));
    }

    @Test
    void shouldAddNamespaceByEnum() {
        // When:
        customizer.ns(Namespace.someNs).with("a", 10);

        // Then:
        assertThat(customizer.build(), hasEntry("someNs", Map.of("a", 10)));
    }

    private enum MetricName {
        someMetric
    }

    private enum Namespace {
        someNs
    }
}
