package com.example.kore.utils;

import static com.example.kore.utils.Boom.boom;

import java.io.Serializable;

public class ReverseComparer<T> implements Comparer<T>, Serializable {
  private final Comparer<T> c;

  public ReverseComparer(Comparer<T> c) {
    this.c = c;
  }

  @Override
  public Comparison compare(T a, T b) {
    switch (c.compare(a, b)) {
    case EQ:
      return Comparison.EQ;
    case GT:
      return Comparison.LT;
    case LT:
      return Comparison.GT;
    default:
      throw boom();
    }
  }
}
