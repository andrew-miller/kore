package com.example.kore.graph;

import java.util.HashMap;
import java.util.Map;

public class Equivalence<O> {
  Map<O, O> pointers;

  public Equivalence() {
    pointers = new HashMap<O, O>();

  }

  public void add(O... os) {
    if (os.length > 0) {
      O result = find(os[0]);
      for (int i = 1; i < os.length; i++) {
        pointers.put(find(os[i]), result);
      }
    }
  }

  private O find(O o) {
    O parent = pointers.get(o);
    if (parent == null)
      return o;
    parent = find(parent);
    pointers.put(o, parent);
    return parent;
  }

  public boolean eq(O o1, O o2) {
    o1 = find(o1);
    o2 = find(o2);
    return (o1 == o2);
  }
}
