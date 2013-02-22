package com.example.kore.codes;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.example.kore.utils.Helpers;
import com.example.unsuck.Null;

public final class Code implements Serializable {
  private static final long serialVersionUID = 1L;

  public enum Tag {
    UNION, PRODUCT
  }

  public final Tag tag;
  public final Map<Label, Code> labels;

  public Code(Tag tag, Map<Label, Code> labels) {
    Null.notNull(labels);
    this.tag = tag;
    this.labels = Collections.unmodifiableMap(labels);
  }

  @Override
  public String toString() {
    switch (tag) {
    case UNION:
      return "[" + Helpers.labelMap(labels) + "]";
    case PRODUCT:
      return "{" + Helpers.labelMap(labels) + "}";
    }
    throw new RuntimeException("Unnown tag: " + tag);
  }

  public final static Code newUnion(Map<Label, Code> labels) {
    return new Code(Tag.UNION, labels);
  }

  public static Code newProduct(Map<Label, Code> labels) {
    return new Code(Tag.PRODUCT, labels);
  }

}
