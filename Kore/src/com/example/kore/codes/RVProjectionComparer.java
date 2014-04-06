package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import com.example.kore.utils.LabelComparer;

public class RVProjectionComparer implements Comparer<RVertex.Projection> {
  public Comparison compare(RVertex.Projection a, RVertex.Projection b) {
    Comparison c = new CodeComparer().compare(a.o, b.o);
    if (c != Comparison.EQ)
      return c;
    return new ListComparer<Label>(new LabelComparer()).compare(a.path, b.path);
  }
}