package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import com.example.kore.utils.LabelComparer;
import com.example.kore.utils.Pair;

public class CodeComparer implements Comparer<Code> {
  public Comparison compare(Code a, Code b) {
    Comparison x = new EnumComparer<Code.Tag>().compare(a.tag, b.tag);
    if (x != Comparison.EQ)
      return x;
    return new ListComparer<Pair<Label, CodeOrPath>>(
        new PairComparer<Label, CodeOrPath>(new LabelComparer(),
            new CodeOrPathComparer())).compare(a.labels.entrySet(),
        b.labels.entrySet());
  }
}