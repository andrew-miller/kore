package com.example.kore.ui;

import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Tag;
import com.example.kore.utils.Map;
import com.example.kore.utils.Pair;

public final class RelationColors {
  public final Map<Relation.Tag, Pair<Integer, Integer>> m;

  public RelationColors(Map<Tag, Pair<Integer, Integer>> m) {
    for (Tag t : Tag.values())
      if (m.get(t).isNothing())
        throw new RuntimeException("Missing color for " + t);
    this.m = m;
  }

}
