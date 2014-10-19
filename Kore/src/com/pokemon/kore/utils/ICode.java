package com.pokemon.kore.utils;

import com.pokemon.kore.codes.Code2;
import com.pokemon.kore.codes.Label;

public interface ICode {
  public Map<Label, Either<ICode, List<Label>>> labels();

  /**
   * <code>(r, p)</code> where <code>r</code> is the root of the SCC containing
   * this code and <code>p</code> is the path from <code>r</code> to this code
   */
  public Pair<Code2, List<Label>> link();

  public Code2.Tag tag();

  /**
   * The code within this SCC at then end of <code>path</code> starting from the
   * root of this SCC
   */
  public ICode codeAt(List<Label> path);
}
