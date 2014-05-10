package com.example.kore.ui;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Label;

public interface CodeLabelAliasMap {
  /** return false if this would break the bijection */
  public boolean setAlias(CanonicalCode c, Label l, String alias);

  public void deleteAlias(CanonicalCode c, Label l);

  public Bijection<Label, String> getAliases(CanonicalCode c);

  public void setAliases(CanonicalCode c, Bijection<Label, String> aliases);
}
