package com.example.kore.utils;

import static com.example.kore.utils.Null.notNull;

import java.io.Serializable;

/**
 * Immutable, thread-safe, null-free.
 * 
 * default serialization
 */
public final class Either3<X, Y, Z> implements Serializable {
  public enum Tag {
    X, Y, Z
  }

  public final Tag tag;
  private final X x;
  private final Y y;
  private final Z z;

  private Either3(Tag tag, X x, Y y, Z z) {
    this.tag = tag;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public X x() {
    if (tag != Tag.X)
      throw new RuntimeException("not x");
    return x;
  }

  public Y y() {
    if (tag != Tag.Y)
      throw new RuntimeException("not y");
    return y;
  }

  public Z z() {
    if (tag != Tag.Z)
      throw new RuntimeException("not z");
    return z;
  }

  public static <X, Y, Z> Either3<X, Y, Z> x(X x) {
    notNull(x);
    return new Either3<>(Tag.X, x, null, null);
  }

  public static <X, Y, Z> Either3<X, Y, Z> y(Y y) {
    notNull(y);
    return new Either3<>(Tag.Y, null, y, null);
  }

  public static <X, Y, Z> Either3<X, Y, Z> z(Z z) {
    notNull(z);
    return new Either3<>(Tag.Z, null, null, z);
  }

  @Override
  public String toString() {
    return "Either [tag=" + tag + ", x=" + x + ", y=" + y + ", z=" + z + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tag == null) ? 0 : tag.hashCode());
    result = prime * result + ((x == null) ? 0 : x.hashCode());
    result = prime * result + ((y == null) ? 0 : y.hashCode());
    result = prime * result + ((z == null) ? 0 : z.hashCode());
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
    Either3 other = (Either3) obj;
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
    if (z == null) {
      if (other.z != null)
        return false;
    } else if (!z.equals(other.z))
      return false;
    return true;
  }
}
