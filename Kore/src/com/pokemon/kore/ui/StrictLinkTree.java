package com.pokemon.kore.ui;

import java.io.Serializable;

import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Pair;

/**
 * A directed cyclic graph represented by a spanning tree with links to nodes in
 * the spanning tree (represented by paths over the spanning tree from the root)
 */
public final class StrictLinkTree<E, V> implements Serializable {
  public final List<Pair<E, Either<StrictLinkTree<E, V>, List<E>>>> edges;
  public final V vertex;

  public StrictLinkTree(
      List<Pair<E, Either<StrictLinkTree<E, V>, List<E>>>> edges, V vertex) {
    this.edges = edges;
    this.vertex = vertex;
  }

  @Override
  public String toString() {
    return "StrictLinkTree [edges=" + edges + ", vertex=" + vertex + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((edges == null) ? 0 : edges.hashCode());
    result = prime * result + ((vertex == null) ? 0 : vertex.hashCode());
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
    StrictLinkTree other = (StrictLinkTree) obj;
    if (edges == null) {
      if (other.edges != null)
        return false;
    } else if (!edges.equals(other.edges))
      return false;
    if (vertex == null) {
      if (other.vertex != null)
        return false;
    } else if (!vertex.equals(other.vertex))
      return false;
    return true;
  }
}