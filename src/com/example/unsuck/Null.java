package com.example.unsuck;

public class Null {
  public static void notNull(Object o) {
    if (o == null)
      throw new NullPointerException();
  }
}
