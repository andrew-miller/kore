package com.example.kore.utils;

import static com.example.kore.utils.Null.notNull;

import java.io.Serializable;

/**
 * <code> tree(E,V) <is> {'v V, 'edges list(pair(E, tree(E,D)))};
 * Immutable, thread-safe, null-free.
 * 
 * default serialization
 */
public final class Tree<E, V> implements Serializable {

  public final V v;
  public final List<Pair<E, Tree<E, V>>> edges;

  public Tree(V v, List<Pair<E, Tree<E, V>>> edges) {
    notNull(v, edges);
    this.v = v;
    this.edges = edges;
  }

  @Override
  public String toString() {
    return "Tree [v=" + v + ", edges=" + edges + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((edges == null) ? 0 : edges.hashCode());
    result = prime * result + ((v == null) ? 0 : v.hashCode());
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
    Tree other = (Tree) obj;
    if (edges == null) {
      if (other.edges != null)
        return false;
    } else if (!edges.equals(other.edges))
      return false;
    if (v == null) {
      if (other.v != null)
        return false;
    } else if (!v.equals(other.v))
      return false;
    return true;
  }

}
