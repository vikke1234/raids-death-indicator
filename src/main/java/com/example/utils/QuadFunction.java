package com.example.utils;

public interface QuadFunction<S, T, U, V, R> {
    R apply(S s, T t, U u, V v);
}
