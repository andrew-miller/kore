package com.example.kore.codes;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.kore.graph.Equivalence;
import com.example.unsuck.Null;

public final class Code implements Serializable {
  private static final long serialVersionUID = 1L;

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

  public final Tag tag;
  public final Map<Label, Code> labels;

  public Code(Tag tag, Map<Label, Code> labels) {
    Null.notNull(labels);
    this.tag = tag;
    this.labels = Collections.unmodifiableMap(labels);
  }

  @Override
  public String toString() {
    return toString(new HashMap<Label, String>());
  }

  public String toString(Map<Label, String> aliases) {
    return tag.open + ((labels.size() > 0) ? "..." : "") + tag.close;
  }

  public final static Code newUnion(Map<Label, Code> labels) {
    return new Code(Tag.UNION, labels);
  }

  public static Code newProduct(Map<Label, Code> labels) {
    return new Code(Tag.PRODUCT, labels);
  }

  public boolean eq(Code o) {
    return eq(o, new Equivalence<Code>());
  }

  private boolean eq(Code o, Equivalence<Code> B) {
    if (this.tag != o.tag)
      return false;
    if (!this.labels.keySet().equals(o.labels.keySet()))
      return false;
    B.add(this, o);
    for (Label l : labels.keySet()) {
      if (this.labels.get(l).eq(o.labels.get(l), B))
        return false;
    }
    return true;
  }

  public Code projection(List<Label> path) {
    if (path == null)
      return null;
    Code root = this;
    for (Label l : path) {
      root = root.labels.get(l);
    }
    return root;
  }
}
