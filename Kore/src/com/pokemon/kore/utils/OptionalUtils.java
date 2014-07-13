package com.pokemon.kore.utils;

import com.pokemon.kore.utils.Optional.Nothing;
import com.pokemon.kore.utils.Optional.Some;

public class OptionalUtils {
  public static <T> Optional<T> nothing() {
    return Optional.nothing(new Nothing<T>());
  }

  public static <T> Optional<T> some(T x) {
    return Optional.some(new Some<>(x));
  }

  public static <T> Optional<T> fromObject(T x) {
    if (x == null)
      return nothing();
    return some(x);
  }
}
