package com.example.kore.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author wojtek
 * 
 * @param <V>
 *          -- vertex type
 * @param <L>
 *          -- label type (i.e., name of an edge)
 * @param <G>
 *          -- alias to LGraph<V,L,G>
 * 
 */
public class LGraph<V, L, G extends LGraph<V, L, G>> {
  final public V node;
  final public Map<L, G> edges;
  final public List<L> order;

  public LGraph(V node, Map<L, G> edges) {
    this(node, edges, null);
  }

  public LGraph(V node, Map<L, G> edges, List<L> order) {
    this.node = node;
    this.edges = edges;
    if (order != null && edges.keySet().equals(new HashSet<L>(order))) {
      this.order = order;
    } else {
      this.order = new ArrayList<L>(edges.keySet());
    }
  }

  public G walk(List<L> path) {
    if (path == null)
      return null;
    if (path.size() > 0)
      return edges.get(path.get(0)).walk(path.subList(1, path.size()));
    return (G) this;
  }

  public boolean eq(G o) {
    return eq(o, new Equivalence<G>());
  }

  private boolean eq(G o, Equivalence<G> B) {
    if (!node.equals(o.node))
      return false;
    if (!order.equals(o.order))
      return false;
    B.add((G) this, o);
    for (L l : order) {
      if (!edges.get(l).eq(o.edges.get(l), B))
        return false;
    }
    return true;
  }

}
