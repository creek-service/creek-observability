/*
 * Copyright 2023 Creek Contributors (https://github.com/creek-service)
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
package org.creekservice.api.observability.patterns;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompositeObserverBuilderTest {

    @Mock Observer observer1;
    @Mock Observer observer2;

    @Test
    public void shouldDelegateToSingle() {
        // Given:
        final Observer result = CompositeObserverBuilder.builder(Observer.class, observer1).build();

        // When:
        result.foo("text");

        // Then:
        verify(observer1).foo("text");
    }

    @Test
    public void shouldDelegateToMultipleInOrder() {
        // Given:
        final Observer result =
                CompositeObserverBuilder.builder(Observer.class, observer1).add(observer2).build();

        // When:
        result.foo("text");

        // Then:
        final InOrder inOrder = inOrder(observer1, observer2);
        inOrder.verify(observer1).foo("text");
        inOrder.verify(observer2).foo("text");
    }

    @Test
    public void shouldBlowUpIfObserverTypeNotInterface() {
        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                CompositeObserverBuilder.builder(
                                        CompositeObserverBuilderTest.class, this));

        // Then:
        assertThat(e.getMessage(), is("observerClass must be an interface"));
    }

    @Test
    public void shouldBlowUpIfYouTryDelegateToNull() {
        // When:
        final Exception e =
                assertThrows(
                        NullPointerException.class,
                        () -> CompositeObserverBuilder.builder(Observer.class, null));

        // Then:
        assertThat(e.getMessage(), is("observer"));
    }

    @Test
    public void shouldBlowUpIfYouTryToAddNullObserver() {
        // Given:
        final CompositeObserverBuilder<Observer> builder =
                CompositeObserverBuilder.builder(Observer.class, observer1);

        // When:
        final Exception e = assertThrows(NullPointerException.class, () -> builder.add(null));

        // Then:
        assertThat(e.getMessage(), is("observer"));
    }

    @Test
    public void shouldBlowUpIfInterfaceHasNonVoidReturnMethods() {
        // When:
        final Exception e =
                assertThrows(
                        UnsupportedOperationException.class,
                        () -> CompositeObserverBuilder.builder(NotAnObserver.class, mock()));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Only observer interfaces, where all methods have a void return type, are"
                            + " supported. Interface: interface"
                            + " org.creekservice.api.observability.patterns.CompositeObserverBuilderTest$NotAnObserver"
                            + " invalidMethods: [public abstract int"
                            + " org.creekservice.api.observability.patterns.CompositeObserverBuilderTest$NotAnObserver.foo(java.lang.String)]"));
    }

    @Test
    public void shouldHandleInterfaceExtensionWithoutBlowingUp() {
        // Given:
        final ExtendingExtendingObserver observer1 = mock();

        final ExtendingExtendingObserver observer =
                CompositeObserverBuilder.builder(ExtendingExtendingObserver.class, observer1)
                        .build();

        // When:
        observer.foo("blah");
        observer.bar();
        observer.lar();

        // Then:
        verify(observer1).foo("blah");
        verify(observer1).bar();
        verify(observer1).lar();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldNotBlowUpOnEquals() {
        CompositeObserverBuilder.builder(ExtendingExtendingObserver.class, mock())
                .build()
                .equals(new Object());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldNotBlowUpOnHashCode() {
        CompositeObserverBuilder.builder(ExtendingExtendingObserver.class, mock())
                .build()
                .hashCode();
    }

    @Test
    public void shouldNotBlowUpOnToString() {
        assertThat(
                CompositeObserverBuilder.builder(ExtendingExtendingObserver.class, mock())
                        .build()
                        .toString(),
                notNullValue());
    }

    public interface Observer {

        void foo(String message);
    }

    public interface ExtendingObserver extends Observer {
        void bar();
    }

    public interface ExtendingExtendingObserver extends ExtendingObserver {
        void lar();
    }

    @SuppressWarnings("unused")
    public interface NotAnObserver {
        int foo(String message);
    }
}
