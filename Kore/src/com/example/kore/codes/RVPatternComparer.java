package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import com.example.kore.utils.LabelComparer;

public class RVPatternComparer implements Comparer<Pattern> {
  public Comparison compare(Pattern a, Pattern b) {
    return new ListComparer<>(new PairComparer<>(new LabelComparer(), this))
        .compare(a.fields.entrySet(), b.fields.entrySet());
  }
}
