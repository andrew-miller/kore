package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import com.example.kore.utils.Map;

public class MapEntryComparer<K, V> implements Comparer<Map.Entry<K, V>> {
  private final Comparer<K> kc;
  private final Comparer<V> vc;

  public MapEntryComparer(Comparer<K> kc, Comparer<V> vc) {
    this.kc = kc;
    this.vc = vc;
  }

  public Comparison compare(Map.Entry<K, V> a, Map.Entry<K, V> b) {
    Comparison ck = kc.compare(a.k, b.k);
    if (ck != Comparison.EQ) return ck;
    return vc.compare(a.v, b.v);
  }
}