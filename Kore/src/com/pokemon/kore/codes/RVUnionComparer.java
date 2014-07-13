package com.pokemon.kore.codes;

import com.pokemon.kore.utils.Comparer;
import com.pokemon.kore.utils.Comparison;

public class RVUnionComparer implements Comparer<RVertex.Union> {
  public Comparison compare(RVertex.Union a, RVertex.Union b) {
    CodeComparer cc = new CodeComparer();
    Comparison c1 = cc.compare(a.i, b.i);
    if (c1 != Comparison.EQ)
      return c1;
    return cc.compare(a.o, b.o);
  }
}