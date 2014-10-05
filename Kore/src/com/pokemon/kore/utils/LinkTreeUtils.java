package com.pokemon.kore.utils;

import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.cons;
import static com.pokemon.kore.utils.ListUtils.drop;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.MapUtils.containsKey;
import static com.pokemon.kore.utils.PairUtils.pair;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DirectedMultigraph;

import com.pokemon.kore.codes.З2Bytes;
import com.pokemon.kore.ui.LinkTree;
import com.pokemon.kore.ui.LinkTree2;
import com.pokemon.kore.ui.StrictLinkTree;

public class LinkTreeUtils {

  public interface Resolver<E, V> {
    public Optional<LinkTree2<E, V>> resolve(Pair<З2Bytes, List<E>> l);
  }

  public interface Hasher<E, V> {
    public З2Bytes hash(LinkTree<E, V> lt);
  }

  public interface Hasher2<E, V> {
    public З2Bytes hash(StrictLinkTree<E, V> lt);
  }

  /**
   * Decompose a <code>LinkTree</code> into strongly connected components with
   * hash links to each other
   */
  public static
      <E, V, L>
      Pair<Map<З2Bytes, StrictLinkTree<E, Either<L, V>>>, StrictLinkTree<E, Either<L, V>>>
      decompose3(LinkTree<E, Either<L, V>> lt, Hasher2<E, Either<L, V>> h,
          F<З2Bytes, StrictLinkTree<E, Either<L, V>>> makeLink) {
    Pair<DirectedMultigraph<Identity<Either<L, V>>, Pair<Identity<Either<L, V>>, E>>, Identity<Either<L, V>>> gr =
        linkTreeToGraph(lt);
    return decompose3(Map.empty(), gr.x,
        new StrongConnectivityInspector<>(gr.x).stronglyConnectedSets(), gr.y,
        lt, lt, 0, 0, h, makeLink);
  }

  private static
      <E, V, L>
      Pair<Map<З2Bytes, StrictLinkTree<E, Either<L, V>>>, StrictLinkTree<E, Either<L, V>>>
      decompose3(
          Map<З2Bytes, StrictLinkTree<E, Either<L, V>>> m,
          DirectedMultigraph<Identity<Either<L, V>>, Pair<Identity<Either<L, V>>, E>> g,
          java.util.List<Set<Identity<Either<L, V>>>> sccs,
          Identity<Either<L, V>> v, LinkTree<E, Either<L, V>> ltv,
          LinkTree<E, Either<L, V>> ltr, Integer height, Integer sccRootHeight,
          Hasher2<E, Either<L, V>> h,
          F<З2Bytes, StrictLinkTree<E, Either<L, V>>> makeLink) {
    Set<Identity<Either<L, V>>> scc = containingSCC(sccs, v);
    List<Pair<E, Either<StrictLinkTree<E, Either<L, V>>, List<E>>>> edges =
        nil();
    for (Pair<E, Either<LinkTree<E, Either<L, V>>, List<E>>> e : iter(ltv
        .edges())) {
      Identity<Either<L, V>> t = g.getEdgeTarget(pair(v, e.x));
      Either<StrictLinkTree<E, Either<L, V>>, List<E>> t2;
      Pair<Map<З2Bytes, StrictLinkTree<E, Either<L, V>>>, StrictLinkTree<E, Either<L, V>>> p;
      if (scc.contains(t)) {
        if (e.y.tag == e.y.tag.Y)
          t2 = Either.y(drop(e.y.y(), sccRootHeight));
        else {
          p =
              decompose3(m, g, sccs, t, e.y.x(), ltr, height + 1,
                  sccRootHeight, h, makeLink);
          m = p.x;
          t2 = Either.x(p.y);
        }
      } else {
        if (e.y.tag == e.y.tag.Y) {
          p =
              decompose3(m, g, sccs, t, linkTreeOrPathAt(ltr, e.y.y()).x(),
                  ltr, height + 1, height + 1, h, makeLink);
          З2Bytes hash = h.hash(p.y);
          t2 = Either.x(makeLink.f(hash));
          m = p.x.put(hash, p.y);
        } else {
          switch (e.y.x().vertex().tag) {
          case X:
            t2 = Either.x(new StrictLinkTree<>(nil(), e.y.x().vertex()));
            break;
          case Y:
            p =
                decompose3(m, g, sccs, t, e.y.x(), ltr, height + 1, height + 1,
                    h, makeLink);
            З2Bytes hash = h.hash(p.y);
            t2 = Either.x(makeLink.f(hash));
            m = p.x.put(hash, p.y);
            break;
          default:
            throw boom();
          }
        }
      }
      edges = cons(pair(e.x, t2), edges);
    }
    return pair(m, new StrictLinkTree<E, Either<L, V>>(edges, v.t));
  }

