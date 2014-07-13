package com.pokemon.kore.utils;

import java.io.Serializable;

public class Unit implements Serializable {
  private Unit() {
  }

  public static final Unit unit() {
    return new Unit();
  }

  @Override
  public String toString() {
    return "Unit []";
  }

  @Override
  public boolean equals(Object o) {
    return (o instanceof Unit);
  }

  @Override
  public int hashCode() {
    return 0;
  }
}
