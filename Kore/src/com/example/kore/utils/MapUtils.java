package com.example.kore.utils;

import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;


public class MapUtils {

  public static <K, V> boolean containsKey(Map<K, V> m, K k) {
    return !m.get(k).isNothing();
  }

  public static <K, V> List<V> values(Map<K, V> m) {
    List<V> l = nil();
    for (Pair<K, V> e : iter(m.entrySet()))
      l = cons(e.y, l);
    return l;
  }

  public static <K, V> List<K> keys(Map<K, V> m) {
    List<K> l = nil();
    for (Pair<K, V> e : iter(m.entrySet()))
      l = cons(e.x, l);
    return l;
  }

  public static <K, V, V2> Map<K, V2> map(F<V, V2> f, Map<K, V> m) {
    Map<K, V2> m2 = Map.empty();
    for (Pair<K, V> x : iter(m.entrySet()))
      m2 = m2.put(x.x, f.f(x.y));
    return m2;
  }

}
