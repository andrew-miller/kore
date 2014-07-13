package com.pokemon.kore.utils;

public interface Comparer<T> {
  Comparison compare(T a, T b);
}
