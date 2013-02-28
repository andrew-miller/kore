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
    switch (type.tag) {
    case PRODUCT:
      return "{" + CodeUtils.labelMap(val) + "}";
    case UNION:
      return CodeUtils.labelMap(val);
    default:
      throw new RuntimeException();
    }
  }

  public static final Value unit = new Value(new HashMap<Label, Value>(),
      CodeUtils.unit);

}
