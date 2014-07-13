package com.pokemon.kore.codes;

import java.io.Serializable;

import com.pokemon.kore.utils.Map;

public class Value implements Serializable {
  public final Map<Label, Value> val;
  public final Code code;

  public Value(Map<Label, Value> val, Code code) {
    this.val = val;
    this.code = code;
  }

  @Override
  public String toString() {
    return "Value [val=" + val + ", type=" + code + "]";
  }
}
