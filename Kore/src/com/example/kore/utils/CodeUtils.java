package com.example.kore.utils;

import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.MapUtils.containsKey;
import static com.example.kore.utils.MapUtils.values;
import static com.example.kore.utils.OptionalUtils.nothing;
import static com.example.kore.utils.OptionalUtils.some;
import static com.example.kore.utils.PairUtils.pair;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DirectedMultigraph;

import android.util.Log;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Code.Tag;
import com.example.kore.codes.Label;
import com.example.kore.ui.CodeLabelAliasMap;
import com.example.kore.ui.LinkTree;

public final class CodeUtils {
  private final static String className = CodeUtils.class.getName();

  public final static Code unit = Code.newProduct(Map
      .<Label, Either<Code, List<Label>>> empty());

  public static Code reroot(Code c, List<Label> p) {
    return linkTreeToCode(LinkTreeUtils.reroot(linkTree(c), p));
  }

  public static String renderCode(Code c, List<Label> p,
      CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, int depth) {
    return renderCode(p, c, codeOrPathAt(p, c), codeLabelAliases, codeAliases,
        depth);
  }

  private static String renderCode(List<Label> path, Code root,
      Either<Code, List<Label>> cp, CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, int depth) {
    if (depth < 0)
      throw new RuntimeException("negative depth");
    if (depth == 0)
      return "...";
    CanonicalCode cc = new CanonicalCode(root, path);
    Optional<String> codeAlias = codeAliases.get(cc);
    if (!codeAlias.isNothing())
      return codeAlias.some().x;
    if (cp.tag == cp.tag.Y)
      return "^";
    Code c = cp.x();
    String start;
    String end;
    switch (c.tag) {
    case UNION:
      start = "[";
      end = "]";
      break;
    case PRODUCT:
      start = "{";
      end = "}";
      break;
    default:
      throw Boom.boom();
    }
    Map<Label, String> labelAliases = codeLabelAliases.getAliases(cc);
    String result = "";
    for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels.entrySet())) {
      Label l = e.x;
      if (result.equals(""))
        result = "'";
      else
        result += ", '";
      Optional<String> la = labelAliases.get(l);
      String ls = la.isNothing() ? l.label : la.some().x;
      result +=
          (ls + " " + renderCode(append(l, path), root, e.y, codeLabelAliases,
              codeAliases, depth - 1));
    }
    return start + result + end;
  }

  /**
   * @return a pair <tt>(c', i)</tt> where <tt>c'</tt> is isomorphic to
   *         <tt>c</tt>. All labels of the nodes within the strongly connected
   *         components containing any node on <tt>path</tt> are randomized.
   *         <tt>i</tt> is the isomorphism itself.
   */
  public static Pair<Code, Map<List<Label>, Map<Label, Label>>> disassociate(
      Code c, List<Label> path) {
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        codeToGraph(c);
    Set<Identity<Tag>> vs = new HashSet<Identity<Tag>>();
    for (Set<Identity<Tag>> scc : new StrongConnectivityInspector<Identity<Tag>, Pair<Identity<Tag>, Label>>(
        p.x).stronglyConnectedSets()) {
      Identity<Tag> v = p.y;
      for (Label l : iter(path)) {
        if (scc.contains(v))
          vs.addAll(scc);
        v = p.x.getEdgeTarget(pair(v, l));
      }
      if (scc.contains(v))
        vs.addAll(scc);
    }
    Ref<Map<List<Label>, Map<Label, Label>>> i =
        new Ref<Map<List<Label>, Map<Label, Label>>>(
            Map.<List<Label>, Map<Label, Label>> empty());
    return pair(
        mapPaths(dissassociate_(p.x, p.y, c, vs, i, ListUtils.<Label> nil()),
            i.get(), ListUtils.<Label> nil()), i.get());
  }

  private static Code mapPaths(Code c, Map<List<Label>, Map<Label, Label>> i,
      List<Label> path) {
    Map<Label, Either<Code, List<Label>>> m2 = Map.empty();
    for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels.entrySet()))
      if (e.y.tag == e.y.tag.Y)
        m2 = m2.put(e.x, Either.<Code, List<Label>> y(mapPath(e.y.y(), i)));
      else
        m2 =
            m2.put(
                e.x,
                Either.<Code, List<Label>> x(mapPaths(e.y.x(), i,
                    append(e.x, path))));
    return new Code(c.tag, m2);
  }

  public static List<Label> mapPath(List<Label> path,
      Map<List<Label>, Map<Label, Label>> m) {
    List<Label> p = nil();
    List<Label> b = nil();
    for (Label l : iter(path)) {
      p = append(m.get(b).some().x.get(l).some().x, p);
      b = append(l, b);
    }
    return p;
  }

  private static Code dissassociate_(
      DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g,
      Identity<Tag> v, Code c, Set<Identity<Tag>> vs,
      Ref<Map<List<Label>, Map<Label, Label>>> i, List<Label> path) {
    boolean randomize = vs.contains(v);
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    Map<Label, Label> lm = Map.empty();
    for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels.entrySet())) {
      Label l = null;
      if (randomize)
        do {
          if (l != null)
            Log.e(className, "generated duplicate label");
          l = new Label(Random.randomId());
        } while (containsKey(m, l));
      else
        l = e.x;
      lm = lm.put(e.x, l);
      m =
          m.put(
              l,
              e.y.tag == e.y.tag.X ? Either
                  .<Code, List<Label>> x(dissassociate_(g,
                      g.getEdgeTarget(pair(v, e.x)), e.y.x(), vs, i,
                      append(e.x, path))) : e.y);
    }
    i.set(i.get().put(path, lm));
    return new Code(c.tag, m);
  }

  /** Code at the end of the simple path <tt>p</tt> from <tt>c</tt> */
  public static Optional<Code> codeAt(List<Label> p, Code c) {
    for (Label l : iter(p)) {
      Optional<Either<Code, List<Label>>> ocp = c.labels.get(l);
      if (ocp.isNothing())
        return nothing();
      Either<Code, List<Label>> cp = ocp.some().x;
      if (cp.tag != cp.tag.X)
        return nothing();
      c = cp.x();
    }
    return some(c);
  }

  private static Either<Code, List<Label>>
      codeOrPathAt(List<Label> path, Code c) {
    Either<Code, List<Label>> cp = Either.<Code, List<Label>> x(c);
    for (Label l : iter(path))
      cp = cp.x().labels.get(l).some().x;
    return cp;
  }

  public static List<Label> longestValidSubPath(List<Label> path, Code c) {
    List<Label> p = nil();
    for (Label l : iter(path)) {
      Optional<Either<Code, List<Label>>> ocp = c.labels.get(l);
      if (ocp.isNothing())
        return p;
      Either<Code, List<Label>> cp = ocp.some().x;
      if (cp.tag != cp.tag.X)
        return p;
      c = cp.x();
      p = append(l, p);
    }
    return p;
  }

  public static Code replaceCodeAt(Code c, List<Label> p,
      Either<Code, List<Label>> newCode) {
    return replaceCodeAt_(Either.<Code, List<Label>> x(c), p, newCode).x();
  }

  private static Either<Code, List<Label>> replaceCodeAt_(
      Either<Code, List<Label>> c, List<Label> p,
      Either<Code, List<Label>> newCode) {
    if (p.isEmpty())
      return newCode;
    Label l = p.cons().x;
    Map<Label, Either<Code, List<Label>>> m =
        c.x().labels
            .put(
                l,
                replaceCodeAt_(c.x().labels.get(l).some().x, p.cons().tail,
                    newCode));
    return Either.<Code, List<Label>> x(new Code(c.x().tag, m));
  }

  /**
   * @return a pair <tt>(g,r)</tt> where <tt>g</tt> is a graph representing
   *         <tt>c</tt> where each edge is a pair <tt>(parent, label)</tt> and
   *         <tt>r</tt> corresponds to <tt>c</tt>. For each <tt>Code</tt>
   *         <tt>co</tt> in <tt>c</tt>, for each <tt>Label</tt> <tt>l</tt> in
   *         <tt>co</tt>, there is an edge <tt>(p,l)</tt> where <tt>p</tt> is
   *         the vertex corresponding to <tt>co</tt>. Note: Each edge is unique.
   */
  public static
      Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>>
      codeToGraph(Code c) {
    DirectedMultigraph<Identity<Code.Tag>, Pair<Identity<Code.Tag>, Label>> g =
        new DirectedMultigraph<Identity<Code.Tag>, Pair<Identity<Code.Tag>, Label>>(
            (Class<Pair<Identity<Code.Tag>, Label>>) null);
    Identity<Tag> root = codeSpanningTreeToGraph(g, c);
    addLinksToCodeGraph(c, g, root, root);
    return pair(g, root);
  }

  private static void addLinksToCodeGraph(Code c,
      DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g,
      Identity<Tag> root, Identity<Tag> parent) {
    for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels.entrySet())) {
      Either<Code, List<Label>> cp = e.y;
      switch (cp.tag) {
      case Y:
        Identity<Tag> v = root;
        for (Label l : iter(cp.y()))
          v = g.getEdgeTarget(pair(v, l));
        g.addEdge(parent, v, pair(parent, e.x));
        break;
      case X:
        addLinksToCodeGraph(cp.x(), g, root, g.getEdgeTarget(pair(parent, e.x)));
        break;
      default:
        throw boom();
      }
    }
  }

  private static
      Identity<Code.Tag>
      codeSpanningTreeToGraph(
          DirectedMultigraph<Identity<Code.Tag>, Pair<Identity<Code.Tag>, Label>> g,
          Code c) {
    Identity<Code.Tag> v = new Identity<Code.Tag>(c.tag);
    g.addVertex(v);
    for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels.entrySet())) {
      Either<Code, List<Label>> cp = e.y;
      if (cp.tag != cp.tag.X)
        continue;
      Identity<Code.Tag> v2 = codeSpanningTreeToGraph(g, cp.x());
      g.addEdge(v, v2, pair(v, e.x));
    }
    return v;
  }

  public static Identity<Tag> followPath(List<Label> path,
      DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g,
      Identity<Tag> r) {
    for (Label l : iter(path))
      r = g.getEdgeTarget(pair(r, l));
    return r;
  }

  /**
   * For all Codes <tt>(c1,c2)</tt>, if <tt>c1</tt> has the same graph as
   * <tt>c2</tt>, then <tt>canonicalCode(c1).equals(canonicalCode(c2))</tt>
   */
  public static Code canonicalCode(Code c, List<Label> path) {
    return linkTreeToCode(LinkTreeUtils.canonicalLinkTree(linkTree(c), path,
        new LabelComparer()));
  }

  /** <code>c</code> has no links to links */
  public static boolean validCode(Code c) {
    return validCode(c, c);
  }

  private static boolean validCode(Code root, Code c) {
    for (Either<Code, List<Label>> cp : iter(values(c.labels))) {
      switch (cp.tag) {
      case Y:
        if (codeAt(cp.y(), root).isNothing())
          return false;
        break;
      case X:
        if (!validCode(root, cp.x()))
          return false;
        break;
      default:
        throw boom();
      }
    }
    return true;
  }

  public static boolean equal(Code c, Code c2) {
    // TODO get real equality check.
    // e.g, {'a <>} should be equal to {'a {'a <>}}
    return (canonicalCode(c, ListUtils.<Label> nil()).equals(canonicalCode(c2,
        ListUtils.<Label> nil())));
  }

  public static Optional<Code> getCode(Code root, Code c, Label l) {
    Optional<Either<Code, List<Label>>> cp = c.labels.get(l);
    if (cp.isNothing())
      return nothing();
    switch (cp.some().x.tag) {
    case X:
      return some(cp.some().x.x());
    case Y:
      return codeAt(cp.some().x.y(), root);
    default:
      throw boom();
    }
  }

  public static Optional<Code> followPath(List<Label> path, Code rootCode) {
    Code c = rootCode;
    while (!path.isEmpty()) {
      Optional<Code> oc = getCode(rootCode, c, path.cons().x);
      if (oc.isNothing())
        return nothing();
      c = oc.some().x;
      path = path.cons().tail;
    }
    return some(c);
  }

  /**
   * map cyclic path over the graph represented by this <tt>Code</tt> (rooted at
   * this <tt>Code</tt>) to a simple path from this <tt>Code</tt> to another
   * <tt>Code</tt>
   * 
   * @param Code
   *          a <tt>Code</tt> that <tt>validCode</tt> maps to <tt>true</tt>
   */
  public static List<Label> directPath(List<Label> path, Code code) {
    return directPath(path, code, code, ListUtils.<Label> nil());
  }

  private static List<Label> directPath(List<Label> p, Code root, Code c,
      List<Label> a) {
    if (p.isEmpty()) {
      Either<Code, List<Label>> cp = codeOrPathAt(a, root);
      switch (cp.tag) {
      case X:
        return a;
      case Y:
        return cp.y();
      default:
        throw boom();
      }
    }
    Either<Code, List<Label>> cp = c.labels.get(p.cons().x).some().x;
    switch (cp.tag) {
    case X:
      return directPath(p.cons().tail, root, cp.x(), append(p.cons().x, a));
    case Y:
      return directPath(p.cons().tail, root, codeAt(cp.y(), root).some().x,
          cp.y());
    default:
      throw boom();
    }
  }

  public static LinkTree<Label, Code.Tag> linkTree(final Code c) {
    return new LinkTree<Label, Code.Tag>() {
      public List<Pair<Label, Either<LinkTree<Label, Tag>, List<Label>>>>
          edges() {
        List<Pair<Label, Either<LinkTree<Label, Tag>, List<Label>>>> l = nil();
        for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels
            .entrySet()))
          switch (e.y.tag) {
          case X:
            l =
                cons(
                    pair(
                        e.x,
                        Either
                            .<LinkTree<Label, Code.Tag>, List<Label>> x(linkTree(e.y
                                .x()))), l);
            break;
          case Y:
            l =
                cons(
                    pair(e.x,
                        Either.<LinkTree<Label, Code.Tag>, List<Label>> y(e.y
                            .y())), l);
            break;
          default:
            throw boom();
          }
        return l;
      }

      public Tag vertex() {
        return c.tag;
      }

    };
  }

  public static Code linkTreeToCode(LinkTree<Label, Tag> lt) {
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    for (Pair<Label, Either<LinkTree<Label, Tag>, List<Label>>> e : iter(lt
        .edges()))
      m =
          m.put(e.x,
              e.y.tag == e.y.tag.Y ? Either.<Code, List<Label>> y(e.y.y())
                  : Either.<Code, List<Label>> x(linkTreeToCode(e.y.x())));
    switch (lt.vertex()) {
    case PRODUCT:
      return Code.newProduct(m);
    case UNION:
      return Code.newUnion(m);
    default:
      throw boom();
    }
  }

}