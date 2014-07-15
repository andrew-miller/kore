package com.pokemon.kore.utils;

import static com.pokemon.kore.utils.ListUtils.cons;
import static com.pokemon.kore.utils.ListUtils.fromArray;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.length;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.OptionalUtils.some;
import static com.pokemon.kore.utils.PairUtils.pair;

import java.io.Serializable;

/**
 * Immutable, thread-safe, null-free. default serialization
 */
public final class Map<K, V> implements Serializable {
  private final List<Pair<K, V>> l;

  private Map(List<Pair<K, V>> l) {
    this.l = l;
  }

  public static <K, V> Map<K, V> empty() {
    return new Map<>(nil());
  }

  public Optional<V> get(K k) {
    for (Pair<K, V> t : iter(l))
      if (k.equals(t.x))
        return some(t.y);
    return nothing();
  }

  public Map<K, V> put(K k, V v) {
    return new Map<>(put(l, k, v));
  }

  private List<Pair<K, V>> put(List<Pair<K, V>> l, K k, V v) {
    if (l.isEmpty())
      return fromArray(pair(k, v));
    if (l.cons().x.x.equals(k))
      return cons(pair(k, v), l.cons().tail);
    return cons(l.cons().x, put(l.cons().tail, k, v));
  }

  public Map<K, V> delete(K k) {
    return new Map<>(delete(l, k));
  }

  private List<Pair<K, V>> delete(List<Pair<K, V>> l, K k) {
    if (l.isEmpty())
      return l;
    if (l.cons().x.x.equals(k))
      return l.cons().tail;
    return cons(l.cons().x, delete(l.cons().tail, k));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((l == null) ? 0 : l.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    Map<K, V> m = (Map<K, V>) o;
    for (Pair<K, V> e : iter(l)) {
      Optional<V> ov = m.get(e.x);
      if (ov.isNothing())
        return false;
      if (!ov.some().x.equals(e.y))
        return false;
    }
    return length(l) == length(m.l);
  }

  @Override
  public String toString() {
    List<Pair<K, V>> es = entrySet();
    if (es.isEmpty())
      return "{}";
    String s = "{";
    s += es.cons().x.x + " -> " + es.cons().x.y;
    es = es.cons().tail;
    while (!es.isEmpty()) {
      s += ", " + es.cons().x.x + " -> " + es.cons().x.y;
      es = es.cons().tail;
    }
    return s + "}";
  }

  public List<Pair<K, V>> entrySet() {
    return l;
  }
}
