package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;

public class RVCompositionComparer implements Comparer<RVertex.Composition> {
  public Comparison compare(RVertex.Composition a, RVertex.Composition b) {
    CodeComparer cc = new CodeComparer();
    Comparison c1 = cc.compare(a.i, b.i);
    if (c1 != Comparison.EQ) return c1;
    return cc.compare(a.o, b.o);
  }
}