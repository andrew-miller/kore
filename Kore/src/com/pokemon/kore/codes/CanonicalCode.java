package com.pokemon.kore.codes;

import java.io.Serializable;

import com.pokemon.kore.utils.CodeUtils;
import com.pokemon.kore.utils.List;

/** The image of <tt>CodeUtils.canonicalCode</tt> */
public class CanonicalCode implements Serializable {
  public final Code code;

  public CanonicalCode(Code c, List<Label> path) {
    code = CodeUtils.canonicalCode(c, path);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((code == null) ? 0 : code.hashCode());
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
    CanonicalCode other = (CanonicalCode) obj;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "CanonicalCode [code=" + code + "]";
  }
}
