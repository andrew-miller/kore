package com.example.kore.utils;

import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.ListUtils.sort;
import static com.example.kore.utils.OptionalUtils.nothing;
import static com.example.kore.utils.OptionalUtils.some;
import static com.example.kore.utils.Pair.pair;

import java.io.Serializable;

/**
 * Map implemented using a prefix tree. Immutable, thread-safe, null-free.
 * 
 * default serialization
 */
public final class Map<K, V> implements Serializable {
  private final Tree<Character, Optional<Pair<K, V>>> tree;
  // keys are sorted in reverse order so entrySet can use cons instead of append
  private final Comparer<Pair<Character, Tree<Character, Optional<Pair<K, V>>>>> comparer =
      new PairLeftComparer<Character, Tree<Character, Optional<Pair<K, V>>>>(
          new ReverseComparer<Character>(new CharacterComparer()));

  private Map(Tree<Character, Optional<Pair<K, V>>> tree) {
    this.tree = tree;
  }

  public static <K, V> Map<K, V> empty() {
    return new Map<K, V>(new Tree<Character, Optional<Pair<K, V>>>(
        OptionalUtils.<Pair<K, V>> nothing(),
        ListUtils
            .<Pair<Character, Tree<Character, Optional<Pair<K, V>>>>> nil()));
  }

  public Optional<V> get(K k) {
    Tree<Character, Optional<Pair<K, V>>> t = tree;
    for (char c : k.toString().toCharArray()) {
      Tree<Character, Optional<Pair<K, V>>> t2 = getEdge(t, c);
      if (t2 == null)
        return nothing();
      t = t2;
    }
    if (t.v.isNothing())
      return nothing();
    return some(t.v.some().x.y);
  }

  public Map<K, V> put(K k, V v) {
    return new Map<K, V>(put(tree, k.toString(), v, k));
  }

  private Tree<Character, Optional<Pair<K, V>>> put(
      Tree<Character, Optional<Pair<K, V>>> t, String s, V v, K k) {
    if (s.equals(""))
      return new Tree<Character, Optional<Pair<K, V>>>(some(Pair.pair(k, v)),
          t.edges);
    char c = s.charAt(0);
    Tree<Character, Optional<Pair<K, V>>> t2 = getEdge(t, c);
    if (t2 == null)
      return new Tree<Character, Optional<Pair<K, V>>>(
          t.v,
          sort(
              cons(
                  pair(
                      c,
                      put(new Tree<Character, Optional<Pair<K, V>>>(
                          OptionalUtils.<Pair<K, V>> nothing(),
                          ListUtils
                              .<Pair<Character, Tree<Character, Optional<Pair<K, V>>>>> nil()),
                          s.substring(1), v, k)), t.edges), comparer));
    return new Tree<Character, Optional<Pair<K, V>>>(t.v, sort(
        replace(pair(c, put(t2, s.substring(1), v, k)), t.edges), comparer));
  }

  private List<Pair<Character, Tree<Character, Optional<Pair<K, V>>>>> replace(
      Pair<Character, Tree<Character, Optional<Pair<K, V>>>> edge,
      List<Pair<Character, Tree<Character, Optional<Pair<K, V>>>>> edges) {
    if (edges.cons().x.x.equals(edge.x))
      return cons(edge, edges.cons().tail);
    return cons(edges.cons().x, replace(edge, edges.cons().tail));
  }

  // I consider this O(1). There are a constant of 2^16 values of
  // char. Either way it's unusual to have a large amount of
  // edges since only ASCII chars are typically used.
  private Tree<Character, Optional<Pair<K, V>>> getEdge(
      Tree<Character, Optional<Pair<K, V>>> t, char c) {
    for (Pair<Character, Tree<Character, Optional<Pair<K, V>>>> p : iter(t.edges))
      if (p.x.equals(c))
        return p.y;
    return null;
  }

  public Map<K, V> delete(K k) {
    return new Map<K, V>(delete(tree, k.toString()).x);
  }

  private Pair<Tree<Character, Optional<Pair<K, V>>>, Boolean> delete(
      Tree<Character, Optional<Pair<K, V>>> t, String k) {
    if (k.equals(""))
      return pair(
          new Tree<Character, Optional<Pair<K, V>>>(
              OptionalUtils.<Pair<K, V>> nothing(), t.edges), t.edges.isEmpty());
    char c = k.charAt(0);
    Tree<Character, Optional<Pair<K, V>>> t2 = getEdge(t, c);
    if (t2 == null)
      return pair(t, false);
    Pair<Tree<Character, Optional<Pair<K, V>>>, Boolean> p =
        delete(t2, k.substring(1));
    List<Pair<Character, Tree<Character, Optional<Pair<K, V>>>>> edges = nil();
    for (Pair<Character, Tree<Character, Optional<Pair<K, V>>>> e : iter(t.edges))
      if (!e.x.equals(c))
        edges = cons(e, edges);
    if (!p.y)
      return pair(
          new Tree<Character, Optional<Pair<K, V>>>(t.v, cons(pair(c, p.x),
              edges)), false);
    boolean oneEdge = t.edges.cons().tail.isEmpty();
    return pair(new Tree<Character, Optional<Pair<K, V>>>(t.v, edges), oneEdge
        && t.v.isNothing());

  }

  @Override
  public String toString() {
    List<Entry<K, V>> es = entrySet();
    if (es.isEmpty())
      return "{}";
    String s = "{";
    s += es.cons().x.k + " -> " + es.cons().x.v;
    es = es.cons().tail;
    while (!es.isEmpty()) {
      s += ", " + es.cons().x.k + " -> " + es.cons().x.v;
      es = es.cons().tail;
    }
    return s + "}";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tree == null) ? 0 : tree.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Map other = (Map) obj;
    if (tree == null) {
      if (other.tree != null)
        return false;
    } else if (!tree.equals(other.tree))
      return false;
    return true;
  }

  public static class Entry<K, V> {
    @Override
    public String toString() {
      return "Entry [k=" + k + ", v=" + v + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((k == null) ? 0 : k.hashCode());
      result = prime * result + ((v == null) ? 0 : v.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Entry other = (Entry) obj;
      if (k == null) {
        if (other.k != null)
          return false;
      } else if (!k.equals(other.k))
        return false;
      if (v == null) {
        if (other.v != null)
          return false;
      } else if (!v.equals(other.v))
        return false;
      return true;
    }

    public Entry(K k, V v) {
      this.k = k;
      this.v = v;
    }

    public final K k;
    public final V v;

  }

  public List<Entry<K, V>> entrySet() {
    return entrySet(ListUtils.<Entry<K, V>> nil(), tree);
  }

  private List<Entry<K, V>> entrySet(List<Entry<K, V>> l,
      Tree<Character, Optional<Pair<K, V>>> t) {
    if (!t.v.isNothing()) {
      Pair<K, V> p = t.v.some().x;
      l = cons(new Entry<K, V>(p.x, p.y), l);
    }
    for (Pair<Character, Tree<Character, Optional<Pair<K, V>>>> e : iter(t.edges))
      l = entrySet(l, e.y);
    return l;
  }
}
