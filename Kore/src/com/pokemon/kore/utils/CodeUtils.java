package com.pokemon.kore.utils;

import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.LinkTreeUtils.decompose3;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.cons;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.length;
import static com.pokemon.kore.utils.ListUtils.map;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.ListUtils.take;
import static com.pokemon.kore.utils.MapUtils.containsKey;
import static com.pokemon.kore.utils.MapUtils.fromList;
import static com.pokemon.kore.utils.MapUtils.values;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.OptionalUtils.some;
import static com.pokemon.kore.utils.PairUtils.pair;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DirectedMultigraph;

import android.util.Log;

import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Code.Tag;
import com.pokemon.kore.codes.Code2;
import com.pokemon.kore.codes.Code2.Link;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.З2Bytes;
import com.pokemon.kore.ui.Bijection;
import com.pokemon.kore.ui.CodeLabelAliasMap;
import com.pokemon.kore.ui.CodeLabelAliasMap2;
import com.pokemon.kore.ui.LinkTree;
import com.pokemon.kore.ui.StrictLinkTree;

public final class CodeUtils {
  private final static String className = CodeUtils.class.getName();

  public final static Code unit = Code.newProduct(Map.empty());
  public final static Code2 unit2 = Code2.newProduct(Map.empty());
  public final static ICode iunit = new ICode() {
    public Code2.Tag tag() {
      return Code2.Tag.PRODUCT;
    }

    public Pair<Code2, List<Label>> link() {
      return pair(unit2, nil());
    }

    public Map<Label, Either<ICode, List<Label>>> labels() {
      return Map.empty();
    }

    public ICode codeAt(List<Label> path) {
      if (path.isEmpty())
        return this;
      throw new RuntimeException("invalid path");
    }
  };

  public final static Code empty = Code.newUnion(Map.empty());

  public static CodeLabelAliasMap makeStaticCLAM(
      Map<CanonicalCode, Bijection<Label, String>> m) {
    return new CodeLabelAliasMap() {
      public void setAliases(CanonicalCode c, Bijection<Label, String> aliases) {
      }

      public boolean setAlias(CanonicalCode c, Label l, String alias) {
        return false;
      }

      public Bijection<Label, String> getAliases(CanonicalCode c) {
        Optional<Bijection<Label, String>> ob = m.get(c);
        return ob.isNothing() ? Bijection.empty() : ob.some().x;
      }

      public void deleteAlias(CanonicalCode c, Label l) {
      }
    };
  }

  public static Code reroot(Code c, List<Label> p) {
    return linkTreeToCode(LinkTreeUtils.reroot(linkTree(c), p));
  }

