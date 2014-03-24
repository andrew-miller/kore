package com.example.kore.ui;

import static com.example.kore.ui.PatternUtils.emptyPattern;
import static com.example.kore.ui.PatternUtils.renderPattern;
import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.CodeUtils.codeToGraph;
import static com.example.kore.utils.CodeUtils.directPath;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.fromArray;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.length;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.ListUtils.nth;
import static com.example.kore.utils.ListUtils.replace;
import static com.example.kore.utils.OptionalUtils.nothing;
import static com.example.kore.utils.OptionalUtils.some;
import static com.example.kore.utils.Unit.unit;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.DirectedMultigraph;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Abstraction;
import com.example.kore.codes.Relation.Composition;
import com.example.kore.codes.Relation.Label_;
import com.example.kore.codes.Relation.Product;
import com.example.kore.codes.Relation.Projection;
import com.example.kore.codes.Relation.Union;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.Either3.Tag;
import com.example.kore.utils.Identity;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.Optional;
import com.example.kore.utils.OptionalUtils;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class RelationUtils {
  public static final Relation unit_unit =
      Relation
          .abstraction(new Abstraction(
              emptyPattern,
              x(Relation.product(new Product(
                  Map.<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> empty(),
                  unit))), unit, unit));

  /**
   * @param argCode
   *          required if there <tt>rx</tt> is a projection or <tt>rx</tt>
   *          contains a projection without an abstraction on the path from it
   *          to that projection
   */
  public static String renderRelation(Optional<Code> argCode,
      Either<Relation, List<Either3<Label, Integer, Unit>>> rx,
      CodeLabelAliasMap codeLabelAliases) {
    if (rx.isY())
      return "^";
    Relation r = rx.x();
    switch (r.tag) {
    case ABSTRACTION:
      return "("
          + renderPattern(r.abstraction().pattern, r.abstraction().i,
              ListUtils.<Label> nil(), codeLabelAliases)
          + " -> "
          + renderRelation(some(r.abstraction().i), r.abstraction().r,
              codeLabelAliases) + ")";
    case COMPOSITION: {
      List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l =
          r.composition().l;
      if (l.isEmpty())
        return "<>";
      String s = "<" + renderRelation(argCode, l.cons().x, codeLabelAliases);
      if (l.cons().tail.isEmpty())
        return s + ">";
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> x : iter(l
          .cons().tail))
        s += "|" + renderRelation(argCode, x, codeLabelAliases);
      return s + ">";
    }
    case LABEL: {
      Optional<String> alias =
          codeLabelAliases.getAliases(
              new CanonicalCode(r.label().o, ListUtils.<Label> nil())).get(
              r.label().label);
      return "'" + (alias.isNothing() ? r.label().label : alias.some().x) + " "
          + renderRelation(argCode, r.label().r, codeLabelAliases);
    }
    case PRODUCT: {
      Map<Label, String> las =
          codeLabelAliases.getAliases(new CanonicalCode(r.product().o,
              ListUtils.<Label> nil()));
      List<Entry<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>>> es =
          r.product().m.entrySet();
      if (es.isEmpty())
        return "{}";
      Optional<String> la = las.get(es.cons().x.k);
      String s =
          "{'" + (la.isNothing() ? es.cons().x.k : la.some().x) + " "
              + renderRelation(argCode, es.cons().x.v, codeLabelAliases);
      if (es.cons().tail.isEmpty())
        return s + "}";
      for (Entry<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(es
          .cons().tail)) {
        la = las.get(e.k);
        s +=
            "," + (la.isNothing() ? e.k : la.some().x) + " "
                + renderRelation(argCode, e.v, codeLabelAliases);
      }
      return s + "}";
    }
    case PROJECTION: {
      String s = "$";
      List<Label> p = nil();
      for (Label l : iter(r.projection().path)) {
        Optional<String> a =
            codeLabelAliases.getAliases(
                new CanonicalCode(argCode.some().x, directPath(p,
                    argCode.some().x))).get(l);
        s += "." + (a.isNothing() ? l.label : a.some().x);
        p = append(l, p);
      }
      return s;
    }
    case UNION:
      List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l =
          r.union().l;
      if (l.isEmpty())
        return "[]";
      String s = "[" + renderRelation(argCode, l.cons().x, codeLabelAliases);
      if (l.cons().tail.isEmpty())
        return s + "]";
      int i = 1;
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> x : iter(l
          .cons().tail))
        s += "," + renderRelation(argCode, x, codeLabelAliases);
      return s + "]";
    default:
      throw boom();
    }
  }

  /** Relation at the end of the edge <tt>e</tt> in the tree */
  public static Optional<Relation> subRelation(Relation r,
      Either3<Label, Integer, Unit> e) {
    Optional<Either<Relation, List<Either3<Label, Integer, Unit>>>> oe =
        subRelationOrPath(r, e);
    if (oe.isNothing())
      return nothing();
    if (oe.some().x.isY())
      return nothing();
    return some(oe.some().x.x());
  }

  /** Relation at the end of the simple path <tt>p</tt> from <tt>r</tt> */
  public static Optional<Relation> relationAt(
      List<Either3<Label, Integer, Unit>> p, Relation r) {
    for (Either3<Label, Integer, Unit> e : iter(p)) {
      Optional<Relation> or = subRelation(r, e);
      if (or.isNothing())
        return nothing();
      r = or.some().x;
    }
    return some(r);
  }

  public static Either<Relation, List<Either3<Label, Integer, Unit>>>
      relationOrPathAt(List<Either3<Label, Integer, Unit>> p, Relation r) {
    Either<Relation, List<Either3<Label, Integer, Unit>>> rp = Either.x(r);
    for (Either3<Label, Integer, Unit> e : iter(p))
      rp = subRelationOrPath(rp.x(), e).some().x;
    return rp;
  }

  public static Optional<Either<Relation, List<Either3<Label, Integer, Unit>>>>
      subRelationOrPath(Relation r, Either3<Label, Integer, Unit> e) {
    switch (r.tag) {
    case ABSTRACTION:
      if (e.tag != Tag.Z)
        return nothing();
      return f(some(r.abstraction().r));
    case COMPOSITION:
      if (e.tag != Tag.Y)
        return nothing();
      return f(nth(r.composition().l, e.y()));
    case PRODUCT:
      if (e.tag != Tag.X)
        return nothing();
      return f(r.product().m.get(e.x()));
    case PROJECTION:
      return nothing();
    case LABEL:
      if (e.tag != Tag.Z)
        return nothing();
      return f(some(r.label().r));
    case UNION:
      if (e.tag != Tag.Y)
        return nothing();
      return f(nth(r.union().l, e.y()));
    default:
      throw boom();
    }
  }

  private static
      Optional<Either<Relation, List<Either3<Label, Integer, Unit>>>> f(
          Optional<Either<Relation, List<Either3<Label, Integer, Unit>>>> r) {
    if (r.isNothing())
      return nothing();
    return some(r.some().x);
  }

  public static Relation replaceRelationOrPathAt(Relation r,
      List<Either3<Label, Integer, Unit>> p,
      Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
    return replaceRelationOrPathAt(x(r), p, er).x();
  }

  private static Either<Relation, List<Either3<Label, Integer, Unit>>>
      replaceRelationOrPathAt(
          Either<Relation, List<Either3<Label, Integer, Unit>>> er,
          List<Either3<Label, Integer, Unit>> p,
          Either<Relation, List<Either3<Label, Integer, Unit>>> er2) {
    if (p.isEmpty())
      return er2;
    Relation r = er.x();
    Either3<Label, Integer, Unit> e = p.cons().x;
    List<Either3<Label, Integer, Unit>> p2 = p.cons().tail;
    switch (r.tag) {
    case ABSTRACTION:
      if (e.tag != Tag.Z)
        throw new RuntimeException("invalid path");
      Abstraction a = r.abstraction();
      return x(Relation.abstraction(new Relation.Abstraction(a.pattern,
          replaceRelationOrPathAt(a.r, p2, er2), a.i, a.o)));
    case COMPOSITION:
      if (e.tag != Tag.Y)
        throw new RuntimeException("invalid path");
      Composition c = r.composition();
      return x(Relation
          .composition(new Relation.Composition(
              replace(r.composition().l, e.y(),
                  replaceRelationOrPathAt(nth(c.l, e.y()).some().x, p2, er2)),
              c.i, c.o)));
    case PRODUCT:
      if (e.tag != Tag.X)
        throw new RuntimeException("invalid path");
      Product prod = r.product();
      return x(Relation
          .product(new Relation.Product(prod.m.put(e.x(),
              replaceRelationOrPathAt(prod.m.get(e.x()).some().x, p2, er2)),
              prod.o)));
    case PROJECTION:
      throw new RuntimeException("path goes through projection");
    case LABEL:
      if (e.tag != Tag.Z)
        throw new RuntimeException("invalid path");
      return x(Relation.label(new Label_(r.label().label,
          replaceRelationOrPathAt(r.label().r, p2, er2), r.label().o)));
    case UNION:
      if (e.tag != Tag.Y)
        throw new RuntimeException("invalid path");
      Union u = r.union();
      return x(Relation
          .union(new Relation.Union(replace(u.l, e.y(),
              replaceRelationOrPathAt(nth(u.l, e.y()).some().x, p2, er2)), u.i,
              u.o)));
    default:
      throw boom();
    }
  }

  public static Code domain(Relation r) {
    switch (r.tag) {
    case COMPOSITION:
      return r.composition().i;
    case ABSTRACTION:
      return r.abstraction().i;
    case LABEL:
      return unit;
    case PRODUCT:
      return unit;
    case PROJECTION:
      return unit;
    case UNION:
      return r.union().i;
    default:
      throw boom();
    }
  }

  public static Code codomain(Relation r) {
    switch (r.tag) {
    case COMPOSITION:
      return r.composition().o;
    case ABSTRACTION:
      return r.abstraction().o;
    case LABEL:
      return r.label().o;
    case PRODUCT:
      return r.product().o;
    case PROJECTION:
      return r.projection().o;
    case UNION:
      return r.union().o;
    default:
      throw boom();
    }
  }

  public static boolean inAbstraction(List<Either3<Label, Integer, Unit>> path,
      Relation relation) {
    if (path.isEmpty())
      return false;
    if (relation.tag == Relation.Tag.ABSTRACTION)
      return true;
    return inAbstraction(path.cons().tail, subRelation(relation, path.cons().x)
        .some().x);
  }

  public static Optional<Abstraction> enclosingAbstraction(
      List<Either3<Label, Integer, Unit>> path, Relation relation) {
    return enclosingAbstraction(path, relation,
        OptionalUtils.<Abstraction> nothing());
  }

  private static Optional<Abstraction> enclosingAbstraction(
      List<Either3<Label, Integer, Unit>> path, Relation relation,
      Optional<Abstraction> a) {
    if (path.isEmpty())
      return a;
    return enclosingAbstraction(path.cons().tail,
        subRelation(relation, path.cons().x).some().x,
        relation.tag == Relation.Tag.ABSTRACTION ? some(relation.abstraction())
            : a);
  }

  public static Relation removeFromComposition(Set<Integer> is, Relation root,
      List<Either3<Label, Integer, Unit>> path) {
    Relation r = relationAt(path, root).some().x;
    Composition c = r.composition();
    List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l = nil();
    Integer i = 0;
    for (Either<Relation, List<Either3<Label, Integer, Unit>>> er : iter(c.l)) {
      if (!is.contains(i))
        l = cons(er, l);
      i++;
    }
    Relation r2 =
        replaceRelationOrPathAt(root, path,
            x(Relation.composition(new Composition(l, c.i, c.o))));
    return adaptComposition(r2, path);
  }

  // FIXME adjust indexes in paths that go through the composition
  public static Relation adaptComposition(Relation root,
      List<Either3<Label, Integer, Unit>> path) {
    Composition c = relationAt(path, root).some().x.composition();

    Either<Relation, List<Either3<Label, Integer, Unit>>> efirst =
        nth(c.l, 0).some().x;
    Either<Relation, List<Either3<Label, Integer, Unit>>> elast =
        nth(c.l, length(c.l) - 1).some().x;
    Code first =
        domain(efirst.isY() ? relationAt(efirst.y(), root).some().x : efirst
            .x());
    Code last =
        codomain(elast.isY() ? relationAt(elast.y(), root).some().x : elast.x());
    if (!equal(first, c.i))
      c = new Composition(cons(x(dummy(c.i, first)), c.l), c.i, c.o);
    if (!equal(last, c.o))
      c = new Composition(append(x(dummy(last, c.o)), c.l), c.i, c.o);

    return Relation.composition(new Composition(adaptComposition_(
        replaceRelationOrPathAt(root, path, x(Relation.composition(c))), c.l),
        c.i, c.o));
  }

  private static List<Either<Relation, List<Either3<Label, Integer, Unit>>>>
      adaptComposition_(Relation root,
          List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l) {
    if (l.isEmpty() || l.cons().tail.isEmpty())
      return l;
    Either<Relation, List<Either3<Label, Integer, Unit>>> er1 = l.cons().x;
    Either<Relation, List<Either3<Label, Integer, Unit>>> er2 =
        l.cons().tail.cons().x;
    Relation r1 = er1.isY() ? relationAt(er1.y(), root).some().x : er1.x();
    Relation r2 = er2.isY() ? relationAt(er2.y(), root).some().x : er2.x();
    if (equal(codomain(r1), domain(r2)))
      return cons(er1, adaptComposition_(root, l.cons().tail));
    return cons(
        er1,
        cons(x(dummy(codomain(r1), domain(r2))),
            adaptComposition_(root, l.cons().tail)));
  }

  /** Empty relation from <tt>i</tt> to <tt>o</tt> */
  public static Relation dummy(Code i, Code o) {
    return Relation.union(new Union(ListUtils
        .<Either<Relation, List<Either3<Label, Integer, Unit>>>> nil(), i, o));
  }

  private static Either<Relation, List<Either3<Label, Integer, Unit>>> x(
      Relation r) {
    return Either.<Relation, List<Either3<Label, Integer, Unit>>> x(r);
  }

  public static Optional<Projection> projection(Code i, Code o) {
    Pair<DirectedMultigraph<Identity<Code.Tag>, Pair<Identity<Code.Tag>, Label>>, Identity<Code.Tag>> p =
        codeToGraph(i);
    Set<Identity<Code.Tag>> vs = new HashSet<Identity<Code.Tag>>();
    return projection(ListUtils.<Label> nil(), p.x, p.y, i, o, vs);
  }

  private static
      Optional<Projection>
      projection(
          List<Label> path,
          DirectedMultigraph<Identity<Code.Tag>, Pair<Identity<Code.Tag>, Label>> g,
          Identity<Code.Tag> v, Code i, Code o, Set<Identity<Code.Tag>> vs) {
    if (vs.contains(v))
      return nothing();
    vs.add(v);
    if (equal(reroot(i, path), o))
      return some(new Projection(path, o));
    for (Pair<Identity<Code.Tag>, Label> e : g.edgesOf(v)) {
      Identity<Code.Tag> t = g.getEdgeTarget(e);
      Optional<Projection> x = projection(append(e.y, path), g, t, i, o, vs);
      if (!x.isNothing())
        return x;
    }
    return nothing();
  }

  public static Relation canonicalRelation(Relation r,
      List<Either3<Label, Integer, Unit>> path) {
    if (path.isEmpty())
      return r;
    throw new RuntimeException("TODO: SHEEEEEEEEIT");
  }

  public static
      List<Pair<Either3<Label, Integer, Unit>, Either<Relation, List<Either3<Label, Integer, Unit>>>>>
      edges(Relation r) {
    switch (r.tag) {
    case ABSTRACTION:
      return fromArray(Pair.pair(Either3.<Label, Integer, Unit> z(unit()),
          r.abstraction().r));
    case COMPOSITION: {
      List<Pair<Either3<Label, Integer, Unit>, Either<Relation, List<Either3<Label, Integer, Unit>>>>> l =
          nil();
      int i = 0;
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> e : iter(r
          .composition().l))
        l = append(Pair.pair(Either3.<Label, Integer, Unit> y(i++), e), l);
      return l;
    }
    case LABEL:
      return fromArray(Pair.pair(Either3.<Label, Integer, Unit> z(unit()),
          r.label().r));
    case PRODUCT: {
      List<Pair<Either3<Label, Integer, Unit>, Either<Relation, List<Either3<Label, Integer, Unit>>>>> l =
          nil();
      for (Entry<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(r
          .product().m.entrySet()))
        l = append(Pair.pair(Either3.<Label, Integer, Unit> x(e.k), e.v), l);
      return l;
    }
    case PROJECTION:
      return nil();
    case UNION:
      List<Pair<Either3<Label, Integer, Unit>, Either<Relation, List<Either3<Label, Integer, Unit>>>>> l =
          nil();
      int i = 0;
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> e : iter(r
          .union().l))
        l = append(Pair.pair(Either3.<Label, Integer, Unit> y(i++), e), l);
      return l;
    default:
      throw boom();
    }
  }

  public static String renderPathElement(Either3<Label, Integer, Unit> e) {
    switch (e.tag) {
    case X:
      return "" + e.x();
    case Y:
      return "" + e.y();
    case Z:
      return "-";
    default:
      throw boom();
    }
  }

  public static Optional<Relation> getRelation(Relation root, Relation r,
      Either3<Label, Integer, Unit> e) {
    Optional<Either<Relation, List<Either3<Label, Integer, Unit>>>> osp =
        subRelationOrPath(r, e);
    if (osp.isNothing())
      return nothing();
    if (osp.some().x.isY())
      return relationAt(osp.some().x.y(), root);
    return some(osp.some().x.x());
  }
}
