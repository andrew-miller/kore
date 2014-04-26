package com.example.kore.utils;

public class PairUtils {
  public static <X, Y> Pair<X, Y> pair(X x, Y y) {
    return new Pair<X, Y>(x, y);
  }
}