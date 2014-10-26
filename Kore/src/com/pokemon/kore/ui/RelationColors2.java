package com.pokemon.kore.ui;

import java.io.Serializable;

import com.pokemon.kore.codes.Relation2;
import com.pokemon.kore.codes.Relation2.Tag;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Pair;

public final class RelationColors2 implements Serializable {
  public final Map<Relation2.Tag, Pair<Integer, Integer>> m;

  public RelationColors2(Map<Tag, Pair<Integer, Integer>> m) {
    for (Tag t : Tag.values())
      if (m.get(t).isNothing())
        throw new RuntimeException("Missing color for " + t);
    this.m = m;
  }

}
