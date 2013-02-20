package com.example.unsuck;

public class Boom {
  public static RuntimeException boom() {
    return new RuntimeException("shit's broke");
  }
}
