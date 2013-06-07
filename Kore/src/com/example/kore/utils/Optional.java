package com.example.kore.utils;

import static com.example.kore.utils.Null.notNull;

import java.io.Serializable;

/**
 * Immutable, thread-safe, heap-pollution-free, null-free.
 * 
 * default serialization
 */
public final class Optional<T> implements Serializable {
  private final Nothing<T> nothing;
  private final Some<T> some;

  private Optional(Nothing<T> nothing, Some<T> some) {
    this.nothing = nothing;
    this.some = some;
  }

  public boolean isNothing() {
    return nothing != null;
  }

  public Nothing<T> nothing() {
    if (nothing == null)
      throw new RuntimeException("not nothing");
    return nothing;
  }

  public Some<T> some() {
    if (some == null)
      throw new RuntimeException("not some");
    return some;
  }

  public void checkType(Class<T> c) {
    notNull(c);
    if (some != null)
      if (!some.x.getClass().equals(c))
        throw new RuntimeException("failed type check");
    if (nothing != null)
      if (!nothing.tClass.equals(c))
        throw new RuntimeException("failed type check");
  }

  public static <T> Optional<T> some(Some<T> some) {
    notNull(some);
    return new Optional<T>(null, some);
  }

  public static <T> Optional<T> nothing(Nothing<T> nothing) {
    notNull(nothing);
    return new Optional<T>(nothing, null);
  }

  public static final class Nothing<T> implements Serializable {
    public final Class<T> tClass;

    public Nothing(Class<T> tClass) {
      notNull(tClass);
      this.tClass = tClass;
    }
  }

  public static final class Some<T> implements Serializable {
    public final T x;

    public Some(T x) {
      notNull(x);
      this.x = x;
    }

  }
}
