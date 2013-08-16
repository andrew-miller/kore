package com.example.kore.utils;

import java.io.Serializable;

public class PairLeftComparer<L, R> implements Comparer<Pair<L, R>>,
    Serializable {
  private final Comparer<L> comparer;

  public PairLeftComparer(Comparer<L> comparer) {
    this.comparer = comparer;
  }

  @Override
  public Comparison compare(Pair<L, R> a, Pair<L, R> b) {
    return comparer.compare(a.x, b.x);
  }

}
