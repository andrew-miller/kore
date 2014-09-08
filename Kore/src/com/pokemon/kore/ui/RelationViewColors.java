package com.pokemon.kore.ui;

import java.io.Serializable;

import com.pokemon.kore.utils.Pair;

public class RelationViewColors implements Serializable {
  public final RelationColors relationcolors;
  public final Integer aliasTextColor;
  public final Integer labelTextColor;
  public final Pair<Integer, Integer> referenceColors;

  public RelationViewColors(RelationColors relationcolors,
      Integer aliasTextColor, Integer labelTextColor,
      Pair<Integer, Integer> referenceColors) {
    this.relationcolors = relationcolors;
    this.labelTextColor = labelTextColor;
    this.aliasTextColor = aliasTextColor;
    this.referenceColors = referenceColors;
  }

}