  public static byte[] labelBytes(Label l) {
    if (l.label.length() != 64)
      throw new RuntimeException("invalid string length");
    try {
      return l.label.getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static З2Bytes hash(Code2 c) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA256");
      hash(c, md);
      return new З2Bytes(md.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  public static void hash(Code2 c, MessageDigest md) {
    byte cs, ce;
    switch (c.tag) {
    case PRODUCT:
      cs = 0;
      ce = 1;
      break;
    case UNION:
      cs = 2;
      ce = 3;
      break;
    default:
      throw boom();
    }
    md.update(cs);
    for (Pair<Label, Either3<Code2, List<Label>, Link>> e : iter(c.labels
        .entrySet())) {
      md.update(labelBytes(e.x));
      switch (e.y.tag) {
      case X:
        hash(e.y.x(), md);
        break;
      case Y:
        md.update((byte) 4);
        for (Label l : iter(e.y.y()))
          md.update(labelBytes(l));
        md.update((byte) 5);
        break;
      case Z:
        md.update((byte) 6);
        md.update(e.y.z().hash.getBytes());
        break;
      default:
        break;
      }
    }
    md.update(ce);
  }

  public static String renderCode3(ICode c, List<Label> p,
      CodeLabelAliasMap2 codeLabelAliases, Bijection<Link, String> codeAliases,
      int depth) {
    Either<ICode, List<Label>> cp = codeOrPathAt2(p, c);
    return renderCode3(
        cp.tag == Either.Tag.X ? Either.x(cp.x()) : Either.y(pair(
            codeOrPathAt2(take(p, length(p) - 1), c).x(), cp.y())),
        codeLabelAliases, codeAliases, depth);
  }

  private static String renderCode3(Either<ICode, Pair<ICode, List<Label>>> cp,
      CodeLabelAliasMap2 codeLabelAliases, Bijection<Link, String> codeAliases,
      int depth) {
    if (depth < 0)
      throw new RuntimeException("negative depth");
    if (depth == 0)
      return "...";
    Link link;
    switch (cp.tag) {
    case X:
      Pair<Code2, List<Label>> p = cp.x().link();
      link = new Link(hash(p.x), p.y);
      break;
    case Y:
      ICode parent = cp.y().x;
      link = new Link(hash(parent.link().x), cp.y().y);
      break;
    default:
      throw boom();
    }
    Optional<String> codeAlias = codeAliases.xy.get(link);
    if (!codeAlias.isNothing())
      return codeAlias.some().x;
    if (cp.tag == cp.tag.Y)
      return "^";
    ICode c = cp.x();
    String start;
    String end;
    switch (c.tag()) {
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
    Bijection<Label, String> labelAliases = codeLabelAliases.getAliases(link);
    String result = "";
    for (Pair<Label, Either<ICode, List<Label>>> e : iter(c.labels().entrySet())) {
      Label l = e.x;
      if (result.equals(""))
        result = "'";
      else
        result += ", '";
      Optional<String> la = labelAliases.xy.get(l);
      String ls = la.isNothing() ? l.label : la.some().x;
      result +=
          (ls + " " + renderCode3(e.y.tag == Either.Tag.X ? Either.x(e.y.x())
              : Either.y(pair(c, e.y.y())), codeLabelAliases, codeAliases,
              depth - 1));
    }
    return start + result + end;
  }

  public static String renderCode(Code c, List<Label> p,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases, int depth) {
    return renderCode(p, c, codeOrPathAt(p, c), codeLabelAliases, codeAliases,
        depth);
  }

  private static String renderCode(List<Label> path, Code root,
      Either<Code, List<Label>> cp, CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases, int depth) {
    if (depth < 0)
      throw new RuntimeException("negative depth");
    if (depth == 0)
      return "...";
    CanonicalCode cc = new CanonicalCode(root, path);
    Optional<String> codeAlias = codeAliases.xy.get(cc);
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
    Bijection<Label, String> labelAliases = codeLabelAliases.getAliases(cc);
    String result = "";
    for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels.entrySet())) {
      Label l = e.x;
      if (result.equals(""))
        result = "'";
      else
        result += ", '";
      Optional<String> la = labelAliases.xy.get(l);
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
  public static Pair<Code2, Map<List<Label>, Map<Label, Label>>> disassociate2(
      ICode c, List<Label> path) {
    throw boom();
  }

  private static
      Pair<Code2, Map<List<Label>, Map<Label, Label>>>
      disassociate2(Code2 c, List<Label> path,
          Map<List<Label>, Map<Label, Label>> i, List<Label> before, Resolver r) {
    Map<Label, Either3<Code2, List<Label>, Link>> ls = Map.empty();
    Map<Label, Label> m = Map.empty();
    for (Pair<Label, Either3<Code2, List<Label>, Link>> e : iter(c.labels
        .entrySet())) {
      Label l = null;
      do {
        if (l != null)
          Log.e(className, "generated duplicate label");
        l = new Label(Random.randomId());
      } while (containsKey(ls, l));
      m = m.put(e.x, l);
      i = i.put(before, m);
      if (e.equals(path.cons().x)) {
        Pair<Code2, Map<List<Label>, Map<Label, Label>>> p;
        switch (e.y.tag) {
        case X:
          p =
              disassociate2(e.y.x(), path.cons().tail, i, append(e.x, before),
                  r);
          i = p.y;
          ls = ls.put(e.x, Either3.x(p.x));
          break;
        case Z:
          // fak. now we have to recurse in the middle of the SCC designated by
          // e.y.z(), but we need to also randomize that entire SCC
          // p =
          // disassociate2(
          // codeAt2(e.y.z().path, r.resolve(e.y.z().hash).some().x)
          // .some().x, path.cons().tail, i, before, r);
          // ls = ls.put(e.x, );
          break;
        case Y:
        default:
          throw boom();
        }
      } else {

      }
      switch (e.y.tag) {
      case X:
        if (path.isEmpty())
          ls = ls.put(e.x, e.y);
        else {
          Pair<Code2, Map<List<Label>, Map<Label, Label>>> p =
              disassociate2(c, path.cons().tail, i, append(e.x, before), r);
          i = p.y;
          ls = ls.put(e.x, Either3.x(p.x));
        }
        break;
      case Y:
      case Z:
        ls.put(l, e.y);
        break;
      }
    }
    switch (c.tag) {
    case PRODUCT:
      return pair(Code2.newProduct(ls), i);
    case UNION:
      return pair(Code2.newUnion(ls), i);
    default:
      throw boom();

    }
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
    Set<Identity<Tag>> vs = new HashSet<>();
    for (Set<Identity<Tag>> scc : new StrongConnectivityInspector<>(p.x)
        .stronglyConnectedSets()) {
      Identity<Tag> v = p.y;
      for (Label l : iter(path)) {
        if (scc.contains(v))
          vs.addAll(scc);
        v = p.x.getEdgeTarget(pair(v, l));
      }
      if (scc.contains(v))
        vs.addAll(scc);
    }
    Ref<Map<List<Label>, Map<Label, Label>>> i = new Ref<>(Map.empty());
    return pair(
        mapPaths(dissassociate_(p.x, p.y, c, vs, i, nil()), i.get(), nil()),
        i.get());
  }

  private static Code mapPaths(Code c, Map<List<Label>, Map<Label, Label>> i,
      List<Label> path) {
    Map<Label, Either<Code, List<Label>>> m2 = Map.empty();
    for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels.entrySet()))
      if (e.y.tag == e.y.tag.Y)
        m2 = m2.put(e.x, Either.y(mapPath(e.y.y(), i)));
      else
        m2 = m2.put(e.x, Either.x(mapPaths(e.y.x(), i, append(e.x, path))));
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
              e.y.tag == e.y.tag.X ? Either.x(dissassociate_(g,
                  g.getEdgeTarget(pair(v, e.x)), e.y.x(), vs, i,
                  append(e.x, path))) : e.y);
    }
    i.set(i.get().put(path, lm));
    return new Code(c.tag, m);
  }

