package com.example.kore.utils;

import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.MapUtils.containsKey;
import static com.example.kore.utils.MapUtils.values;
import static com.example.kore.utils.Null.notNull;
import static com.example.kore.utils.OptionalUtils.nothing;
import static com.example.kore.utils.OptionalUtils.some;
import static com.example.kore.utils.Pair.pair;

import java.util.Comparator;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.Set;
import java.util.TreeMap;

import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DirectedMultigraph;

import android.util.Log;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Code.Tag;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.ui.CodeLabelAliasMap;
import com.example.kore.utils.Map.Entry;

public final class CodeUtils {
  private static String className = CodeUtils.class.getName();

  public final static Code unit = Code.newProduct(Map
      .<Label, CodeOrPath> empty());

  public static String renderCode(Code c, List<Label> p,
      CodeLabelAliasMap codeLabelAliases, Map<CanonicalCode, String> codeAliases,
      int depth) {
    return renderCode(p, c, codeOrLabelAt(p, c), codeLabelAliases, codeAliases,
        depth);
  }

  private static String renderCode(List<Label> path, Code root, CodeOrPath cp,
      CodeLabelAliasMap codeLabelAliases, Map<CanonicalCode, String> codeAliases,
      int depth) {
    if (depth < 0)
      throw new RuntimeException("negative depth");
    if (depth == 0)
      return "...";
    CanonicalCode cc = new CanonicalCode(root, path);
    Optional<String> codeAlias = codeAliases.get(cc);
    if (!codeAlias.isNothing())
      return codeAlias.some().x;
    if (cp.tag == CodeOrPath.Tag.PATH)
      return "^";
    Code c = cp.code;
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
    for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet())) {
      Label l = e.k;
      if (result.equals(""))
        result = "'";
      else
        result += ", '";
      Optional<String> la = labelAliases.get(l);
      String ls = la.isNothing() ? l.label : la.some().x;
      result +=
          (ls + " " + renderCode(append(l, path), root, e.v, codeLabelAliases,
              codeAliases, depth - 1));
    }
    return start + result + end;
  }

  /**
   * @return a pair <tt>(c', i)</tt> where c' is isomorphic to <tt>c</tt>. All
   *         labels of the nodes within the strongly connected components
   *         containing any node on <tt>path</tt> are randomized. <tt>i</tt> is
   *         the isomorphism itself.
   */
  public static Pair<Code, Map<List<Label>, Map<Label, Label>>> dissassociate(
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
    Map<Label, CodeOrPath> m2 = Map.empty();
    for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet()))
      if (e.v.tag == CodeOrPath.Tag.PATH)
        m2 = m2.put(e.k, CodeOrPath.newPath(mapPath(e.v.path, i)));
      else
        m2 =
            m2.put(e.k,
                CodeOrPath.newCode(mapPaths(e.v.code, i, append(e.k, path))));
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
    Map<Label, CodeOrPath> m = Map.empty();
    Map<Label, Label> lm = Map.empty();
    for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet())) {
      Label l = null;
      if (randomize)
        do {
          if (l != null)
            Log.e(className, "generated duplicate label");
          l = new Label(Random.randomId());
        } while (containsKey(m, l));
      else
        l = e.k;
      lm = lm.put(e.k, l);
      m =
          m.put(
              l,
              e.v.tag == CodeOrPath.Tag.CODE ? CodeOrPath
                  .newCode(dissassociate_(g, g.getEdgeTarget(pair(v, e.k)),
                      e.v.code, vs, i, append(e.k, path))) : e.v);
    }
    i.set(i.get().put(path, lm));
    return new Code(c.tag, m);
  }

  public static Optional<Code> codeAt(List<Label> path, Code c) {
    for (Label l : iter(path)) {
      Optional<CodeOrPath> ocp = c.labels.get(l);
      if (ocp.isNothing())
        return nothing();
      CodeOrPath cp = ocp.some().x;
      if (cp.tag != CodeOrPath.Tag.CODE)
        return nothing();
      c = cp.code;
    }
    return some(c);
  }

  private static CodeOrPath codeOrLabelAt(List<Label> path, Code c) {
    CodeOrPath cp = CodeOrPath.newCode(c);
    for (Label l : iter(path)) {
      cp = cp.code.labels.get(l).some().x;
    }
    notNull(cp);
    return cp;
  }

  public static List<Label> longestValidSubPath(List<Label> path, Code c) {
    List<Label> p = nil();
    for (Label l : iter(path)) {
      Optional<CodeOrPath> ocp = c.labels.get(l);
      if (ocp.isNothing())
        return p;
      CodeOrPath cp = ocp.some().x;
      if (cp.tag != CodeOrPath.Tag.CODE)
        return p;
      c = cp.code;
      p = append(l, p);
    }
    return p;
  }

  public static Code replaceCodeAt(Code c, List<Label> p, CodeOrPath newCode) {
    return replaceCodeAt_(c, p, newCode).code;
  }

  private static CodeOrPath replaceCodeAt_(Code c, List<Label> p,
      CodeOrPath newCode) {
    if (p.isEmpty())
      return newCode;
    Label l = p.cons().x;
    Map<Label, CodeOrPath> m =
        c.labels.put(
            l,
            replaceCodeAt_(c.labels.get(l).some().x.code, p.cons().tail,
                newCode));
    return CodeOrPath.newCode(new Code(c.tag, m));
  }

  /**
   * @return a graph representing <tt>c</tt> where each edge is a pair
   *         <tt>(parent, label)</tt>, where parent is the <tt>parent</tt> of
   *         <tt>c</tt> in its spanning tree
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
    for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet())) {
      CodeOrPath cp = e.v;
      switch (cp.tag) {
      case PATH:
        Identity<Tag> v = root;
        for (Label l : iter(cp.path))
          v = g.getEdgeTarget(pair(v, l));
        g.addEdge(parent, v, pair(parent, e.k));
        break;
      case CODE:
        addLinksToCodeGraph(cp.code, g, root,
            g.getEdgeTarget(pair(parent, e.k)));
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
    for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet())) {
      CodeOrPath cp = e.v;
      if (cp.tag != CodeOrPath.Tag.CODE)
        continue;
      Identity<Code.Tag> v2 = codeSpanningTreeToGraph(g, cp.code);
      g.addEdge(v, v2, pair(v, e.k));
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
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        codeToGraph(c);
    DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g = p.x;
    Identity<Tag> root = p.y;
    Set<Pair<Identity<Tag>, Label>> spanningTreeEdges =
        new HashSet<Pair<Identity<Tag>, Label>>();
    Ref<Map<Identity<Tag>, List<Label>>> m =
        new Ref<Map<Identity<Tag>, List<Label>>>(
            Map.<Identity<Tag>, List<Label>> empty());
    Identity<Tag> r = followPath(path, g, root);
    buildCanonicalSpanningTreeOfCodeGraph(g, r, m, ListUtils.<Label> nil(),
        spanningTreeEdges);
    return buildCodeFromSpanningTree(g, m.get(), r, spanningTreeEdges);
  }

  public static Code reRoot(Code c, List<Label> path) {
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        codeToGraph(c);
    DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g = p.x;
    Identity<Tag> root = p.y;
    Set<Pair<Identity<Tag>, Label>> spanningTreeEdges =
        new HashSet<Pair<Identity<Tag>, Label>>();
    Ref<Map<Identity<Tag>, List<Label>>> m =
        new Ref<Map<Identity<Tag>, List<Label>>>(
            Map.<Identity<Tag>, List<Label>> empty());
    Identity<Tag> r = followPath(path, g, root);
    buildSpanningTreeOfCodeGraph(g, r, m, ListUtils.<Label> nil(),
        spanningTreeEdges);
    return buildCodeFromSpanningTree(g, m.get(), r, spanningTreeEdges);
  }

  private static Code buildCodeFromSpanningTree(
      DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g,
      Map<Identity<Tag>, List<Label>> m, Identity<Tag> v,
      Set<Pair<Identity<Tag>, Label>> spanningTreeEdges) {
    Map<Label, CodeOrPath> lm = Map.empty();
    for (Pair<Identity<Tag>, Label> e : g.outgoingEdgesOf(v))
      if (spanningTreeEdges.contains(e))
        lm =
            lm.put(
                e.y,
                CodeOrPath.newCode(buildCodeFromSpanningTree(g, m,
                    g.getEdgeTarget(e), spanningTreeEdges)));
      else
        lm =
            lm.put(e.y, CodeOrPath.newPath(m.get(g.getEdgeTarget(e)).some().x));
    switch (v.t) {
    case PRODUCT:
      return Code.newProduct(lm);
    case UNION:
      return Code.newUnion(lm);
    default:
      throw boom();
    }
  }

  private static void buildCanonicalSpanningTreeOfCodeGraph(
      DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g,
      Identity<Tag> v, Ref<Map<Identity<Tag>, List<Label>>> m,
      List<Label> path, Set<Pair<Identity<Code.Tag>, Label>> spanningTreeEdges) {
    m.set(m.get().put(v, path));
    SortedMap<Label, Pair<Identity<Tag>, Label>> sm =
        new TreeMap<Label, Pair<Identity<Tag>, Label>>(new Comparator<Label>() {
          @Override
          public int compare(Label a, Label b) {
            return a.label.compareTo(b.label);
          };
        });
    for (Pair<Identity<Tag>, Label> e : g.outgoingEdgesOf(v))
      sm.put(e.y, e);
    for (Pair<Identity<Tag>, Label> e : sm.values()) {
      Identity<Tag> v2 = g.getEdgeTarget(e);
      if (!containsKey(m.get(), v2)) {
        buildCanonicalSpanningTreeOfCodeGraph(g, v2, m, append(e.y, path),
            spanningTreeEdges);
        spanningTreeEdges.add(e);
      }
    }
  }

  private static void buildSpanningTreeOfCodeGraph(
      DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g,
      Identity<Tag> v, Ref<Map<Identity<Tag>, List<Label>>> m,
      List<Label> path, Set<Pair<Identity<Code.Tag>, Label>> spanningTreeEdges) {
    m.set(m.get().put(v, path));
    for (Pair<Identity<Tag>, Label> e : g.outgoingEdgesOf(v)) {
      Identity<Tag> v2 = g.getEdgeTarget(e);
      if (!containsKey(m.get(), v2)) {
        buildSpanningTreeOfCodeGraph(g, v2, m, append(e.y, path),
            spanningTreeEdges);
        spanningTreeEdges.add(e);
      }
    }
  }

  public static Code rebase(List<Label> p, Code c) {
    Map<Label, CodeOrPath> m = Map.empty();
    for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet())) {
      Label l = e.k;
      CodeOrPath cp = e.v;
      switch (cp.tag) {
      case CODE:
        m = m.put(l, CodeOrPath.newCode(rebase(p, cp.code)));
        break;
      case PATH:
        m = m.put(l, CodeOrPath.newPath(append(p, cp.path)));
        break;
      default:
        throw boom();
      }
    }
    switch (c.tag) {
    case PRODUCT:
      return Code.newProduct(m);
    case UNION:
      return Code.newUnion(m);
    default:
      throw boom();
    }
  }

  /** <code>c</code> has no links to links */
  public static boolean validCode(Code c) {
    return validCode(c, c);
  }

  private static boolean validCode(Code root, Code c) {
    for (CodeOrPath cp : iter(values(c.labels))) {
      switch (cp.tag) {
      case PATH:
        if (codeAt(cp.path, root).isNothing())
          return false;
        break;
      case CODE:
        if (!validCode(root, cp.code))
          return false;
        break;
      default:
        throw boom();
      }
    }
    return true;
  }
}
