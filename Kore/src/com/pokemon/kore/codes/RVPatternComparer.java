package com.pokemon.kore.codes;

import com.pokemon.kore.utils.Comparer;
import com.pokemon.kore.utils.Comparison;
import com.pokemon.kore.utils.LabelComparer;

public class RVPatternComparer implements Comparer<Pattern> {
  public Comparison compare(Pattern a, Pattern b) {
    return new ListComparer<>(new PairComparer<>(new LabelComparer(), this))
        .compare(a.fields.entrySet(), b.fields.entrySet());
  }
}
