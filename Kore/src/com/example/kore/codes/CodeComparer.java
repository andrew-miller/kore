package com.example.kore.codes;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import com.example.kore.utils.Either;
import com.example.kore.utils.EitherComparer;
import com.example.kore.utils.LabelComparer;
import com.example.kore.utils.List;
import com.example.kore.utils.Pair;

public class CodeComparer implements Comparer<Code> {
  public Comparison compare(Code a, Code b) {
    Comparison x = new EnumComparer<Code.Tag>().compare(a.tag, b.tag);
    if (x != Comparison.EQ)
      return x;
    return new ListComparer<Pair<Label, Either<Code, List<Label>>>>(
        new PairComparer<Label, Either<Code, List<Label>>>(new LabelComparer(),
            new EitherComparer<Code, List<Label>>(this,
                new ListComparer<Label>(new LabelComparer())))).compare(
        a.labels.entrySet(), b.labels.entrySet());
  }
}