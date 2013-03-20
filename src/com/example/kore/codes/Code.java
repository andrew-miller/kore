package com.example.kore.codes;

import java.io.Serializable;
import com.example.unsuck.Null;

import fj.data.TreeMap;

public final class Code implements Serializable {
  private static final long serialVersionUID = 1L;

  public enum Tag {
    UNION, PRODUCT
  }

  public final Tag tag;
  public final TreeMap<Label, CodeRef> labels;

  public Code(Tag tag, TreeMap<Label, CodeRef> labels) {
    Null.notNull(tag, labels);
    this.tag = tag;
    this.labels = labels;
  }

  @Override
  public String toString() {
    return "Code [tag=" + tag + ", labels=" + labels + "]";
  }

  public final static Code newUnion(TreeMap<Label, CodeRef> labels) {
    return new Code(Tag.UNION, labels);
  }

  public static Code newProduct(TreeMap<Label, CodeRef> labels) {
    return new Code(Tag.PRODUCT, labels);
  }

}
