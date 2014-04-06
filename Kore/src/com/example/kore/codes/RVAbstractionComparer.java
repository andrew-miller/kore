package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;

public class RVAbstractionComparer implements Comparer<RVertex.Abstraction> {
  public Comparison compare(RVertex.Abstraction a, RVertex.Abstraction b) {
    CodeComparer cc = new CodeComparer();
    Comparison c1 = cc.compare(a.i, b.i);
    if (c1 != Comparison.EQ)
      return c1;
    Comparison c2 = cc.compare(a.o, b.o);
    if (c2 != Comparison.EQ)
      return c2;
    return new RVPatternComparer().compare(a.pattern, b.pattern);
  }
}