  /**
   * Decompose a <code>LinkTree</code> into strongly connected components with
   * hash links to each other
   */
  public static <E, V> StrictLinkTree<E, Either<З2Bytes, V>> decompose2(
      LinkTree<E, V> lt, Hasher2<E, Either<З2Bytes, V>> h) {
    Pair<DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>>, Identity<V>> gr =
        linkTreeToGraph(lt);
    return decompose2(gr.x,
        new StrongConnectivityInspector<>(gr.x).stronglyConnectedSets(), gr.y,
        lt, lt, 0, 0, h);
  }

  private static <E, V> StrictLinkTree<E, Either<З2Bytes, V>> decompose2(
      DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g,
      java.util.List<Set<Identity<V>>> sccs, Identity<V> v, LinkTree<E, V> ltv,
      LinkTree<E, V> ltr, Integer height, Integer sccRootHeight,
      Hasher2<E, Either<З2Bytes, V>> h) {
    Set<Identity<V>> scc = containingSCC(sccs, v);
    List<Pair<E, Either<StrictLinkTree<E, Either<З2Bytes, V>>, List<E>>>> edges =
        nil();
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> e : iter(ltv.edges())) {
      Identity<V> t = g.getEdgeTarget(pair(v, e.x));
      Either<StrictLinkTree<E, Either<З2Bytes, V>>, List<E>> t2;
      if (scc.contains(t)) {
        if (e.y.tag == e.y.tag.Y)
          t2 = Either.y(drop(e.y.y(), sccRootHeight));
        else
          t2 =
              Either.x(decompose2(g, sccs, t, e.y.x(), ltr, height + 1,
                  sccRootHeight, h));
      } else {
        if (e.y.tag == e.y.tag.Y)
          t2 =
              Either.x(new StrictLinkTree<>(nil(), Either.x(h.hash(decompose2(
                  g, sccs, t, linkTreeOrPathAt(ltr, e.y.y()).x(), ltr,
                  height + 1, height + 1, h)))));
        else
          t2 =
              Either.x(new StrictLinkTree<>(nil(), Either.x(h.hash(decompose2(
                  g, sccs, t, e.y.x(), ltr, height + 1, height + 1, h)))));
      }
      edges = cons(pair(e.x, t2), edges);
    }
    return new StrictLinkTree<E, Either<З2Bytes, V>>(edges, Either.y(v.t));
  }

  /**
   * Decompose a <code>LinkTree</code> into strongly connected components with
   * hash links to each other
   */
  public static <E, V> LinkTree<E, Either<З2Bytes, V>> decompose(
      LinkTree<E, V> lt, Hasher<E, Either<З2Bytes, V>> h) {
    Pair<DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>>, Identity<V>> gr =
        linkTreeToGraph(lt);
    return decompose(gr.x,
        new StrongConnectivityInspector<>(gr.x).stronglyConnectedSets(), gr.y,
        lt, lt, 0, 0, h);
  }

  private static <E, V> LinkTree<E, Either<З2Bytes, V>> decompose(
      DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g,
      java.util.List<Set<Identity<V>>> sccs, Identity<V> v, LinkTree<E, V> ltv,
      LinkTree<E, V> ltr, Integer height, Integer sccRootHeight,
      Hasher<E, Either<З2Bytes, V>> h) {
    Set<Identity<V>> scc = containingSCC(sccs, v);
    return new LinkTree<E, Either<З2Bytes, V>>() {
      public List<Pair<E, Either<LinkTree<E, Either<З2Bytes, V>>, List<E>>>>
          edges() {
        List<Pair<E, Either<LinkTree<E, Either<З2Bytes, V>>, List<E>>>> es =
            nil();
        for (Pair<E, Either<LinkTree<E, V>, List<E>>> e : iter(ltv.edges())) {
          Identity<V> t = g.getEdgeTarget(pair(v, e.x));
          Either<LinkTree<E, Either<З2Bytes, V>>, List<E>> t2;
          if (scc.contains(t)) {
            if (e.y.tag == e.y.tag.Y)
              t2 = Either.y(drop(e.y.y(), sccRootHeight));
            else
              t2 =
                  Either.x(decompose(g, sccs, t, e.y.x(), ltr, height + 1,
                      sccRootHeight, h));
          } else {
            if (e.y.tag == e.y.tag.Y)
              t2 =
                  Either.x(decompose(g, sccs, t, linkTreeOrPathAt(ltr, e.y.y())
                      .x(), ltr, height + 1, height + 1, h));
            else
              t2 =
                  Either.x(decompose(g, sccs, t, e.y.x(), ltr, height + 1,
                      height + 1, h));
          }
          es = cons(pair(e.x, t2), es);
        }
        return es;
      }

      public Either<З2Bytes, V> vertex() {
        return height != 0 & height == sccRootHeight ? Either.x(h.hash(this))
            : Either.y(v.t);
      }
    };
  }

  private static <V> Set<Identity<V>> containingSCC(
      java.util.List<Set<Identity<V>>> sccs, Identity<V> v) {
    for (Set<Identity<V>> scc : sccs)
      if (scc.contains(v))
        return scc;
    throw boom();
  }

  /**
   * a <code>LinkTree</code> representing the same graph as <code>lt</code>, but
   * rooted at <code>path</code>
   */
  public static <E, V> LinkTree<E, V> reroot(LinkTree<E, V> lt, List<E> path) {
    Pair<DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>>, Identity<V>> p =
        linkTreeToGraph(lt);
    DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g = p.x;
    Identity<V> root = p.y;
    Set<Pair<Identity<V>, E>> spanningTreeEdges = new HashSet<>();
    Ref<Map<Identity<V>, List<E>>> m = new Ref<>(Map.empty());
    Identity<V> r = followPath(path, g, root);
    buildSpanningTreeOfCodeGraph(g, r, m, nil(), spanningTreeEdges);
    return buildLinkTreeFromSpanningTree(g, m.get(), r, spanningTreeEdges);
  }

  private static <E, V> void buildSpanningTreeOfCodeGraph(
      DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g, Identity<V> r,
      Ref<Map<Identity<V>, List<E>>> m, List<E> list,
      Set<Pair<Identity<V>, E>> spanningTreeEdges) {
    m.set(m.get().put(r, list));
    for (Pair<Identity<V>, E> e : g.outgoingEdgesOf(r)) {
      Identity<V> v2 = g.getEdgeTarget(e);
      if (!containsKey(m.get(), v2)) {
        buildSpanningTreeOfCodeGraph(g, v2, m, append(e.y, list),
            spanningTreeEdges);
        spanningTreeEdges.add(e);
      }
    }
  }

  /** prepend <tt>p</tt> to all paths */
  public static <E, V> LinkTree<E, V> rebase(List<E> p, LinkTree<E, V> lt) {
    return mapPaths(lt, p2 -> append(p, p2));
  }

  public static <E, V> LinkTree<E, V> mapPaths(LinkTree<E, V> lt,
      F<List<E>, List<E>> f) {
    return new LinkTree<E, V>() {
      public List<Pair<E, Either<LinkTree<E, V>, List<E>>>> edges() {
        List<Pair<E, Either<LinkTree<E, V>, List<E>>>> l = nil();
        for (Pair<E, Either<LinkTree<E, V>, List<E>>> e : iter(lt.edges())) {
          switch (e.y.tag) {
          case X:
            l = cons(pair(e.x, Either.x(mapPaths(e.y.x(), f))), l);
            break;
          case Y:
            l = cons(pair(e.x, Either.y(f.f(e.y.y()))), l);
            break;
          }
        }
        return l;
      }

      public V vertex() {
        return lt.vertex();
      }
    };
  }

  /**
   * @return A pair <code>(g,r)</code> where <code>g</code> is a graph
   *         representing <code>lt</code> where each edge is a pair
   *         <code>(parent, edge)</code> and <code>r</code> corresponds to
   *         <code>lt</code>. For each <code>LinkTree</code> <code>t</code> in
   *         <code>lt</code>, for each outgoing edge <code>E</code>
   *         <code>e</code> of <code>t</code>, there is an edge
   *         <code>(p,e)</code> in <code>g</code> where <code>p</code> is the
   *         vertex corresponding to <code>t</code>. Note: Each edge is unique.
   */
  public static <E, V>
      Pair<DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>>, Identity<V>>
      linkTreeToGraph(LinkTree<E, V> lt) {
    DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g =
        new DirectedMultigraph<>((Class<Pair<Identity<V>, E>>) null);
    Identity<V> root = linkTreeSpanningTreeToGraph(g, lt);
    addLinksToLinkTreeGraph(lt, g, root, root);
    return pair(g, root);
  }

  private static <E, V> Identity<V>
      linkTreeSpanningTreeToGraph(
          DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g,
          LinkTree<E, V> lt) {
    Identity<V> v = new Identity<>(lt.vertex());
    g.addVertex(v);
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> e : iter(lt.edges())) {
      Either<LinkTree<E, V>, List<E>> vp = e.y;
      if (vp.tag == vp.tag.Y)
        continue;
      Identity<V> v2 = linkTreeSpanningTreeToGraph(g, vp.x());
      g.addEdge(v, v2, pair(v, e.x));
    }
    return v;
  }

  private static <E, V> void addLinksToLinkTreeGraph(LinkTree<E, V> lt,
      DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g,
      Identity<V> root, Identity<V> parent) {
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> e : iter(lt.edges())) {
      switch (e.y.tag) {
      case Y:
        Identity<V> v = root;
        for (E e_ : iter(e.y.y()))
          v = g.getEdgeTarget(pair(v, e_)); // NPE in getEdgeTarget
        g.addEdge(parent, v, pair(parent, e.x));
        break;
      case X:
        addLinksToLinkTreeGraph(e.y.x(), g, root,
            g.getEdgeTarget(pair(parent, e.x)));
        break;
      }
    }
  }

  /**
   * For all <code>LinkTree</code>s <code>(t1,t2)</code>, if <code>t1</code> has
   * the same graph as <tt>t2</tt>, then <code>canonicalLinkTree(t1)</code> will
   * be structurally equivalent to <code>canonicalLinkTree(t2)</code>
   */
  public static <E, V> LinkTree<E, V> canonicalLinkTree(LinkTree<E, V> lt,
      List<E> path, Comparer<E> c) {
    Pair<DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>>, Identity<V>> p =
        linkTreeToGraph(lt);
    DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g = p.x;
    Identity<V> root = p.y;
    Set<Pair<Identity<V>, E>> spanningTreeEdges = new HashSet<>();
    Ref<Map<Identity<V>, List<E>>> m = new Ref<>(Map.empty());
    Identity<V> r = followPath(path, g, root);
    buildCanonicalSpanningTreeOfLinkTreeGraph(g, r, m, nil(),
        spanningTreeEdges, c);
    return buildLinkTreeFromSpanningTree(g, m.get(), r, spanningTreeEdges);
  }

  private static <E, V> LinkTree<E, V> buildLinkTreeFromSpanningTree(
      DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g,
      Map<Identity<V>, List<E>> m, Identity<V> v,
      Set<Pair<Identity<V>, E>> spanningTreeEdges) {
    return new LinkTree<E, V>() { /*
                                   * XXX Leaks capability to use mutable data
                                   * (g). Rewrite to be strict
                                   */
      public List<Pair<E, Either<LinkTree<E, V>, List<E>>>> edges() {
        List<Pair<E, Either<LinkTree<E, V>, List<E>>>> l = nil();
        for (Pair<Identity<V>, E> e : g.outgoingEdgesOf(v))
          l =
              cons(
                  pair(
                      e.y,
                      spanningTreeEdges.contains(e) ? Either
                          .x(buildLinkTreeFromSpanningTree(g, m,
                              g.getEdgeTarget(e), spanningTreeEdges)) : Either
                          .y(m.get(g.getEdgeTarget(e)).some().x)), l);
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
      Set<Pair<Identity<V>, E>> spanningTreeEdges, Comparer<E> c) {
    m.set(m.get().put(v, path));
    SortedMap<E, Pair<Identity<V>, E>> sm = new TreeMap<>((a, b) -> {
      switch (c.compare(a, b)) {
      case EQ:
        return 0;
      case GT:
        return 1;
      case LT:
        return -1;
      default:
        throw boom();
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

  public static <E, V> Either<LinkTree<E, V>, List<E>> linkTreeOrPathAt(
      LinkTree<E, V> lt, List<E> p) {
    return linkTreeOrPathAt_(Either.x(lt), p);
  }

  public static <E, V> Either<LinkTree<E, V>, List<E>> linkTreeOrPathAt_(
      Either<LinkTree<E, V>, List<E>> ltp, List<E> p) {
    if (p.isEmpty())
      return ltp;
    return linkTreeOrPathAt_(getEdge(ltp.x(), p.cons().x), p.cons().tail);
  }

  public static <E, V> Either<LinkTree<E, V>, List<E>> getEdge(
      LinkTree<E, V> lt, E e) {
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> p : iter(lt.edges()))
      if (p.x.equals(e))
        return p.y;
    throw boom();
  }

  public static <E, V> Identity<V> followPath(List<E> path,
      DirectedMultigraph<Identity<V>, Pair<Identity<V>, E>> g, Identity<V> r) {
    for (E e : iter(path))
      r = g.getEdgeTarget(pair(r, e));
    return r;
  }

  /**
   * @return true if <code>lt</code> contains a link to the node designated by
   *         <code>path</code> or any child of it
   */
  public static <E, V> boolean isReferenced(LinkTree<E, V> lt, List<E> path) {
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> p : iter(lt.edges())) {
      switch (p.y.tag) {
      case Y:
        if (ListUtils.isPrefix(path, p.y.y()))
          return true;
        break;
      case X:
        if (isReferenced(p.y.x(), path))
          return true;
        break;
      }
    }
    return false;
  }

  public static <E, V> boolean validLinkTree(LinkTree<E, V> lt) {
    return validLinkTree(lt, lt);
  }

  private static <E, V> boolean validLinkTree(LinkTree<E, V> root,
      LinkTree<E, V> lt) {
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> p : iter(lt.edges())) {
      switch (p.y.tag) {
      case Y:
        if (!validPath(root, p.y.y()))
          return false;
        break;
      case X:
        if (!validLinkTree(root, p.y.x()))
          return false;
        break;
      }
    }
    return true;
  }

  public static <E, V> boolean validPath(LinkTree<E, V> lt, List<E> path) {
    if (path.isEmpty())
      return true;
    for (Pair<E, Either<LinkTree<E, V>, List<E>>> p : iter(lt.edges()))
      if (p.x.equals(path.cons().x))
        return p.y.tag == p.y.tag.Y ? false : validPath(p.y.x(),
            path.cons().tail);
    return false;
  }

}