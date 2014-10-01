package com.pokemon.kore.utils;

import com.pokemon.kore.codes.Code2;
import com.pokemon.kore.codes.Label;

public interface ICode {
  public Map<Label, Either<ICode, List<Label>>> labels();

  /**
   * <code>(r, p)</code> where <code>r</code> is the root of the SCC containing
   * this code and <code>p</code> is the path from r to this code
   */
  public Pair<Code2, List<Label>> link();

  public Code2.Tag tag();
}
