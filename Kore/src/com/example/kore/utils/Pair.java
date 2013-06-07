package com.example.kore.utils;

import static com.example.kore.utils.Null.notNull;

public final class Pair<X, Y> {
  public final X x;
  public final Y y;

  private Pair(X x, Y y) {
    this.x = x;
    this.y = y;
  }

  public static <X, Y> Pair<X, Y> pair(X x, Y y) {
    notNull(x, y);
    return new Pair<X, Y>(x, y);
  }

  @Override
  public String toString() {
    return "Pair [x=" + x + ", y=" + y + "]";
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
    Pair other = (Pair) obj;
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