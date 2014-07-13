package com.pokemon.kore.codes;

import com.pokemon.kore.utils.Comparer;
import com.pokemon.kore.utils.Comparison;
import com.pokemon.kore.utils.Pair;

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