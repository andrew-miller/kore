package com.pokemon.kore.codes;

import static com.pokemon.kore.utils.Null.notNull;

import java.io.Serializable;

import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;

public final class Code implements Serializable {
  public enum Tag {
    UNION, PRODUCT
  }

  public final Tag tag;
  public final Map<Label, Either<Code, List<Label>>> labels;

  public Code(Tag tag, Map<Label, Either<Code, List<Label>>> labels) {
    notNull(tag, labels);
    this.tag = tag;
    this.labels = labels;
  }

  @Override
  public String toString() {
    return "Code [tag=" + tag + ", labels=" + labels + "]";
  }

  public static Code newUnion(Map<Label, Either<Code, List<Label>>> labels) {
    return new Code(Tag.UNION, labels);
  }

  public static Code newProduct(Map<Label, Either<Code, List<Label>>> labels) {
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
