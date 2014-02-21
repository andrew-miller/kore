package com.example.kore.ui;

import static com.example.kore.ui.PatternUtils.emptyPattern;
import static com.example.kore.ui.PatternUtils.renderPattern;
import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.CodeUtils.codeToGraph;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.reRoot;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.cycle;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.length;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.ListUtils.nth;
import static com.example.kore.utils.ListUtils.replace;
import static com.example.kore.utils.ListUtils.reverse;
import static com.example.kore.utils.OptionalUtils.nothing;
import static com.example.kore.utils.OptionalUtils.some;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.graph.DirectedMultigraph;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
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

  public static String renderRelation(
      Either<Relation, List<Either3<Label, Integer, Unit>>> rx,
      CodeLabelAliasMap codeLabelAliases) {
    if (rx.isY())
      return "^";
    Relation r = rx.x();
    switch (r.tag) {
    case ABSTRACTION:
      return "("
          + renderPattern(r.abstraction().pattern, r.abstraction().i,
              ListUtils.<Label> nil(), codeLabelAliases) + " -> "
          + renderRelation(r.abstraction().r, codeLabelAliases) + ")";
    case COMPOSITION: {
      List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l =
          r.composition().l;
      if (l.isEmpty())
        return "<>";
      String s = "<" + renderRelation(l.cons().x, codeLabelAliases);
      if (l.cons().tail.isEmpty())
        return s + ">";
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> x : iter(l
          .cons().tail))
        s += "|" + renderRelation(x, codeLabelAliases);
      return s + ">";
    }
    case LABEL: {
      Optional<String> alias =
          codeLabelAliases.getAliases(
              new CanonicalCode(r.label().o, ListUtils.<Label> nil())).get(
              r.label().label);
      return "'" + (alias.isNothing() ? r.label().label : alias.some().x) + " "
          + renderRelation(r.label().r, codeLabelAliases);
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
              + renderRelation(es.cons().x.v, codeLabelAliases);
      if (es.cons().tail.isEmpty())
        return s + "}";
      for (Entry<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(es
          .cons().tail)) {
        la = las.get(e.k);
        s +=
            "," + (la.isNothing() ? e.k : la.some().x) + " "
                + renderRelation(e.v, codeLabelAliases);
      }
      return s + "}";
    }
    case PROJECTION: {
      List<Label> l = r.projection().path;
      String s = "$";
      for (Label x : iter(l))
        s += "." + x.label;
      return s;
    }
    case UNION:
      List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l =
          r.union().l;
      if (l.isEmpty())
        return "[]";
      String s = "[" + renderRelation(l.cons().x, codeLabelAliases);
      if (l.cons().tail.isEmpty())
        return s + "]";
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> x : iter(l
          .cons().tail))
        s += "," + renderRelation(x, codeLabelAliases);
      return s + "]";
    default:
      throw boom();
    }
  }

  public static Optional<Relation> subRelation(Relation r,
      Either3<Label, Integer, Unit> e) {
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
      throw new RuntimeException("path goes through projection");
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

  private static Optional<Relation> f(
      Optional<Either<Relation, List<Either3<Label, Integer, Unit>>>> r) {
    if (r.isNothing())
      return nothing();
    if (r.some().x.isY())
      throw new RuntimeException("TODO handle link");
    return some(r.some().x.x());
  }

  public static Optional<Relation> relationAt(
      List<Either3<Label, Integer, Unit>> path, Relation r) {
    for (Either3<Label, Integer, Unit> e : iter(path)) {
      Optional<Relation> or = subRelation(r, e);
      if (or.isNothing())
        return nothing();
      r = or.some().x;
    }
    return some(r);
  }

  public static Relation replaceRelationAt(Relation r,
      List<Either3<Label, Integer, Unit>> p, Relation newRelation) {
    if (p.isEmpty())
      return newRelation;
    Either3<Label, Integer, Unit> e = p.cons().x;
    List<Either3<Label, Integer, Unit>> p2 = p.cons().tail;
    switch (r.tag) {
    case ABSTRACTION:
      if (e.tag != Tag.Z)
        throw new RuntimeException("invalid path");
      Abstraction a = r.abstraction();
      return Relation.abstraction(new Relation.Abstraction(a.pattern, Either
          .<Relation, List<Either3<Label, Integer, Unit>>> x(replaceRelationAt(
              a.r.x(), p2, newRelation)), a.i, a.o));
    case COMPOSITION:
      if (e.tag != Tag.Y)
        throw new RuntimeException("invalid path");
      Composition c = r.composition();
      return Relation.composition(new Relation.Composition(replace(
          r.composition().l, e.y(),
          x(replaceRelationAt(nth(c.l, e.y()).some().x.x(), p2, newRelation))),
          c.i, c.o));
    case PRODUCT:
      if (e.tag != Tag.X)
        throw new RuntimeException("invalid path");
      Product prod = r.product();
      return Relation.product(new Relation.Product(
          prod.m.put(
              e.x(),
              x(replaceRelationAt(prod.m.get(e.x()).some().x.x(), p2,
                  newRelation))), prod.o));
    case PROJECTION:
      throw new RuntimeException("path goes through projection");
    case LABEL:
      if (e.tag != Tag.Z)
        throw new RuntimeException("invalid path");
      return Relation.label(new Label_(r.label().label, x(replaceRelationAt(
          r.label().r.x(), p2, newRelation)), r.label().o));
    case UNION:
      if (e.tag != Tag.Y)
        throw new RuntimeException("invalid path");
      Union u = r.union();
      return Relation.union(new Relation.Union(replace(u.l, e.y(),
          x(replaceRelationAt(nth(u.l, e.y()).some().x.x(), p2, newRelation))),
          u.i, u.o));
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

  /**
   * A relation from <tt>d</tt> to <tt>c</tt> if there's one that's not
   * <tt>[]</tt>
   */
  public static Optional<Relation> element(Code d, Code c) {
    if (d.equals(unit))
      return element(c);
    switch (d.tag) {
    case PRODUCT:
      return nothing();
    case UNION:
      switch (c.tag) {
      case UNION:
        List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l = nil();
        found: {
          for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet()))
            if (!element(reRoot(c, cons(e.k, ListUtils.<Label> nil())))
                .isNothing())
              break found;
          return nothing();
        }
        Iterator<Entry<Label, CodeOrPath>> i =
            cycle(reverse(c.labels.entrySet())).iterator();
        for (Entry<Label, CodeOrPath> e : iter(d.labels.entrySet())) {
          Optional<Relation> or;
          do
            or = element(reRoot(c, cons(i.next().k, ListUtils.<Label> nil())));
          while (or.isNothing());
          l =
              cons(x(Relation.abstraction(new Abstraction(new Pattern(Map
                  .<Label, Pattern> empty().put(e.k, emptyPattern)), x(or
                  .some().x), d, c))), l);
        }
        if (l.isEmpty())
          return nothing();
        return some(Relation.union(new Union(l, c, d)));
      case PRODUCT:
        Optional<Relation> oe = element(c);
        return some(Relation.abstraction(new Abstraction(emptyPattern, x(oe
            .some().x), d, c)));
      }
    default:
      throw boom();
    }
  }

  /**
   * A relation from <tt>{}</tt> to <tt>c</tt> if there's one that's not
   * <tt>[]</tt>
   */
  public static Optional<Relation> element(Code c) {
    Pair<DirectedMultigraph<Identity<Code.Tag>, Pair<Identity<Code.Tag>, Label>>, Identity<Code.Tag>> p =
        codeToGraph(c);
    HashSet<Identity<Code.Tag>> s = new HashSet<Identity<Code.Tag>>();
    s.add(p.y);
    return element(p.y, p.x, s, c);
  }

  private static
      Optional<Relation>
      element(
          Identity<Code.Tag> v,
          DirectedMultigraph<Identity<Code.Tag>, Pair<Identity<Code.Tag>, Label>> g,
          HashSet<Identity<Code.Tag>> vs, Code c) {
    switch (v.t) {
    case UNION:
      for (Pair<Identity<Code.Tag>, Label> e : g.edgesOf(v))
        if (e.x == v) {
          CodeOrPath cp = c.labels.get(e.y).some().x;
          if (cp.tag == CodeOrPath.Tag.CODE) {
            Identity<Code.Tag> v2 = g.getEdgeTarget(e);
            if (vs.contains(v2))
              return nothing();
            vs.add(v2);
            Optional<Relation> r = element(v2, g, vs, cp.code);
            vs.remove(v);
            if (!r.isNothing())
              return some(Relation.label(new Label_(e.y, x(r.some().x), c)));
          }
        }
      return nothing();
    case PRODUCT:
      Map<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> m =
          Map.empty();
      for (Pair<Identity<Code.Tag>, Label> e : g.edgesOf(v))
        if (e.x == v) {
          CodeOrPath cp = c.labels.get(e.y).some().x;
          if (cp.tag == CodeOrPath.Tag.PATH)
            return nothing();
          Identity<Code.Tag> v2 = g.getEdgeTarget(e);
          if (vs.contains(v2))
            return nothing();
          vs.add(v2);
          Optional<Relation> r = element(v2, g, vs, cp.code);
          vs.remove(v);
          if (r.isNothing())
            return nothing();
          m = m.put(e.y, x(r.some().x));
        }
      return some(Relation.product(new Product(m, c)));
    default:
      throw boom();
    }
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
        replaceRelationAt(root, path,
            Relation.composition(new Composition(l, c.i, c.o)));
    return adaptComposition(r2, path);
  }

  // FIXME adjusts indexes in paths that go through the composition
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
      c = new Composition(cons(x(elementOrDummy(c.i, first)), c.l), c.i, c.o);
    if (!equal(last, c.o))
      c = new Composition(append(x(elementOrDummy(last, c.o)), c.l), c.i, c.o);

    return Relation
        .composition(new Composition(adaptComposition_(
            replaceRelationAt(root, path, Relation.composition(c)), c.l), c.i,
            c.o));
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
    Relation t = elementOrDummy(codomain(r1), domain(r2));
    return cons(er1, cons(x(t), adaptComposition_(root, l.cons().tail)));
  }

  /** Empty relation from <tt>i</tt> to <tt>o</tt> */
  public static Relation dummyRelation(Code i, Code o) {
    return Relation.abstraction(new Abstraction(emptyPattern, x(Relation
        .union(new Union(nil, i, o))), i, o));
  }

  public static Relation elementOrDummy(Code c1, Code c2) {
    Optional<Relation> ot = element(c1, c2);
    return ot.isNothing() ? dummyRelation(c1, c2) : ot.some().x;
  }

  private static List<Either<Relation, List<Either3<Label, Integer, Unit>>>> nil =
      ListUtils.<Either<Relation, List<Either3<Label, Integer, Unit>>>> nil();

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
    if (equal(reRoot(i, path), o))
      return some(new Projection(path, o));
    for (Pair<Identity<Code.Tag>, Label> e : g.edgesOf(v)) {
      Identity<Code.Tag> t = g.getEdgeTarget(e);
      Optional<Projection> x = projection(append(e.y, path), g, t, i, o, vs);
      if (!x.isNothing())
        return x;
    }
    return nothing();
  }
}