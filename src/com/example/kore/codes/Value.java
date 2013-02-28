package com.example.kore.codes;

import java.util.HashMap;
import java.util.Map;

import com.example.kore.utils.CodeUtils;

public class Value {
  public final Map<Label, Value> val;
  public final Code type;

  public Value(Map<Label, Value> val, Code type) {
    this.val = val;
    this.type = type;
  }

  @Override
  public String toString() {
    return "Value [val=" + val + ", type=" + type + "]";
  }

  public static final Value unit = new Value(new HashMap<Label, Value>(),
      CodeUtils.unit);

}
