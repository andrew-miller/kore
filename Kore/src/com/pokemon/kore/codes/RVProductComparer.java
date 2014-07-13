package com.pokemon.kore.codes;

import com.pokemon.kore.utils.Comparer;
import com.pokemon.kore.utils.Comparison;

class RVProductComparer implements Comparer<RVertex.Product> {
  public Comparison compare(RVertex.Product a, RVertex.Product b) {
    return new CodeComparer().compare(a.o, b.o);
  }
}
