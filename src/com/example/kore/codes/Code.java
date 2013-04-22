package com.example.kore.codes;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.example.kore.utils.Null;

public final class Code implements Serializable {
  public enum Tag {
    UNION, PRODUCT
  }

  public final Tag tag;
  public final Map<Label, CodeOrPath> labels;

  public Code(Tag tag, Map<Label, CodeOrPath> labels) {
    Null.notNull(tag, labels);
    this.tag = tag;
    this.labels = Collections.unmodifiableMap(labels);
  }

  @Override
  public String toString() {
    return "Code [tag=" + tag + ", labels=" + labels + "]";
  }

  public final static Code newUnion(Map<Label, CodeOrPath> labels) {
    return new Code(Tag.UNION, labels);
  }

  public static Code newProduct(Map<Label, CodeOrPath> labels) {
    return new Code(Tag.PRODUCT, labels);
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
    Code other = (Code) obj;
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
