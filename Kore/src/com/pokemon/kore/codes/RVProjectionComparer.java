package com.pokemon.kore.codes;

import com.pokemon.kore.utils.Comparer;
import com.pokemon.kore.utils.Comparison;
import com.pokemon.kore.utils.LabelComparer;

public class RVProjectionComparer implements Comparer<RVertex.Projection> {
  public Comparison compare(RVertex.Projection a, RVertex.Projection b) {
    Comparison c = new CodeComparer().compare(a.o, b.o);
    if (c != Comparison.EQ)
      return c;
    return new ListComparer<>(new LabelComparer()).compare(a.path, b.path);
  }
}