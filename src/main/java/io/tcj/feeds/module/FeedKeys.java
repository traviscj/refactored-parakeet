package io.tcj.feeds.module;

import com.google.inject.Key;
import com.google.inject.util.Types;

import java.lang.reflect.Type;

/**
 * Builds Guice {@link Key}s for parameterized feed types from runtime class tokens.
 * Lets the framework modules be generic over {@code <Rec, Dto>} yet still bind
 * concrete parameterized types (which erasure otherwise hides at the binding site).
 */
final class FeedKeys {
    private FeedKeys() {
    }

    @SuppressWarnings("unchecked")
    static <T> Key<T> parameterized(Class<?> raw, Type... typeArgs) {
        return (Key<T>) Key.get(Types.newParameterizedType(raw, typeArgs));
    }
}
