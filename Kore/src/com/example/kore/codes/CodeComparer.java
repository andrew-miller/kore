package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import com.example.kore.utils.EitherComparer;
import com.example.kore.utils.LabelComparer;

public class CodeComparer implements Comparer<Code> {
  public Comparison compare(Code a, Code b) {
    Comparison x = new EnumComparer<Code.Tag>().compare(a.tag, b.tag);
    if (x != Comparison.EQ)
      return x;
    return new ListComparer<>(new PairComparer<>(new LabelComparer(),
        new EitherComparer<>(this, new ListComparer<>(new LabelComparer()))))
        .compare(a.labels.entrySet(), b.labels.entrySet());
  }
}