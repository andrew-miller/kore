package com.example.kore.graph;

public enum Tag {
  UNION("[", "]"), PRODUCT("{", "}");
  public final String open;
  public final String close;
  public final String text;

  Tag(String s, String e) {
    open = s;
    close = e;
    text = s + e;
  }
}
