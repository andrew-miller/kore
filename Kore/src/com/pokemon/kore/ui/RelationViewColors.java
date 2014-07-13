package com.pokemon.kore.ui;

import com.pokemon.kore.utils.Pair;

public class RelationViewColors {
  public final RelationColors relationcolors;
  public final Integer aliasTextColor;
  public final Pair<Integer, Integer> referenceColors;

  public RelationViewColors(RelationColors relationcolors,
      Integer aliasTextColor, Pair<Integer, Integer> referenceColors) {
    this.relationcolors = relationcolors;
    this.aliasTextColor = aliasTextColor;
    this.referenceColors = referenceColors;
  }

}
