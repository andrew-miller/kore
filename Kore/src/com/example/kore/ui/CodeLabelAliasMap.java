package com.example.kore.ui;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Label;
import com.example.kore.utils.Map;

public interface CodeLabelAliasMap {
  public void setAlias(CanonicalCode c, Label l, String alias);

  public void deleteAlias(CanonicalCode c, Label l);

  public Map<Label, String> getAliases(CanonicalCode c);

  public void setAliases(CanonicalCode c, Map<Label, String> aliases);
}
