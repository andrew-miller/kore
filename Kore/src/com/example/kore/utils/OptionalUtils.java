package com.example.kore.utils;

import com.example.kore.utils.Optional.Nothing;
import com.example.kore.utils.Optional.Some;

public class OptionalUtils {
  public static <T> Optional<T> nothing(Class<T> tClass) {
    return Optional.nothing(new Nothing<T>(tClass));
  }

  public static <T> Optional<T> some(T x) {
    return Optional.some(new Some<T>(x));
  }

  public static <T> Optional<T> fromObject(T x, Class<T> tClass) {
    if (x == null)
      return nothing(tClass);
    return some(x);
  }
}
