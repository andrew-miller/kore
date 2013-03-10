package com.example.kore.codes;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.example.kore.codes.Code.Node;
import com.example.kore.graph.LGraph;

public final class Code extends LGraph<Node, Label, Code> implements
    Serializable {

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

  public static class Node {
    public final Tag tag;

    public Node(Tag tag) {
      this.tag = tag;
    }
  }

  public Code(Tag t, Map<Label, Code> edges) {
    super(new Node(t), edges);
  }

  @Override
  public String toString() {
    return toString(new HashMap<Label, String>());
  }

  public String toString(Map<Label, String> aliases) {
    return node.tag.open + ((edges.size() > 0) ? "..." : "") + node.tag.close;
  }

  public final static Code newUnion(Map<Label, Code> labels) {
    return new Code(Tag.UNION, labels);
  }

  public static Code newProduct(Map<Label, Code> labels) {
    return new Code(Tag.PRODUCT, labels);
  }

}
