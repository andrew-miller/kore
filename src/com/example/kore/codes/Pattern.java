package com.example.kore.codes;

import java.util.Map;

public class Pattern {
  public Code type;

  public static class Any extends Pattern {
  }

  public static class Spec extends Pattern {
    public Map<Label, Pattern> fields;
  }

}
