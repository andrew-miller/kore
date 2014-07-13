package com.pokemon.kore.utils;

import static com.pokemon.kore.utils.Comparison.EQ;
import static com.pokemon.kore.utils.Comparison.GT;
import static com.pokemon.kore.utils.Comparison.LT;

import com.pokemon.kore.codes.Label;

public class LabelComparer implements Comparer<Label> {
  public Comparison compare(Label a, Label b) {
    int c = a.label.compareTo(b.label);
    if (c < 0)
      return LT;
    if (c > 0)
      return GT;
    return EQ;
  }
}
