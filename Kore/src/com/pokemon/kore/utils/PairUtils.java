package com.pokemon.kore.utils;

public class PairUtils {
  public static <X, Y> Pair<X, Y> pair(X x, Y y) {
    return new Pair<>(x, y);
  }

  public static <X, Y> F<Pair<X, Y>, Y> snd() {
    return new F<Pair<X, Y>, Y>() {
      public Y f(Pair<X, Y> p) {
        return p.y;
      }
    };
  }
}