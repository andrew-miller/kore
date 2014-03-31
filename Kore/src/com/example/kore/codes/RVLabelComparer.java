package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import com.example.kore.utils.LabelComparer;

public class RVLabelComparer implements Comparer<RVertex.Label> {
  public Comparison compare(RVertex.Label a, RVertex.Label b) {
    Comparison c = new LabelComparer().compare(a.l, b.l);
    if (c != Comparison.EQ)
      return c;
    return new CodeComparer().compare(a.o, b.o);
  }
}