package com.example.kore.utils;

import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.ui.LinkTree;
import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.MapUtils.containsKey;
import static com.example.kore.utils.Pair.pair;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.jgrapht.graph.DirectedMultigraph;

public class LinkTreeUtils {

  /**
   * @return A pair <code>(g,r)</code> where <code>g</code> is a graph
   *         representing <code>lt</code> where each edge is a pair
   *         <code>(parent, edge)</code> and <code>r</code> corresponds to
   *         <code>lt</code>. For each <code>LinkTree</code> <code>t</code> in
   *         <code>lt</code>, for each outgoing edge
   *         <code>E</code> <code>e</code> of <code>t</code>, there is an edge
   *         <code>(p,e)</code> in <code>g</code> where <code>p</code> is
   *         the vertex corresponding to <code>t</code>.
   *         Note: Each edge is unique.
   */
  public static <E,V> Pair<DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>>, Identity<V>>
      linkTreeToGraph(LinkTree<E,V> lt) {
    DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g =
        new DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>>(
            (Class<Pair<Identity<V>, E>>) null);
    Identity<V> root = linkTreeSpanningTreeToGraph(g, lt);
    addLinksToLinkTreeGraph(lt, g, root, root);
    return pair(g, root);
  }

  private static <E, V> Identity<V>
      linkTreeSpanningTreeToGraph(DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g, LinkTree<E, V> lt) {
    Identity<V> v = new Identity<V>(lt.vertex());
    g.addVertex(v);
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> e : iter(lt.edges())) {
      Either<LinkTree<E, V>, List<E>> vp = e.y;
      if (vp.isY()) continue;
      Identity<V> v2 = linkTreeSpanningTreeToGraph(g, vp.x());
      g.addEdge(v, v2, pair(v, e.x));
    }
    return v;
  }

  private static <E, V> void
      addLinksToLinkTreeGraph(LinkTree<E, V> lt,
          DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g,
          Identity<V> root, Identity<V> parent) {
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> e : iter(lt.edges())) {
      Either<LinkTree<E, V>, List<E>> vp = e.y;
      if (vp.isY()) {
        Identity<V> v = root;
        for (E e_:iter(vp.y()))
          v= g.getEdgeTarget(pair(v, e_));
        g.addEdge(parent, v, pair(parent,e.x));
      } else {
        addLinksToLinkTreeGraph(vp.x(), g, root,
            g.getEdgeTarget(pair(parent, e.x)));
      }
    }
  }

  /**
   * For all <code>LinkTree</code>s <code>(t1,t2)</code>, if <code>t1</code>
   * has the same graph as <tt>t2</tt>, then <code>canonicalLinkTree(t1)</code>
   * will be structurally equivalent to <code>canonicalLinkTree(t2)</code>
   */
  public static <E,V> LinkTree<E,V> canonicalLinkTree(LinkTree<E,V> lt, List<E> path,
      Comparer<E> c) {
    Pair<DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>>, Identity<V>> p =
        linkTreeToGraph(lt);
    DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g = p.x;
    Identity<V> root = p.y;
    Set<Pair<Identity<V>, E>> spanningTreeEdges =
        new HashSet<Pair<Identity<V>, E>>();
    Ref<Map<Identity<V>, List<E>>> m =
        new Ref<Map<Identity<V>, List<E>>>(
            Map.<Identity<V>, List<E>> empty());
    Identity<V> r = followPath(path, g, root);
    buildCanonicalSpanningTreeOfLinkTreeGraph(g, r, m, ListUtils.<E> nil(),
        spanningTreeEdges, c);
    return buildLinkTreeFromSpanningTree(g, m.get(), r, spanningTreeEdges);
  }

  private static <E, V> LinkTree<E, V> buildLinkTreeFromSpanningTree(
      final DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g,
      final Map<Identity<V>, List<E>> m, final Identity<V> v,
      final Set<Pair<Identity<V>, E>> spanningTreeEdges) {
    Map<Label, CodeOrPath> lm = Map.empty();
    return new LinkTree<E, V>() { /* XXX Leaks capability to use mutable data
                                   *     (g). Rewrite to be strict */
      public List<Pair<E, Either<LinkTree<E, V>, List<E>>>> edges() {
        List<Pair<E, Either<LinkTree<E, V>, List<E>>>> l = nil();
        for (Pair<Identity<V>, E> e : g.outgoingEdgesOf(v))
          l = cons(pair(e.y, spanningTreeEdges.contains(e)
              ? Either.<LinkTree<E, V>, List<E>> x(
                  buildLinkTreeFromSpanningTree(g, m,g.getEdgeTarget(e),
                      spanningTreeEdges))
              : Either.<LinkTree<E, V>, List<E>> y(
                  m.get(g.getEdgeTarget(e)).some().x))
          , l);
        return l;
      }

      public V vertex() {
        return v.t;
      }
    };
  }

  private static <E, V> void buildCanonicalSpanningTreeOfLinkTreeGraph(
      DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g, Identity<V> v,
      Ref<Map<Identity<V>, List<E>>> m, List<E> path,
      Set<Pair<Identity<V>, E>> spanningTreeEdges, final Comparer<E> c) {
    m.set(m.get().put(v, path));
    SortedMap<E, Pair<Identity<V>, E>> sm =
        new TreeMap<E, Pair<Identity<V>, E>>(new Comparator<E>() {
      public int compare(E a, E b) {
        switch (c.compare(a, b)) {
          case EQ:
            return 0;
          case GT:
            return 1;
          case LT:
            return -1;
          default: throw boom();
        }
      }
    });
    for (Pair<Identity<V>, E> e : g.outgoingEdgesOf(v))
      sm.put(e.y, e);
    for (Pair<Identity<V>, E> e : sm.values()) {
      Identity<V> v2 = g.getEdgeTarget(e);
      if (!containsKey(m.get(), v2)) {
        buildCanonicalSpanningTreeOfLinkTreeGraph(g, v2, m, append(e.y, path),
            spanningTreeEdges, c);
        spanningTreeEdges.add(e);
      }
    }
  }

  public static <E, V> Identity<V>
      followPath(List<E> path, DirectedMultigraph<Identity<V>,
        Pair<Identity<V>, E>> g, Identity<V> r) {
    for (E e : iter(path))
      r = g.getEdgeTarget(pair(r, e));
    return r;
  }

  /** @return true if <code>lt</code> contains a link to the node
   *          designated by <code>path</code> or any child of it */
  public static <E,V> boolean isReferenced(LinkTree<E,V> lt, List<E> path) {
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> p : iter(lt.edges())) {
      if (p.y.isY()) {
        if (ListUtils.isSubList(path, p.y.y()))
          return true;
      } else {
        if (isReferenced(p.y.x(), path))
          return true;
      }
    }
    return false;
  }

  public static <E,V> boolean validLinkTree(LinkTree<E,V> lt) {
    return validLinkTree(lt, lt);
  }

  private static <E, V> boolean validLinkTree(LinkTree<E, V> root, LinkTree<E, V> lt) {
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> p : iter(lt.edges())) {
      if (p.y.isY()) {
        if (!validPath(root, p.y.y()))
          return false;
      } else {
        if (!validLinkTree(root, p.y.x()))
          return false;
      }
    }
    return true;
  }

  public static <E, V> boolean validPath(LinkTree<E, V> lt, List<E> path) {
    if (path.isEmpty()) return true;
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> p : iter(lt.edges()))
      if (p.x.equals(path.cons().x))
        return p.y.isY() ? false : validPath(p.y.x(), path.cons().tail);
    return false;
  }

}