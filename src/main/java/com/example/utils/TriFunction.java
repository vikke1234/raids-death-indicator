package com.example.utils;

import java.util.Objects;
import java.util.function.Function;

public interface TriFunction<S, T, U, V, R> {
    R apply(S s, T t, U u, V v);
    default <K> TriFunction<S, T, U, V, K> andThen(Function<? super R, ? extends K> after) {
        Objects.requireNonNull(after);
        return (S s, T t, U u, V v) -> after.apply(apply(s, t, u, v));
    }
}
