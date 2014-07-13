package com.pokemon.kore.utils;

public class Boom {
  public static RuntimeException boom() {
    return new RuntimeException("shit's broke");
  }
}