  /**
   * Code at the end of the path <tt>p</tt> along the spanning tree (but also
   * through <code>Code2.Link</code>s) from <tt>c</tt>
   */
  public static Optional<Code2> codeAt2(List<Label> p, Code2 c, Resolver r) {
    if (p.isEmpty())
      return some(c);
    Optional<Either3<Code2, List<Label>, Link>> ocp = c.labels.get(p.cons().x);
    if (ocp.isNothing())
      return nothing();
    switch (ocp.some().x.tag) {
    case X:
      return codeAt2(p.cons().tail, ocp.some().x.x(), r);
    case Y:
      throw new RuntimeException();
    case Z:
      return codeAt2(p.cons().tail, resolve(ocp.some().x.z(), r).some().x, r);
    default:
      throw boom();
    }
  }

  /**
   * Code at the end of the path <tt>p</tt> along the spanning tree from
   * <tt>c</tt>
   */
  public static Either<Code2, CodeAtErr> codeAt2(List<Label> p, Code2 c) {
    if (p.isEmpty())
      return Either.x(c);
    Optional<Either3<Code2, List<Label>, Link>> ocp = c.labels.get(p.cons().x);
    if (ocp.isNothing())
      return Either.y(CodeAtErr.InvalidPath);
    switch (ocp.some().x.tag) {
    case X:
      return codeAt2(p.cons().tail, ocp.some().x.x());
    case Y:
      return Either.y(CodeAtErr.HitSelfReference);
    case Z:
      return Either.y(CodeAtErr.HitLink);
    default:
      throw boom();
    }
  }

  /**
   * Code at the end of the path <tt>p</tt> along the spanning tree from
   * <tt>c</tt>
   */
  public static Optional<ICode> codeAt2(List<Label> p, ICode c) {
    if (p.isEmpty())
      return some(c);
    Optional<Either<ICode, List<Label>>> ocp = c.labels().get(p.cons().x);
    if (ocp.isNothing())
      return nothing();
    switch (ocp.some().x.tag) {
    case X:
      return codeAt2(p.cons().tail, ocp.some().x.x());
    case Y:
      throw new RuntimeException();
    default:
      throw boom();
    }
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
    Either<Code, List<Label>> cp = Either.x(c);
    for (Label l : iter(path))
      cp = cp.x().labels.get(l).some().x;
    return cp;
  }

