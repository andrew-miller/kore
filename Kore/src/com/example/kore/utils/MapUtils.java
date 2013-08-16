package com.example.kore.utils;

import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;

import com.example.kore.utils.Map.Entry;

public class MapUtils {

  public static <K, V> boolean containsKey(Map<K, V> m, K k) {
    return !m.get(k).isNothing();
  }

  public static <K, V> List<V> values(Map<K, V> m) {
    List<V> l = nil();
    for (Entry<K, V> e : iter(m.entrySet()))
      l = cons(e.v, l);
    return l;
  }

  public static <K, V> List<K> keys(Map<K, V> m) {
    List<K> l = nil();
    for (Entry<K, V> e : iter(m.entrySet()))
      l = cons(e.k, l);
    return l;
  }

}
