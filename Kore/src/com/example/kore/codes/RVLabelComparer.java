package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;

public class RVLabelComparer implements Comparer<RVertex.Label> {
  public Comparison compare(RVertex.Label a, RVertex.Label b) {
    return new CodeComparer().compare(a.o, b.o);
  }
}