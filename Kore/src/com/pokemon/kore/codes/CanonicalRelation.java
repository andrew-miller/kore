package com.pokemon.kore.codes;

import java.io.Serializable;

import com.pokemon.kore.ui.RelationUtils;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Unit;

/** The image of <tt>RelationUtils.canonicalRelation</tt> */
public class CanonicalRelation implements Serializable {
  public final Relation relation;

  public CanonicalRelation(Relation r, List<Either3<Label, Integer, Unit>> path) {
    relation = RelationUtils.canonicalRelation(r, path);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((relation == null) ? 0 : relation.hashCode());
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
    CanonicalRelation other = (CanonicalRelation) obj;
    if (relation == null) {
      if (other.relation != null)
        return false;
    } else if (!relation.equals(other.relation))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "CanonicalRelation [relation=" + relation + "]";
  }

}
