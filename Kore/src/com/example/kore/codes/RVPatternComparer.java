package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import com.example.kore.utils.LabelComparer;
import com.example.kore.utils.Map;

public class RVPatternComparer implements Comparer<Pattern> {
  public Comparison compare(Pattern a, Pattern b) {
    return new ListComparer<Map.Entry<Label, Pattern>>(
        new MapEntryComparer<Label, Pattern>(new LabelComparer(), this))
        .compare(a.fields.entrySet(), b.fields.entrySet());
  }
}
