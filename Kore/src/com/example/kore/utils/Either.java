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
    return new Either<X, Y>(Tag.X, x, null);
  }

  public static <X, Y> Either<X, Y> y(Y y) {
    notNull(y);
    return new Either<X, Y>(Tag.Y, null, y);
  }

}