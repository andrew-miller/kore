package com.example.unsuck;

public class Variant {
  public static <V, T> V get(T expected, T actual, V value) {
    if (expected != actual)
      throw new RuntimeException("pattern match failure; expected tag "
          + expected + " " + "but actual tag is " + actual);
    return value;
  }

}