  public static Either<ICode, List<Label>> codeOrPathAt2(List<Label> path,
      ICode c) {
    Either<ICode, List<Label>> cp = Either.x(c);
    for (Label l : iter(path))
      cp = cp.x().labels().get(l).some().x;
    return cp;
  }

  public static List<Label> longestValidSubPath2(List<Label> path, ICode c) {
    List<Label> p = nil();
    for (Label l : iter(path)) {
      Optional<Either<ICode, List<Label>>> ocp = c.labels().get(l);
      if (ocp.isNothing())
        return p;
      Either<ICode, List<Label>> cp = ocp.some().x;
      if (cp.tag != cp.tag.X)
        return p;
      c = cp.x();
      p = append(l, p);
    }
    return p;
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

  public static Code replaceCodeAt3(Code c, List<Label> p,
      Either<Code, List<Label>> newCode) {
    return replaceCodeAt3_(Either.x(c), p, newCode).x();
  }

  private static Either<Code, List<Label>> replaceCodeAt3_(
      Either<Code, List<Label>> c, List<Label> p,
      Either<Code, List<Label>> newCode) {
    if (p.isEmpty())
      return newCode;
    Label l = p.cons().x;
    Map<Label, Either<Code, List<Label>>> m =
        c.x().labels.put(
            l,
            replaceCodeAt3_(c.x().labels.get(l).some().x, p.cons().tail,
                newCode));
    return Either.x(new Code(c.x().tag, m));
  }

  public interface Resolver {
    public Optional<Code2> resolve(З2Bytes hash);
  }

  public static Pair<Map<З2Bytes, Code2>, Code2> replaceCodeAt2(Code2 c,
      List<Label> path, Either3<Code2, List<Label>, Link> newCode, Resolver r) {
    Pair<Map<З2Bytes, StrictLinkTree<Label, Either<Link, Code2.Tag>>>, StrictLinkTree<Label, Either<Link, Code2.Tag>>> p =
        decompose3(
            linkTree(inlineAndReplace(
                newCode.tag == Either3.Tag.Y ? inline(c, newCode.y(), r) : c,
                path, newCode, r)), lt -> hash(strictLinkTreeToCode(lt)),
            h -> new StrictLinkTree<Label, Either<Link, Code2.Tag>>(nil(),
                Either.x(new Link(h, nil()))));
    Code2 c2 = strictLinkTreeToCode(p.y);
    return pair(
        fromList(map($p -> pair($p.x, strictLinkTreeToCode($p.y)),
            p.x.put(hash(c2), p.y).entrySet())), c2);
  }

  private static Code2 inline(Code2 c, List<Label> p, Resolver r) {
    return inline(c, nil(), nil(), p, r);
  }

  private static Code2 inline(Code2 c, List<Label> beforeRoot,
      List<Label> before, List<Label> after, Resolver r) {
    if (after.isEmpty())
      return c;
    Either3<Code2, List<Label>, Link> cpl =
        c.labels.get(after.cons().x).some().x;
    Code2 c2;
    switch (cpl.tag) {
    case X:
      c2 =
          inline(cpl.x(), beforeRoot, append(after.cons().x, before),
              after.cons().tail, r);
      break;
    case Y:
      return c;
    case Z: {
      List<Label> before2 = append(after.cons().x, before);
      c2 =
          inline(rebase(beforeRoot, resolve(cpl.z(), r).some().x), before2,
              before2, after.cons().tail, r);
      break;
    }
    default:
      throw boom();
    }
    return new Code2(c.tag, c.labels.put(after.cons().x, Either3.x(c2)));
  }

  private static Code2 rebase(List<Label> p, Code2 c) {
    Map<Label, Either3<Code2, List<Label>, Link>> es = Map.empty();
    for (Pair<Label, Either3<Code2, List<Label>, Link>> e : iter(c.labels
        .entrySet()))
      switch (e.y.tag) {
      case X:
        es = es.put(e.x, Either3.x(rebase(p, e.y.x())));
        break;
      case Y:
        es = es.put(e.x, Either3.y(append(p, e.y.y())));
        break;
      case Z:
        es = es.put(e.x, e.y);
        break;
      }
    return new Code2(c.tag, es);
  }

  public static Code2 inlineAndReplace(Code2 c, List<Label> p,
      Either3<Code2, List<Label>, Link> newCode, Resolver r) {
    return inlineAndReplace(Either.x(c), nil(), nil(), p, newCode, r).x();
  }

  private static Either3<Code2, List<Label>, Link> inlineAndReplace(
      Either<Code2, List<Label>> c, List<Label> beforeRoot, List<Label> before,
      List<Label> after, Either3<Code2, List<Label>, Link> newCode, Resolver r) {
    if (after.isEmpty())
      return newCode;
    Either3<Code2, List<Label>, Link> cpl =
        c.x().labels.get(after.cons().x).some().x;
    Either<Code2, List<Label>> cp;
    List<Label> b2 = append(after.cons().x, before);
    List<Label> br2;
    switch (cpl.tag) {
    case X:
      cp = Either.x(cpl.x());
      br2 = beforeRoot;
      break;
    case Y:
      cp = Either.y(cpl.y());
      br2 = beforeRoot;
      break;
    case Z:
      cp = Either.x(rebase(beforeRoot, resolve(cpl.z(), r).some().x));
      br2 = b2;
      break;
    default:
      throw boom();
    }
    return Either3.x(new Code2(c.x().tag, c.x().labels.put(after.cons().x,
        inlineAndReplace(cp, br2, b2, after.cons().tail, newCode, r))));
  }

  public static Code replaceCodeAt(Code c, List<Label> p,
      Either<Code, List<Label>> newCode) {
    return replaceCodeAt_(Either.x(c), p, newCode).x();
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
    return Either.x(new Code(c.x().tag, m));
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
        new DirectedMultigraph<>((Class<Pair<Identity<Code.Tag>, Label>>) null);
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
    Identity<Code.Tag> v = new Identity<>(c.tag);
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
  public static boolean validCode2(Code2 c) {
    return validCode2(c, c);
  }

  private static boolean validCode2(Code2 root, Code2 c) {
    for (Either3<Code2, List<Label>, Link> cp : iter(values(c.labels))) {
      switch (cp.tag) {
      case Y:
        if (codeAt2(cp.y(), root).tag == Either.Tag.Y)
          return false;
        break;
      case X:
        if (!validCode2(root, cp.x()))
          return false;
        break;
      case Z:
        break;
      default:
        throw boom();
      }
    }
    return true;
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
    return (canonicalCode(c, nil()).equals(canonicalCode(c2, nil())));
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

  public static ICode followPath(List<Label> path, ICode c) {
    return path.isEmpty() ? c : followPath(path.cons().tail,
        child(c, path.cons().x));
  }

  public static ICode child(ICode c, Label l) {
    Either<ICode, List<Label>> cp = c.labels().get(l).some().x;
    switch (cp.tag) {
    case X:
      return cp.x();
    case Y:
      return c.codeAt(cp.y());
    default:
      throw boom();
    }
  }

  /**
   * map cyclic path over the graph represented by this <tt>Code</tt> (rooted at
   * this <tt>Code</tt>) to a simple path from this <tt>Code</tt> to another
   * <tt>Code</tt>
   *
   * @param code
   *          a <tt>Code</tt> that <tt>validCode</tt> maps to <tt>true</tt>
   */
  public static List<Label> directPath(List<Label> path, Code code) {
    return directPath(path, code, code, nil());
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

  public static LinkTree<Label, Either<Link, Code2.Tag>> linkTree(Code2 c) {
    return new LinkTree<Label, Either<Link, Code2.Tag>>() {
      public
          List<Pair<Label, Either<LinkTree<Label, Either<Link, Code2.Tag>>, List<Label>>>>
          edges() {
        List<Pair<Label, Either<LinkTree<Label, Either<Link, Code2.Tag>>, List<Label>>>> l =
            nil();
        for (Pair<Label, Either3<Code2, List<Label>, Link>> e : iter(c.labels
            .entrySet())) {
          Either<LinkTree<Label, Either<Link, Code2.Tag>>, List<Label>> t;
          switch (e.y.tag) {
          case X:
            t = Either.x(linkTree(e.y.x()));
            break;
          case Y:
            t = Either.y(e.y.y());
            break;
          case Z:
            t = Either.x(new LinkTree<Label, Either<Link, Code2.Tag>>() {
              public
                  List<Pair<Label, Either<LinkTree<Label, Either<Link, Code2.Tag>>, List<Label>>>>
                  edges() {
                return nil();
              }

              public Either<Link, Code2.Tag> vertex() {
                return Either.x(e.y.z());
              }
            });
            break;
          default:
            throw boom();
          }
          l = cons(pair(e.x, t), l);
        }
        return l;
      }

      public Either<Link, Code2.Tag> vertex() {
        return Either.y(c.tag);
      }

    };
  }

  public static LinkTree<Label, Code.Tag> linkTree(Code c) {
    return new LinkTree<Label, Code.Tag>() {
      public List<Pair<Label, Either<LinkTree<Label, Tag>, List<Label>>>>
          edges() {
        List<Pair<Label, Either<LinkTree<Label, Tag>, List<Label>>>> l = nil();
        for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels
            .entrySet()))
          switch (e.y.tag) {
          case X:
            l = cons(pair(e.x, Either.x(linkTree(e.y.x()))), l);
            break;
          case Y:
            l = cons(pair(e.x, Either.y(e.y.y())), l);
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
          m.put(
              e.x,
              e.y.tag == e.y.tag.Y ? Either.y(e.y.y()) : Either
                  .x(linkTreeToCode(e.y.x())));
    switch (lt.vertex()) {
    case PRODUCT:
      return Code.newProduct(m);
    case UNION:
      return Code.newUnion(m);
    default:
      throw boom();
    }
  }

  public static Code2 strictLinkTreeToCode(
      StrictLinkTree<Label, Either<Link, Code2.Tag>> lt) {
    Map<Label, Either3<Code2, List<Label>, Link>> m = Map.empty();
    for (Pair<Label, Either<StrictLinkTree<Label, Either<Link, Code2.Tag>>, List<Label>>> e : iter(lt.edges)) {
      Either3<Code2, List<Label>, Link> v;
      switch (e.y.tag) {
      case X:
        switch (e.y.x().vertex.tag) {
        case X:
          v = Either3.z(e.y.x().vertex.x());
          break;
        case Y:
          v = Either3.x(strictLinkTreeToCode(e.y.x()));
          break;
        default:
          throw boom();
        }
        break;
      case Y:
        v = Either3.y(e.y.y());
        break;
      default:
        throw boom();
      }
      m = m.put(e.x, v);
    }
    switch (lt.vertex.y()) {
    case PRODUCT:
      return Code2.newProduct(m);
    case UNION:
      return Code2.newUnion(m);
    default:
      throw boom();
    }
  }

  public static Code2 linkTreeToCode3(
      LinkTree<Label, Either<Link, Code2.Tag>> lt) {
    Map<Label, Either3<Code2, List<Label>, Link>> m = Map.empty();
    for (Pair<Label, Either<LinkTree<Label, Either<Link, Code2.Tag>>, List<Label>>> e : iter(lt
        .edges())) {
      Either3<Code2, List<Label>, Link> v;
      switch (e.y.tag) {
      case X:
        switch (e.y.x().vertex().tag) {
        case X:
          v = Either3.z(e.y.x().vertex().x());
          break;
        case Y:
          v = Either3.x(linkTreeToCode3(e.y.x()));
          break;
        default:
          throw boom();
        }
      case Y:
        v = Either3.y(e.y.y());
        break;
      default:
        throw boom();
      }
      m = m.put(e.x, v);
    }
    switch (lt.vertex().y()) {
    case PRODUCT:
      return Code2.newProduct(m);
    case UNION:
      return Code2.newUnion(m);
    default:
      throw boom();
    }
  }

  public static boolean canReplace(Code2 c, List<Label> p,
      Either3<Code2, List<Label>, Link> n, Resolver r) {
    return validCode2(inlineAndReplace(c, p, n, r));
  }

  public static Optional<Code2> resolve(Link l, Resolver r) {
    Optional<Code2> oc = r.resolve(l.hash);
    return oc.isNothing() ? nothing() : some(codeAt2(l.path, oc.some().x).x());
  }

  public static ICode icode(Link l, Resolver r) {
    return icode(resolve(l, r).some().x, r);
  }

  public static ICode icode(Code2 c, Resolver r) {
    return icode(c, nil(), c, r);
  }

  private static ICode icode(Code2 c, List<Label> p, Code2 root, Resolver r) {
    return new ICode() {
      public Code2.Tag tag() {
        return c.tag;
      }

      public Pair<Code2, List<Label>> link() {
        return new Pair<Code2, List<Label>>(root, p);
      }

      public Map<Label, Either<ICode, List<Label>>> labels() {
        Map<Label, Either<ICode, List<Label>>> m = Map.empty();
        for (Pair<Label, Either3<Code2, List<Label>, Link>> e : iter(c.labels
            .entrySet())) {
          switch (e.y.tag) {
          case X:
            m = m.put(e.x, Either.x(icode(e.y.x(), append(e.x, p), root, r)));
            break;
          case Y:
            m = m.put(e.x, Either.y(e.y.y()));
            break;
          case Z:
            Code2 c2 = r.resolve(e.y.z().hash).some().x;
            m =
                m.put(e.x, Either.x(icode(codeAt2(e.y.z().path, c2).x(),
                    e.y.z().path, c2, r)));
            break;
          default:
            break;
          }
        }
        return m;
      }

      public ICode codeAt(List<Label> path) {
        return icode(CodeUtils.codeAt2(path, root).x(), r);
      }
    };
  }

  public static Code code(Code2 c, Resolver r) {
    return code(c, nil(), nil(), r);
  }

  private static Code code(Code2 c, List<Label> beforeRoot, List<Label> before,
      Resolver r) {
    Map<Label, Either<Code, List<Label>>> ls = Map.empty();
    for (Pair<Label, Either3<Code2, List<Label>, Link>> e : iter(c.labels
        .entrySet())) {
      Either<Code, List<Label>> t;
      switch (e.y.tag) {
      case X:
        t = Either.x(code(e.y.x(), beforeRoot, append(e.x, before), r));
        break;
      case Y:
        t = Either.y(append(beforeRoot, e.y.y()));
        break;
      case Z:
        List<Label> before2 = append(e.x, before);
        t = Either.x(code(resolve(e.y.z(), r).some().x, before2, before2, r));
        break;
      default:
        throw boom();
      }
      ls = ls.put(e.x, t);
    }
    switch (c.tag) {
    case PRODUCT:
      return Code.newProduct(ls);
    case UNION:
      return Code.newUnion(ls);
    default:
      throw boom();

    }
  }

  public static Code2 code2(Code c) {
    Map<Label, Either3<Code2, List<Label>, Link>> ls = Map.empty();
    for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels.entrySet())) {
      switch (e.y.tag) {
      case X:
        ls = ls.put(e.x, Either3.x(code2(e.y.x())));
        break;
      case Y:
        ls = ls.put(e.x, Either3.y(e.y.y()));
      }
    }
    switch (c.tag) {
    case PRODUCT:
      return Code2.newProduct(ls);
    case UNION:
      return Code2.newUnion(ls);
    default:
      throw boom();
    }
  }

  public static List<Label> rootPath(List<Label> path, Code2 c, Resolver r) {
    return rootPath(nil(), nil(), path, c, r);
  }

  private static List<Label> rootPath(List<Label> rootPath, List<Label> before,
      List<Label> path, Code2 c, Resolver r) {
    if (path.isEmpty())
      return rootPath;
    Either3<Code2, List<Label>, Link> cpl =
        c.labels.get(path.cons().x).some().x;
    switch (cpl.tag) {
    case X:
      return rootPath(rootPath, append(path.cons().x, before),
          path.cons().tail, cpl.x(), r);
    case Y:
      throw new RuntimeException("path points to a self-reference");
    case Z:
      Code2 c2 = resolve(cpl.z(), r).some().x;
      before = append(path.cons().x, before);
      return rootPath(before, before, path.cons().tail, c2, r);
    default:
      throw boom();
    }
  }

  public static void foreach(Code2 c,
      F<Pair<List<Label>, Either3<Code2, List<Label>, Link>>, Unit> f) {
    foreach(Either3.x(c), nil(), f);
  }

  public static void foreach(Either3<Code2, List<Label>, Link> cpl,
      List<Label> p,
      F<Pair<List<Label>, Either3<Code2, List<Label>, Link>>, Unit> f) {
    f.f(pair(p, cpl));
    if (cpl.tag == Either3.Tag.X)
      for (Pair<Label, Either3<Code2, List<Label>, Link>> e : iter(cpl.x().labels
          .entrySet()))
        foreach(e.y, append(e.x, p), f);

  }

  public static Link hashLink(Pair<Code2, List<Label>> l) {
    return new Link(hash(l.x), l.y);
  }

  public static boolean equal(ICode a, ICode b) {
    return a.link().equals(b.link());
  }
}