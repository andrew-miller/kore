package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import com.example.kore.utils.List;

public class ListComparer<E> implements Comparer<List<E>> {
  private final Comparer<E> c;

  public ListComparer(Comparer<E> c) {
    this.c = c;
  }

  public Comparison compare(List<E> a, List<E> b) {
    if (a.isEmpty() & b.isEmpty())
      return Comparison.EQ;
    if (a.isEmpty())
      return Comparison.LT;
    if (b.isEmpty())
      return Comparison.GT;
    Comparison comp = c.compare(a.cons().x, b.cons().x);
    if (comp != Comparison.EQ)
      return comp;
    return compare(a.cons().tail, b.cons().tail);
  }
}