package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import com.example.kore.utils.Pair;

public class PairComparer<K, V> implements Comparer<Pair<K, V>> {
  private final Comparer<K> kc;
  private final Comparer<V> vc;

  public PairComparer(Comparer<K> kc, Comparer<V> vc) {
    this.kc = kc;
    this.vc = vc;
  }

  public Comparison compare(Pair<K, V> a, Pair<K, V> b) {
    Comparison ck = kc.compare(a.x, b.x);
    if (ck != Comparison.EQ)
      return ck;
    return vc.compare(a.y, b.y);
  }
}