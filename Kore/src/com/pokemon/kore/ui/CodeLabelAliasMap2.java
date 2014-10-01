package com.pokemon.kore.ui;

import com.pokemon.kore.codes.Code2.Link;
import com.pokemon.kore.codes.Label;

public interface CodeLabelAliasMap2 {
  /** return false if this would break the bijection */
  public boolean setAlias(Link link, Label l, String alias);

  public void deleteAlias(Link link, Label l);

  public Bijection<Label, String> getAliases(Link link);

  public void setAliases(Link link, Bijection<Label, String> aliases);
}
