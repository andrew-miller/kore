package com.example.kore.utils;

import static com.example.kore.utils.Null.notNull;

import java.io.Serializable;

/**
 * Immutable, thread-safe, null-free.
 * 
 * default serialization
 */
public final class Either<X, Y> implements Serializable {
  public enum Tag {
    X, Y
  }

  public final Tag tag;
  private final X x;
  private final Y y;

  private Either(Tag tag, X x, Y y) {
    this.tag = tag;
    this.x = x;
    this.y = y;
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
    return new Either<>(Tag.X, x, null);
  }

  public static <X, Y> Either<X, Y> y(Y y) {
    notNull(y);
    return new Either<>(Tag.Y, null, y);
  }

  @Override
  public String toString() {
    return "Either [tag=" + tag + ", x=" + x + ", y=" + y + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tag == null) ? 0 : tag.hashCode());
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
    if (tag != other.tag)
      return false;
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