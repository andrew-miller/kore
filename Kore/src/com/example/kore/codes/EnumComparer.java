package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;

public class EnumComparer<E extends Enum<E>> implements Comparer<E> {
  public Comparison compare(E a, E b) {
    int x = a.compareTo(b);
    if (x < 0)
      return Comparison.LT;
    if (x > 0)
      return Comparison.GT;
    return Comparison.EQ;
  }
}