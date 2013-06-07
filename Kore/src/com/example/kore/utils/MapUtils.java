package com.example.kore.utils;

import static com.example.kore.utils.Null.notNull;

import java.util.HashMap;
import java.util.Map.Entry;

public class MapUtils {
  public static <KO, KI, V> HashMap<KO, HashMap<KI, V>> cloneNestedMap(
      HashMap<KO, HashMap<KI, V>> m) {
    HashMap<KO, HashMap<KI, V>> m2 = new HashMap<KO, HashMap<KI, V>>();
    for (Entry<KO, HashMap<KI, V>> e : m.entrySet()) {
      notNull(e.getKey(), e.getValue());
      m2.put(e.getKey(), new HashMap<KI, V>(e.getValue()));
    }
    return m2;
  }
}
