package com.example.kore.codes;

import static com.example.kore.utils.Boom.boom;
import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import com.example.kore.utils.LabelComparer;

public class CodeOrPathComparer implements Comparer<CodeOrPath> {
  public Comparison compare(CodeOrPath a, CodeOrPath b) {
    Comparison tc = new EnumComparer<CodeOrPath.Tag>().compare(a.tag, b.tag);
    if (tc != Comparison.EQ)
      return tc;
    switch (a.tag) {
    case CODE:
      return new CodeComparer().compare(a.code, b.code);
    case PATH:
      return new ListComparer<Label>(new LabelComparer()).compare(a.path,
          b.path);
    default:
      throw boom();
    }
  }
}