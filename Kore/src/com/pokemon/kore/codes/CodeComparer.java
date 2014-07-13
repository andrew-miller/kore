package com.pokemon.kore.codes;

import com.pokemon.kore.utils.Comparer;
import com.pokemon.kore.utils.Comparison;
import com.pokemon.kore.utils.EitherComparer;
import com.pokemon.kore.utils.LabelComparer;

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