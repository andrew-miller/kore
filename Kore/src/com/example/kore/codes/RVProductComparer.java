package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;

class RVProductComparer implements Comparer<RVertex.Product> {
  public Comparison compare(RVertex.Product a, RVertex.Product b) {
    return new CodeComparer().compare(a.o, b.o);
  }
}
