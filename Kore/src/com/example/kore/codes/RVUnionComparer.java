package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;

public class RVUnionComparer implements Comparer<RVertex.Union> {
  public Comparison compare(RVertex.Union a, RVertex.Union b) {
    CodeComparer cc = new CodeComparer();
    Comparison c1 = cc.compare(a.i, b.i);
    if (c1 != Comparison.EQ)
      return c1;
    return cc.compare(a.o, b.o);
  }
}