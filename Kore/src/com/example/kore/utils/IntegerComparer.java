package com.example.kore.utils;

public class IntegerComparer implements Comparer<Integer> {
  @Override
  public Comparison compare(Integer a, Integer b) {
    if (a.equals(b))
      return Comparison.EQ;
    if (a < b)
      return Comparison.LT;
    return Comparison.GT;
  }

}