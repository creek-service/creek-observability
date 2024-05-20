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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Easily build a chain of multiple <i>Observer</i> / <i>Listener</i> implementations.
 *
 * <p>This encourages decomposition and decoupling of functionality.
 *
 * <p>Given an interface {@code MyListener}, this class can be used to chain multiple listeners
 * together, without the need to write any custom code.
 *
 * <pre>{@code
 * MyListener composite = CompositeObserverBuilder.builder(MyListener.class, listener1)
 *     .observer(listener2)
 *     ...
 *     .observer(listenerN)
 *     .build()
 * }</pre>
 *
 * @param <Observer> the observer interface type
 */
public final class CompositeObserverBuilder<Observer> {

    private final Class<Observer> observerClass;
    private final List<Observer> observers = new ArrayList<>();

    /**
     * Create a builder
     *
     * @param observerClass the observer interface type.
     * @param observer the first observer to call.
     * @return the composite builder
     * @param <Observer> the type of the observer interface.
     */
    public static <Observer> CompositeObserverBuilder<Observer> builder(
            final Class<Observer> observerClass, final Observer observer) {
        if (!observerClass.isInterface()) {
            throw new IllegalArgumentException("observerClass must be an interface");
        }
        validateMethodsAreVoidReturnType(observerClass);
        return new CompositeObserverBuilder<>(observerClass, observer);
    }

    private CompositeObserverBuilder(final Class<Observer> observerClass, final Observer observer) {
        this.observerClass = observerClass;
        add(observer);
    }

    /**
     * Add another observer to the chain
     *
     * @param observer the observer to add.
     * @return self, to allow chaining.
     */
    public CompositeObserverBuilder<Observer> add(final Observer observer) {
        this.observers.add(Objects.requireNonNull(observer, "observer"));
        return this;
    }

    /**
     * @return the composite observer implementation.
     */
    @SuppressWarnings("unchecked")
    public Observer build() {
        try {
            final Map<Method, MethodHandle> handles = methodHandles(observerClass);
            return (Observer)
                    Proxy.newProxyInstance(
                            getClass().getClassLoader(),
                            new Class<?>[] {observerClass},
                            (proxy, method, args) -> {
                                if (method.getName().equals("toString")) {
                                    return "Proxy composite for " + observerClass.getName();
                                }
                                if (method.getName().equals("equals")) {
                                    return this == proxy;
                                }
                                if (method.getName().equals("hashCode")) {
                                    return observerClass.getName().hashCode();
                                }

                                for (Observer delegate : observers) {
                                    final MethodHandle methodHandle = handles.get(method);
                                    methodHandle.bindTo(delegate).invokeWithArguments(args);
                                }
                                return null;
                            });
        } catch (Exception e) {
            throw new CompositeCreationFailedException(
                    "failed to generate composite observer for interface: " + observerClass, e);
        }
    }

    private Map<Method, MethodHandle> methodHandles(final Class<?> observerClass) {
        final Map<Method, MethodHandle> handles =
                Arrays.stream(observerClass.getDeclaredMethods())
                        .collect(
                                Collectors.toMap(
                                        Function.identity(),
                                        m -> {
                                            try {
                                                return MethodHandles.publicLookup().unreflect(m);
                                            } catch (IllegalAccessException e) {
                                                throw new CompositeCreationFailedException(
                                                        "Unable to unreflect method", e);
                                            }
                                        }));

        for (Class<?> extending : observerClass.getInterfaces()) {
            handles.putAll(methodHandles(extending));
        }
        return handles;
    }

    private static void validateMethodsAreVoidReturnType(final Class<?> observerClass) {
        final List<Method> invalidMethods =
                Arrays.stream(observerClass.getDeclaredMethods())
                        .filter(method -> !method.getReturnType().equals(void.class))
                        .collect(Collectors.toList());

        if (!invalidMethods.isEmpty()) {
            throw new UnsupportedOperationException(
                    "Only observer interfaces, where all methods have a void return type, are"
                            + " supported. Interface: "
                            + observerClass
                            + " invalidMethods: "
                            + invalidMethods);
        }

        for (Class<?> extending : observerClass.getInterfaces()) {
            validateMethodsAreVoidReturnType(extending);
        }
    }

    private static class CompositeCreationFailedException extends RuntimeException {
        CompositeCreationFailedException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
