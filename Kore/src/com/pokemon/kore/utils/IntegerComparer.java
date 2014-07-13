package com.pokemon.kore.utils;

public class IntegerComparer implements Comparer<Integer> {
  public Comparison compare(Integer a, Integer b) {
    if (a.equals(b))
      return Comparison.EQ;
    if (a < b)
      return Comparison.LT;
    return Comparison.GT;
  }

}
