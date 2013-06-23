package com.example.kore.utils;

import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;
import static com.example.kore.utils.OptionalUtils.nothing;
import static com.example.kore.utils.OptionalUtils.some;
import static com.example.kore.utils.Pair.pair;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.Map.Entry;
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
import com.example.kore.ui.CodeEditorActivity;

public final class CodeUtils {

  public final static Code unit = Code
      .newProduct(new HashMap<Label, CodeOrPath>());

  public static String renderCode(Code c, List<Label> p,
      HashMap<CanonicalCode, HashMap<Label, String>> codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, int depth) {
    return renderCode(p, c, codeOrLabelAt(p, c), codeLabelAliases, codeAliases,
        depth);
  }

  private static String renderCode(List<Label> path, Code root, CodeOrPath cp,
      HashMap<CanonicalCode, HashMap<Label, String>> codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, int depth) {
    if (depth < 0)
      throw new RuntimeException("negative depth");
    if (depth == 0)
      return "...";
    if (cp.tag == CodeOrPath.Tag.PATH)
      return "^";
    CanonicalCode cc = new CanonicalCode(root, path);
    String codeAlias = codeAliases.get(cc);
    if (codeAlias != null)
      return codeAlias;
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
    HashMap<Label, String> labelAliases = codeLabelAliases.get(cc);
    String result = "";
    for (Label l : c.labels.keySet()) {
      if (result.equals(""))
        result = "'";
      else
        result += ", '";
      String la = labelAliases == null ? null : labelAliases.get(l);
      String ls = la == null ? l.label : la;
      result +=
          (ls + " " + renderCode(append(l, path), root, c.labels.get(l),
              codeLabelAliases, codeAliases, depth - 1));
    }
    return start + result + end;
  }

  /**
   * @return a pair <tt>(c', i)</tt> where c' is isomorphic to <tt>c</tt>. All
   *         labels of the nodes within the strongly connected components
   *         containing any node on <tt>path</tt> are randomized. <tt>i</tt> is
   *         the isomorphism itself.
   */
  public static Pair<Code, HashMap<List<Label>, HashMap<Label, Label>>>
      dissassociate(Code c, List<Label> path) {
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
    HashMap<List<Label>, HashMap<Label, Label>> i =
        new HashMap<List<Label>, HashMap<Label, Label>>();
    return pair(
        mapPaths(dissassociate_(p.x, p.y, c, vs, i, ListUtils.<Label> nil()),
            i, ListUtils.<Label> nil()), i);
  }

  private static Code mapPaths(Code c,
      HashMap<List<Label>, HashMap<Label, Label>> i, List<Label> path) {
    HashMap<Label, Label> m = i.get(path);
    HashMap<Label, CodeOrPath> m2 = new HashMap<Label, CodeOrPath>();
    for (Entry<Label, CodeOrPath> e : c.labels.entrySet())
      if (e.getValue().tag == CodeOrPath.Tag.PATH)
        m2.put(e.getKey(), CodeOrPath.newPath(mapPath(e.getValue().path, i)));
      else
        m2.put(
            e.getKey(),
            CodeOrPath.newCode(mapPaths(e.getValue().code, i,
                append(e.getKey(), path))));
    return new Code(c.tag, m2);
  }

  public static List<Label> mapPath(List<Label> path,
      HashMap<List<Label>, HashMap<Label, Label>> m) {
    List<Label> p = nil();
    List<Label> b = nil();
    for (Label l : iter(path)) {
      p = append(m.get(b).get(l), p);
      b = append(l, b);
    }
    return p;
  }

  private static Code dissassociate_(
      DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g,
      Identity<Tag> v, Code c, Set<Identity<Tag>> vs,
      HashMap<List<Label>, HashMap<Label, Label>> i, List<Label> path) {
    boolean randomize = vs.contains(v);
    HashMap<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>();
    HashMap<Label, Label> lm = new HashMap<Label, Label>();
    for (Entry<Label, CodeOrPath> e : c.labels.entrySet()) {
      Label l = null;
      if (randomize) {
        do {
          if (l != null)
            Log.e(CodeEditorActivity.class.getName(),
                "generated duplicate label");
          l = new Label(Random.randomId());
        } while (m.containsKey(l));
      } else {
        l = e.getKey();
      }
      lm.put(e.getKey(), l);
      m.put(
          l,
          e.getValue().tag == CodeOrPath.Tag.CODE ? CodeOrPath
              .newCode(dissassociate_(g, g.getEdgeTarget(pair(v, e.getKey())),
                  e.getValue().code, vs, i, append(e.getKey(), path))) : e
              .getValue());
    }
    i.put(path, lm);
    return new Code(c.tag, m);
  }

  public static Optional<Code> codeAt(List<Label> path, Code c) {
    for (Label l : iter(path)) {
      CodeOrPath cp = c.labels.get(l);
      if (cp == null)
        return nothing();
      if (cp.tag != CodeOrPath.Tag.CODE)
        return nothing();
      c = cp.code;
    }
    return some(c);
  }

