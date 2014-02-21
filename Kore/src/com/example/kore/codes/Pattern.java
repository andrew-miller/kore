package com.example.kore.codes;

import java.io.Serializable;

import com.example.kore.utils.Map;

public final class Pattern implements Serializable {
  public final Map<Label, Pattern> fields;

  public Pattern(Map<Label, Pattern> fields) {
    this.fields = fields;
  }

  @Override
  public String toString() {
    return "Pattern [fields=" + fields + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fields == null) ? 0 : fields.hashCode());
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
    Pattern other = (Pattern) obj;
    if (fields == null) {
      if (other.fields != null)
        return false;
    } else if (!fields.equals(other.fields))
      return false;
    return true;
  }
}
