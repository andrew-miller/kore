package com.pokemon.kore.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Labeled ordered directed graph
 * 
 * @author wojtek
 * 
 * @param <V>
 *          -- vertex type
 * @param <L>
 *          -- label type (i.e., name of an edge)
 * 
 */
public final class LGraph<V, L> {
  final public V node;
  final public Map<L, LGraph<V, L>> edges;
  final public List<L> order;

  public LGraph(V node, Map<L, LGraph<V, L>> edges) {
    this(node, edges, null);
  }

  public LGraph(V node, Map<L, LGraph<V, L>> edges, List<L> order) {
    this.node = node;
    this.edges = edges;
    if (order != null && edges.keySet().equals(new HashSet<>(order))) {
      this.order = order;
    } else {
      this.order = new ArrayList<>(edges.keySet());
    }
  }

  public LGraph<V, L> walk(List<L> path) {
    if (path == null)
      return null;
    if (path.size() > 0)
      return edges.get(path.get(0)).walk(path.subList(1, path.size()));
    return this;
  }

  public boolean eq(LGraph<V, L> o) {
    return eq(o, new Equivalence<LGraph<V, L>>());
  }

  @SuppressWarnings("unchecked")
  private boolean eq(LGraph<V, L> o, Equivalence<LGraph<V, L>> B) {
    if (B.eq(this, o))
      return true;
    if (!node.equals(o.node))
      return false;
    if (!order.equals(o.order))
      return false;
    B.add(this, o);
    for (L l : order) {
      if (!edges.get(l).eq(o.edges.get(l), B))
        return false;
    }
    return true;
  }

}
