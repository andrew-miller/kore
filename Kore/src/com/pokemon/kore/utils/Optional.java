package com.pokemon.kore.utils;

import static com.pokemon.kore.utils.Null.notNull;

import java.io.Serializable;

/**
 * Immutable, thread-safe, null-free.
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

  public static <T> Optional<T> some(Some<T> some) {
    notNull(some);
    return new Optional<>(null, some);
  }

  public static <T> Optional<T> nothing(Nothing<T> nothing) {
    notNull(nothing);
    return new Optional<>(nothing, null);
  }

  public static final class Nothing<T> implements Serializable {

    @Override
    public String toString() {
      return "Nothing []";
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null)
        return false;
      return (o.getClass().equals(getClass()));
    }
  }

  public static final class Some<T> implements Serializable {
    public final T x;

    public Some(T x) {
      notNull(x);
      this.x = x;
    }

    @Override
    public String toString() {
      return "Some [x=" + x + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((x == null) ? 0 : x.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Some other = (Some) obj;
      if (x == null) {
        if (other.x != null)
          return false;
      } else if (!x.equals(other.x))
        return false;
      return true;
    }

  }

  @Override
  public String toString() {
    return "Optional [nothing=" + nothing + ", some=" + some + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((nothing == null) ? 0 : nothing.hashCode());
    result = prime * result + ((some == null) ? 0 : some.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Optional other = (Optional) obj;
    if (nothing == null) {
      if (other.nothing != null)
        return false;
    } else if (!nothing.equals(other.nothing))
      return false;
    if (some == null) {
      if (other.some != null)
        return false;
    } else if (!some.equals(other.some))
      return false;
    return true;
  }
}
