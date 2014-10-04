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

    @Override
    public String toString() {
      return "Link [hash=" + hash + ", path=" + path + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((hash == null) ? 0 : hash.hashCode());
      result = prime * result + ((path == null) ? 0 : path.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Link other = (Link) obj;
      if (hash == null) {
        if (other.hash != null)
          return false;
      } else if (!hash.equals(other.hash))
        return false;
      if (path == null) {
        if (other.path != null)
          return false;
      } else if (!path.equals(other.path))
        return false;
      return true;
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((labels == null) ? 0 : labels.hashCode());
    result = prime * result + ((tag == null) ? 0 : tag.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Code2 other = (Code2) obj;
    if (labels == null) {
      if (other.labels != null)
        return false;
    } else if (!labels.equals(other.labels))
      return false;
    if (tag != other.tag)
      return false;
    return true;
  }

}
