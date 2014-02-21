package com.example.kore.utils;

import static com.example.kore.utils.Null.notNull;

import java.io.Serializable;

/**
 * Immutable, thread-safe, null-free.
 * 
 * default serialization
 */
public final class Either<X, Y> implements Serializable {
  private final X x;
  private final Y y;

  private Either(X x, Y y) {
    this.x = x;
    this.y = y;
  }

  public boolean isY() {
    return x == null;
  }

  public X x() {
    if (x == null)
      throw new RuntimeException("not x");
    return x;
  }

  public Y y() {
    if (y == null)
      throw new RuntimeException("not y");
    return y;
  }

  public static <X, Y> Either<X, Y> x(X x) {
    notNull(x);
    return new Either<X, Y>(x, null);
  }

  public static <X, Y> Either<X, Y> y(Y y) {
    notNull(y);
    return new Either<X, Y>(null, y);
  }

  @Override
  public String toString() {
    return "Either [x=" + x + ", y=" + y + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((x == null) ? 0 : x.hashCode());
    result = prime * result + ((y == null) ? 0 : y.hashCode());
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
    Either other = (Either) obj;
    if (x == null) {
      if (other.x != null)
        return false;
    } else if (!x.equals(other.x))
      return false;
    if (y == null) {
      if (other.y != null)
        return false;
    } else if (!y.equals(other.y))
      return false;
    return true;
  }
}