  private static CodeOrPath codeOrLabelAt(List<Label> path, Code c) {
    CodeOrPath cp = CodeOrPath.newCode(c);
    for (Label l : iter(path)) {
      cp = cp.code.labels.get(l);
    }
    notNull(cp);
    return cp;
  }

  public static List<Label> longestValidSubPath(List<Label> path, Code c) {
    List<Label> p = nil();
    for (Label l : iter(path)) {
      CodeOrPath cp = c.labels.get(l);
      if (cp == null)
        return p;
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
    Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>(c.labels);
    Label l = p.cons().x;
    m.put(l, replaceCodeAt_(m.get(l).code, p.cons().tail, newCode));
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
    for (Entry<Label, CodeOrPath> e : c.labels.entrySet()) {
      CodeOrPath cp = e.getValue();
      switch (cp.tag) {
      case PATH:
        Identity<Tag> v = root;
        for (Label l : iter(cp.path))
          v = g.getEdgeTarget(pair(v, l));
        g.addEdge(parent, v, pair(parent, e.getKey()));
        break;
      case CODE:
        addLinksToCodeGraph(cp.code, g, root,
            g.getEdgeTarget(pair(parent, e.getKey())));
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
    for (Entry<Label, CodeOrPath> e : c.labels.entrySet()) {
      CodeOrPath cp = e.getValue();
      if (cp.tag != CodeOrPath.Tag.CODE)
        continue;
      Identity<Code.Tag> v2 = codeSpanningTreeToGraph(g, cp.code);
      g.addEdge(v, v2, pair(v, e.getKey()));
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
    HashMap<Identity<Tag>, List<Label>> m =
        new HashMap<Identity<Tag>, List<Label>>();
    Identity<Tag> r = followPath(path, g, root);
    buildCanonicalSpanningTreeOfCodeGraph(g, r, m, ListUtils.<Label> nil(),
        spanningTreeEdges);
    return buildCodeFromSpanningTree(g, m, r, spanningTreeEdges);
  }

  public static Code reRoot(Code c, List<Label> path) {
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        codeToGraph(c);
    DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g = p.x;
    Identity<Tag> root = p.y;
    Set<Pair<Identity<Tag>, Label>> spanningTreeEdges =
        new HashSet<Pair<Identity<Tag>, Label>>();
    HashMap<Identity<Tag>, List<Label>> m =
        new HashMap<Identity<Tag>, List<Label>>();
    Identity<Tag> r = followPath(path, g, root);
    buildSpanningTreeOfCodeGraph(g, r, m, ListUtils.<Label> nil(),
        spanningTreeEdges);
    return buildCodeFromSpanningTree(g, m, r, spanningTreeEdges);
  }

  private static Code buildCodeFromSpanningTree(
      DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g,
      HashMap<Identity<Tag>, List<Label>> m, Identity<Tag> v,
      Set<Pair<Identity<Tag>, Label>> spanningTreeEdges) {
    Map<Label, CodeOrPath> lm = new HashMap<Label, CodeOrPath>();
    for (Pair<Identity<Tag>, Label> e : g.outgoingEdgesOf(v)) {
      if (spanningTreeEdges.contains(e)) {
        lm.put(
            e.y,
            CodeOrPath.newCode(buildCodeFromSpanningTree(g, m,
                g.getEdgeTarget(e), spanningTreeEdges)));
      } else {
        lm.put(e.y, CodeOrPath.newPath(m.get(g.getEdgeTarget(e))));
      }
    }
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
      Identity<Tag> v, Map<Identity<Tag>, List<Label>> m, List<Label> path,
      Set<Pair<Identity<Code.Tag>, Label>> spanningTreeEdges) {
    m.put(v, path);
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
      if (!m.containsKey(v2)) {
        buildCanonicalSpanningTreeOfCodeGraph(g, v2, m, append(e.y, path),
            spanningTreeEdges);
        spanningTreeEdges.add(e);
      }
    }
  }

  private static void buildSpanningTreeOfCodeGraph(
      DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g,
      Identity<Tag> v, Map<Identity<Tag>, List<Label>> m, List<Label> path,
      Set<Pair<Identity<Code.Tag>, Label>> spanningTreeEdges) {
    m.put(v, path);
    for (Pair<Identity<Tag>, Label> e : g.outgoingEdgesOf(v)) {
      Identity<Tag> v2 = g.getEdgeTarget(e);
      if (!m.containsKey(v2)) {
        buildSpanningTreeOfCodeGraph(g, v2, m, append(e.y, path),
            spanningTreeEdges);
        spanningTreeEdges.add(e);
      }
    }
  }

  public static Code rebase(List<Label> p, Code c) {
    Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>();
    for (Entry<Label, CodeOrPath> e : c.labels.entrySet()) {
      Label l = e.getKey();
      CodeOrPath cp = e.getValue();
      switch (cp.tag) {
      case CODE:
        m.put(l, CodeOrPath.newCode(rebase(p, cp.code)));
        break;
      case PATH:
        m.put(l, CodeOrPath.newPath(append(p, cp.path)));
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
    for (CodeOrPath cp : c.labels.values()) {
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
