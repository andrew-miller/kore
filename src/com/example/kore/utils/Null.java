package com.example.kore.utils;

public class Null {
  public static void notNull(Object... o) {
    for (Object p : o)
      if (p == null)
        throw new NullPointerException();
  }
}
