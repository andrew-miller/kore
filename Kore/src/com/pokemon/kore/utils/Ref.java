package com.pokemon.kore.utils;

import static com.pokemon.kore.utils.Null.notNull;

/** null-free mutable reference */
public class Ref<T> {
  private T x;

  public Ref(T x) {
    notNull(x);
    this.x = x;
  }

  public void set(T x) {
    notNull(x);
    this.x = x;
  }

  public T get() {
    return x;
  }
}
