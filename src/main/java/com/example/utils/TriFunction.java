package com.example.utils;

public interface TriFunction<S, T, U, V> {
    V apply(S s, T t, U u);
}
