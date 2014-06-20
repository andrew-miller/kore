package com.example.kore.utils;

import static com.example.kore.utils.Null.notNull;

/**
 * null-free reference. Initially has no value, and can never change once it
 * gets assigned.
 */
public class SARef<T> {
  private T x;

  public void set(T x) {
    notNull(x);
    if (this.x != null)
      throw new RuntimeException("already set");
    this.x = x;
  }

  public T get() {
    if (x == null)
      throw new RuntimeException("not set");
    return x;
  }
}