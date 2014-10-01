package com.pokemon.kore.codes;

import static com.pokemon.kore.utils.Null.notNull;

import java.io.Serializable;

import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;

public final class Code2 implements Serializable {
  public enum Tag {
    UNION, PRODUCT
  }

  public static final class Link implements Serializable {
    public final З2Bytes hash;
    public final List<Label> path;

    public Link(З2Bytes hash, List<Label> path) {
      this.hash = hash;
      this.path = path;
    }
  }

  public final Tag tag;
  public final Map<Label, Either3<Code2, List<Label>, Link>> labels;

  public Code2(Tag tag, Map<Label, Either3<Code2, List<Label>, Link>> labels) {
    notNull(tag, labels);
    this.tag = tag;
    this.labels = labels;
  }

  @Override
  public String toString() {
    return "Code [tag=" + tag + ", labels=" + labels + "]";
  }

  public static Code2 newUnion(
      Map<Label, Either3<Code2, List<Label>, Link>> labels) {
    return new Code2(Tag.UNION, labels);
  }

  public static Code2 newProduct(
      Map<Label, Either3<Code2, List<Label>, Link>> labels) {
    return new Code2(Tag.PRODUCT, labels);
  }

}
