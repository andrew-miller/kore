package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.PatternUtils.emptyPattern;
import static com.pokemon.kore.ui.PatternUtils.renderPattern;
import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.CodeUtils.codeToGraph;
import static com.pokemon.kore.utils.CodeUtils.directPath;
import static com.pokemon.kore.utils.CodeUtils.equal;
import static com.pokemon.kore.utils.CodeUtils.reroot;
import static com.pokemon.kore.utils.CodeUtils.unit;
import static com.pokemon.kore.utils.LinkTreeUtils.canonicalLinkTree;
import static com.pokemon.kore.utils.LinkTreeUtils.rebase;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.cons;
import static com.pokemon.kore.utils.ListUtils.drop;
import static com.pokemon.kore.utils.ListUtils.fromArray;
import static com.pokemon.kore.utils.ListUtils.insert;
import static com.pokemon.kore.utils.ListUtils.isPrefix;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.length;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.ListUtils.nth;
import static com.pokemon.kore.utils.ListUtils.replace;
import static com.pokemon.kore.utils.MapUtils.containsKey;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.OptionalUtils.some;
import static com.pokemon.kore.utils.PairUtils.pair;
import static com.pokemon.kore.utils.Unit.unit;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jgrapht.graph.DirectedMultigraph;

import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.RVertex;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.codes.Relation.Abstraction;
import com.pokemon.kore.codes.Relation.Composition;
import com.pokemon.kore.codes.Relation.Label_;
import com.pokemon.kore.codes.Relation.Product;
import com.pokemon.kore.codes.Relation.Projection;
import com.pokemon.kore.codes.Relation.Union;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.Either3.Tag;
import com.pokemon.kore.utils.Either3Comparer;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.Identity;
import com.pokemon.kore.utils.IntegerComparer;
import com.pokemon.kore.utils.LabelComparer;
import com.pokemon.kore.utils.LinkTreeUtils;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.ListUtils;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;
import com.pokemon.kore.utils.UnitComparer;

public class RelationUtils {
  public static final Relation unit_unit = Relation
      .abstraction(new Abstraction(emptyPattern, Either.x(Relation
          .product(new Product(Map.empty(), unit))), unit, unit));

  /**
   * @param argCode
   *          required if there <tt>rx</tt> is a projection or <tt>rx</tt>
   *          contains a projection without an abstraction on the path from it
   *          to that projection
   */
  public static String renderRelation(Optional<Code> argCode,
      Either<Relation, List<Either3<Label, Integer, Unit>>> rx,
      CodeLabelAliasMap codeLabelAliases) {
    if (rx.tag == rx.tag.Y)
      return "^";
    Relation r = rx.x();
    switch (r.tag) {
    case ABSTRACTION:
      return "("
          + renderPattern(r.abstraction().pattern, r.abstraction().i, nil(),
              codeLabelAliases)
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
          codeLabelAliases.getAliases(new CanonicalCode(r.label().o, nil())).xy
              .get(r.label().label);
      return "'" + (alias.isNothing() ? r.label().label : alias.some().x) + " "
          + renderRelation(argCode, r.label().r, codeLabelAliases);
    }
    case PRODUCT: {
      Bijection<Label, String> las =
          codeLabelAliases.getAliases(new CanonicalCode(r.product().o, nil()));
      List<Pair<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>>> es =
          r.product().m.entrySet();
      if (es.isEmpty())
        return "{}";
      Optional<String> la = las.xy.get(es.cons().x.x);
      String s =
          "{'" + (la.isNothing() ? es.cons().x.x : la.some().x) + " "
              + renderRelation(argCode, es.cons().x.y, codeLabelAliases);
      if (es.cons().tail.isEmpty())
        return s + "}";
      for (Pair<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(es
          .cons().tail)) {
        la = las.xy.get(e.x);
        s +=
            "," + (la.isNothing() ? e.x : la.some().x) + " "
                + renderRelation(argCode, e.y, codeLabelAliases);
      }
      return s + "}";
    }
    case PROJECTION: {
      String s = "$";
      List<Label> p = nil();
      for (Label l : iter(r.projection().path)) {
        Optional<String> a =
            codeLabelAliases.getAliases(new CanonicalCode(argCode.some().x,
                directPath(p, argCode.some().x))).xy.get(l);
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
    if (oe.some().x.tag == Either.Tag.Y)
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
    return replaceRelationOrPathAt(Either.x(r), p, er).x();
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
      return Either.x(Relation.abstraction(new Relation.Abstraction(a.pattern,
          replaceRelationOrPathAt(a.r, p2, er2), a.i, a.o)));
    case COMPOSITION:
      if (e.tag != Tag.Y)
        throw new RuntimeException("invalid path");
      Composition c = r.composition();
      return Either
          .x(Relation.composition(new Relation.Composition(replace(
              r.composition().l, e.y(),
              replaceRelationOrPathAt(nth(c.l, e.y()).some().x, p2, er2)), c.i,
              c.o)));
    case PRODUCT:
      if (e.tag != Tag.X)
        throw new RuntimeException("invalid path");
      Product prod = r.product();
      return Either
          .x(Relation.product(new Relation.Product(prod.m.put(e.x(),
              replaceRelationOrPathAt(prod.m.get(e.x()).some().x, p2, er2)),
              prod.o)));
    case PROJECTION:
      throw new RuntimeException("path goes through projection");
    case LABEL:
      if (e.tag != Tag.Z)
        throw new RuntimeException("invalid path");
      return Either.x(Relation.label(new Label_(r.label().label,
          replaceRelationOrPathAt(r.label().r, p2, er2), r.label().o)));
    case UNION:
      if (e.tag != Tag.Y)
        throw new RuntimeException("invalid path");
      Union u = r.union();
      return Either
          .x(Relation.union(new Relation.Union(replace(u.l, e.y(),
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

  public static Optional<Abstraction> enclosingAbstraction(
      List<Either3<Label, Integer, Unit>> path, Relation relation) {
    return enclosingAbstraction(path, relation, nothing());
  }

  private static Optional<Abstraction> enclosingAbstraction(
      List<Either3<Label, Integer, Unit>> path, Relation relation,
      Optional<Abstraction> a) {
    if (path.isEmpty())
      return a;
    Either<Relation, List<Either3<Label, Integer, Unit>>> srop =
        subRelationOrPath(relation, path.cons().x).some().x;
    Optional<Abstraction> a2 =
        relation.tag == Relation.Tag.ABSTRACTION ? some(relation.abstraction())
            : a;
    switch (srop.tag) {
    case X:
      return enclosingAbstraction(path.cons().tail, srop.x(), a2);
    case Y:
      return a2;
    }
    throw boom();
  }

  /**
   * Insert transformations between elements of the composition that are not
   * compatible. Also pads the start and end of the composition if the first
   * element's domain is not the same as the composition's domain, or the last
   * element's codomain is not the same as the composition's codomain,
   * respectively.
   */
  public static Relation adaptComposition(Relation root,
      List<Either3<Label, Integer, Unit>> path) {
    Composition c = relationAt(path, root).some().x.composition();
    Pair<List<Boolean>, List<Either<Relation, List<Either3<Label, Integer, Unit>>>>> p =
        adaptComposition_(root, c.i, c.l);
    Either<Relation, List<Either3<Label, Integer, Unit>>> elast =
        nth(c.l, length(c.l) - 1).some().x;
    Code last = codomain(resolve(root, elast));
    return mapPaths(
        replaceRelationOrPathAt(root, path, Either.x(Relation
            .composition(new Composition(equal(last, c.o) ? p.y : append(
                Either.x(defaultValue(last, c.o)), p.y), c.i, c.o)))),
        p2 -> {
          if (isPrefix(path, p2)) {
            List<Either3<Label, Integer, Unit>> l = drop(p2, length(path));
            if (!l.isEmpty()) {
              Integer o = 0;
              for (Boolean b_ : iter(ListUtils.take(p.x, l.cons().x.y() + 1)))
                if (b_)
                  o++;
              return append(path,
                  cons(Either3.y(l.cons().x.y() + o), l.cons().tail));
            }
          }
          return p2;
        });
  }

  private static
      Pair<List<Boolean>, List<Either<Relation, List<Either3<Label, Integer, Unit>>>>>
      adaptComposition_(Relation root, Code c,
          List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l) {
    if (l.isEmpty())
      return pair(nil(), l);
    Either<Relation, List<Either3<Label, Integer, Unit>>> er = l.cons().x;
    Relation r = resolve(root, er);
    Pair<List<Boolean>, List<Either<Relation, List<Either3<Label, Integer, Unit>>>>> p =
        adaptComposition_(root, codomain(r), l.cons().tail);
    return equal(c, domain(r)) ? pair(cons(false, p.x), cons(er, p.y)) : pair(
        cons(true, p.x),
        cons(Either.x(defaultValue(c, domain(r))), cons(er, p.y)));
  }

  /** Empty relation from <tt>i</tt> to <tt>o</tt> */
  public static Relation dummy(Code i, Code o) {
    return Relation.union(new Union(nil(), i, o));
  }

  public static Optional<Projection> projection(Code i, Code o) {
    Pair<DirectedMultigraph<Identity<Code.Tag>, Pair<Identity<Code.Tag>, Label>>, Identity<Code.Tag>> p =
        codeToGraph(i);
    Set<Identity<Code.Tag>> vs = new HashSet<>();
    return projection(nil(), p.x, p.y, i, o, vs);
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
    return linkTreeToRelation(canonicalLinkTree(linkTree(r), path,
        new Either3Comparer<>(new LabelComparer(), new IntegerComparer(),
            new UnitComparer())));
  }

  public static
      List<Pair<Either3<Label, Integer, Unit>, Either<Relation, List<Either3<Label, Integer, Unit>>>>>
      edges(Relation r) {
    switch (r.tag) {
    case ABSTRACTION:
      return fromArray(pair(Either3.z(unit()), r.abstraction().r));
    case COMPOSITION: {
      List<Pair<Either3<Label, Integer, Unit>, Either<Relation, List<Either3<Label, Integer, Unit>>>>> l =
          nil();
      int i = 0;
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> e : iter(r
          .composition().l))
        l = append(pair(Either3.y(i++), e), l);
      return l;
    }
    case LABEL:
      return fromArray(pair(Either3.z(unit()), r.label().r));
    case PRODUCT: {
      List<Pair<Either3<Label, Integer, Unit>, Either<Relation, List<Either3<Label, Integer, Unit>>>>> l =
          nil();
      for (Pair<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(r
          .product().m.entrySet()))
        l = append(pair(Either3.x(e.x), e.y), l);
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
        l = append(pair(Either3.y(i++), e), l);
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
    if (osp.some().x.tag == Either.Tag.Y)
      return relationAt(osp.some().x.y(), root);
    return some(osp.some().x.x());
  }

  public static Relation resolve(Relation root,
      Either<Relation, List<Either3<Label, Integer, Unit>>> rp) {
    return rp.tag == rp.tag.X ? rp.x() : relationAt(rp.y(), root).some().x;
  }

  public static
      Relation
      mapPaths(
          Relation r,
          F<List<Either3<Label, Integer, Unit>>, List<Either3<Label, Integer, Unit>>> f) {
    return linkTreeToRelation(LinkTreeUtils.mapPaths(linkTree(r), f));
  }

  public static
      Either<Relation, List<Either3<Label, Integer, Unit>>>
      map_(
          Either<Relation, List<Either3<Label, Integer, Unit>>> r,
          F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Either<Relation, List<Either3<Label, Integer, Unit>>>> f) {
    switch (r.tag) {
    case X:
      return map(r.x(), f);
    case Y:
      return f.f(r);
    default:
      throw boom();
    }
  }

  public static
      Either<Relation, List<Either3<Label, Integer, Unit>>>
      map(Relation r,
          F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Either<Relation, List<Either3<Label, Integer, Unit>>>> f) {
    switch (r.tag) {
    case ABSTRACTION:
      Abstraction a = r.abstraction();
      return f.f(Either.x(Relation.abstraction(new Abstraction(a.pattern, map_(
          a.r, f), a.i, a.o))));
    case COMPOSITION: {
      Composition c = r.composition();
      List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l = nil();
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> er : iter(c.l))
        l = append(map_(er, f), l);
      return f.f(Either.x(Relation.composition(new Composition(l, c.i, c.o))));
    }
    case LABEL: {
      Label_ l = r.label();
      return f.f(Either.x(Relation
          .label(new Label_(l.label, map_(l.r, f), l.o))));
    }
    case PRODUCT: {
      Product p = r.product();
      Map<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> m =
          Map.empty();
      for (Pair<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(p.m
          .entrySet()))
        m = m.put(e.x, map_(e.y, f));
      return f.f(Either.x(Relation.product(new Product(m, p.o))));
    }
    case PROJECTION:
      Projection p = r.projection();
      return f.f(Either.x(Relation.projection(new Projection(p.path, p.o))));
    case UNION:
      Union u = r.union();
      List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l = nil();
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> er : iter(u.l))
        l = append(map_(er, f), l);
      return f.f(Either.x(Relation.union(new Union(l, u.i, u.o))));
    default:
      throw boom();
    }
  }

  public static Relation extendComposition(Relation relation,
      List<Either3<Label, Integer, Unit>> path, Integer i,
      Either<Relation, List<Either3<Label, Integer, Unit>>> r2) {
    if (i < 0)
      throw new RuntimeException("index can't be negative");
    Either<Relation, List<Either3<Label, Integer, Unit>>> er =
        relationOrPathAt(path, relation);
    Relation r1 = resolve(relation, er);
    Code d = domain(r1);
    Code c = codomain(r1);

    Composition comp;
    Either<Relation, List<Either3<Label, Integer, Unit>>> r2rb =
        r2.tag == r2.tag.X ? Either.x(linkTreeToRelation(rebase(
            append(Either3.y(i), path), linkTree(r2.x())))) : r2;
    if (er.tag == er.tag.X && er.x().tag == Relation.Tag.COMPOSITION) {
      comp =
          new Composition(
              insert(er.x().composition().l, i, Either.x(unit_unit)), d, c);
      return adaptComposition(
          replaceRelationOrPathAt(
              bumpIndexes(
                  replaceRelationOrPathAt(relation, path,
                      Either.x(Relation.composition(comp))), i, path),
              append(Either3.y(i), path), r2rb), path);
    } else {
      switch (i) {
      case 0:
        comp = new Composition(fromArray(Either.x(unit_unit), er), d, c);
        break;
      case 1:
        comp = new Composition(fromArray(er, Either.x(unit_unit)), d, c);
        break;
      default:
        throw new RuntimeException("invalid index");
      }
      return adaptComposition(
          replaceRelationOrPathAt(
              insertIndexes(
                  replaceRelationOrPathAt(relation, path,
                      Either.x(Relation.composition(comp))), i, path),
              append(Either3.y(i), path), r2rb), path);
    }
  }

  public static Relation extendUnion(Relation relation,
      List<Either3<Label, Integer, Unit>> path, Integer i,
      Either<Relation, List<Either3<Label, Integer, Unit>>> er2) {
    if (i < 0)
      throw new RuntimeException("index can't be negative");
    Either<Relation, List<Either3<Label, Integer, Unit>>> er =
        relationOrPathAt(path, relation);
    Relation r1 = resolve(relation, er);
    Code d = domain(r1);
    Code c = codomain(r1);
    Relation r2 = resolve(relation, er2);
    Code d2 = domain(r2);
    Code c2 = codomain(r2);
    if (!equal(d, d2)) {
      Relation t = defaultValue(d, d2);
      if (er2.tag == er2.tag.X && er2.x().tag == Relation.Tag.COMPOSITION)
        er2 =
            Either.x(Relation.composition(new Composition(cons(Either.x(t), er2
                .x().composition().l), d, c2)));
      else
        er2 =
            Either.x(Relation.composition(new Composition(fromArray(
                Either.x(t), er2), d, c2)));
    }
    if (!equal(c, c2)) {
      Relation t = defaultValue(c2, c);
      if (er2.tag == er2.tag.X && er2.x().tag == Relation.Tag.COMPOSITION)
        er2 =
            Either.x(Relation.composition(new Composition(append(Either.x(t),
                er2.x().composition().l), d, c)));
      else
        er2 =
            Either.x(Relation.composition(new Composition(fromArray(er2,
                Either.x(t)), d, c)));
    }
    Either<Relation, List<Either3<Label, Integer, Unit>>> r2rb =
        er2.tag == er2.tag.X ? Either.x(linkTreeToRelation(rebase(
            append(Either3.y(i), path), linkTree(er2.x())))) : er2;
    Union union;
    if (er.tag == er.tag.X && er.x().tag == Relation.Tag.UNION) {
      union =
          new Union(insert(er.x().union().l, i, Either.x(unit_unit)), er.x()
              .union().i, er.x().union().o);
      return replaceRelationOrPathAt(
          bumpIndexes(
              replaceRelationOrPathAt(relation, path,
                  Either.x(Relation.union(union))), i, path),
          append(Either3.y(i), path), r2rb);
    } else {
      switch (i) {
      case 0:
        union = new Union(fromArray(Either.x(unit_unit), er), d, c);
        break;
      case 1:
        union = new Union(fromArray(er, Either.x(unit_unit)), d, c);
        break;
      default:
        throw new RuntimeException("invalid index");
      }
      return replaceRelationOrPathAt(
          insertIndexes(
              replaceRelationOrPathAt(relation, path,
                  Either.x(Relation.union(union))), i, path),
          append(Either3.y(i), path), r2rb);
    }
  }

  private static Relation bumpIndexes(Relation r, Integer i,
      List<Either3<Label, Integer, Unit>> path) {
    return mapPaths(
        r,
        p -> {
          if (isPrefix(path, p)) {
            List<Either3<Label, Integer, Unit>> l = drop(p, length(path));
            if (!l.isEmpty() && l.cons().x.y() >= i)
              return append(path,
                  cons(Either3.y(l.cons().x.y() + 1), l.cons().tail));
          }
          return p;
        });
  }

  private static Relation insertIndexes(Relation r, Integer i,
      List<Either3<Label, Integer, Unit>> path) {
    return mapPaths(
        r,
        p -> isPrefix(path, p) ? append(path,
            cons(Either3.y((i + 1) % 2), drop(p, length(path)))) : p);
  }

  /**
   * <code>d</code> is the argument code when <code>t</code> is
   * <code>Projection</code>
   */
  public static Relation emptyRelation(Code d, Code c, Relation.Tag t) {
    switch (t) {
    case ABSTRACTION:
      return Relation.abstraction(new Abstraction(emptyPattern, Either
          .x(defaultValue(unit, c)), d, c));
    case COMPOSITION:
      return Relation.composition(new Composition(nil(), d, c));
    case PRODUCT:
      if (!equal(d, unit))
        throw new RuntimeException("cannot make product with non-unit domain");
      Map<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> m =
          Map.empty();
      for (Pair<Label, ?> e : iter(c.labels.entrySet()))
        m = m.put(e.x, Either.x(defaultValue(unit, reroot(c, fromArray(e.x)))));
      return Relation.product(new Product(m, c));
    case LABEL:
      if (!equal(d, unit))
        throw new RuntimeException("cannot make label with non-unit domain");
      for (Pair<Label, ?> e : iter(c.labels.entrySet()))
        return Relation.label(new Label_(e.x, Either.x(defaultValue(unit,
            reroot(c, fromArray(e.x)))), c));
      return defaultValue(d, c);
    case PROJECTION:
      Optional<Projection> or2 = projection(d, c);
      return or2.isNothing() ? defaultValue(d, c) : Relation.projection(or2
          .some().x);
    case UNION:
      return dummy(d, c);
    default:
      throw boom();
    }
  }

  private static Relation adaptReferences(Relation relation,
      List<Either3<Label, Integer, Unit>> path, Code o, Either<Code, Code> dc) {
    F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Either<Relation, List<Either3<Label, Integer, Unit>>>> f =
        er -> {
          switch (er.tag) {
          case X:
            return er;
          case Y:
            if (!er.y().equals(path))
              return er;
            Relation r = relationAt(er.y(), relation).some().x;
            Code d = domain(r);
            Code c = codomain(r);
            switch (dc.tag) {
            case X:
              return Either.x(Relation.composition(new Composition(fromArray(
                  Either.x(defaultValue(o, dc.x())), er), o, c)));
            case Y:
              return Either.x(Relation.composition(new Composition(fromArray(
                  er, Either.x(defaultValue(dc.y(), o))), d, o)));
            default:
              throw boom();
            }
          default:
            throw boom();
          }
        };
    return map(relation, f).x();
  }

  public static Relation changeCodomain(Relation relation,
      List<Either3<Label, Integer, Unit>> path, Code c2) {
    Relation r = relationAt(path, relation).some().x;
    Code d = domain(r);
    Code c = codomain(r);
    Relation t = defaultValue(c, c2);
    switch (r.tag) {
    case COMPOSITION:
      return adaptReferences(
          replaceRelationOrPathAt(relation, path, Either.x(Relation
              .composition(new Composition(append(Either.x(t),
                  r.composition().l), d, c2)))), path, c, Either.y(c2));
    case ABSTRACTION:
    case LABEL:
    case PRODUCT:
    case PROJECTION:
    case UNION:
      return insertIndexes(
          replaceRelationOrPathAt(
              relation,
              path,
              Either.x(Relation.composition(new Composition(fromArray(
                  Either.x(r), Either.x(t)), d, c2)))), 1, path);
    default:
      throw boom();
    }
  }

  public static Relation changeDomain(Relation relation,
      List<Either3<Label, Integer, Unit>> path, Code d2) {
    Relation r = relationAt(path, relation).some().x;
    Code d = domain(r);
    Code c = codomain(r);
    Relation t = defaultValue(d2, d);
    switch (r.tag) {
    case COMPOSITION:
      return adaptReferences(
          bumpIndexes(
              replaceRelationOrPathAt(
                  relation,
                  path,
                  Either.x(Relation.composition(new Composition(cons(
                      Either.x(t), r.composition().l), d2, c)))), 0, path),
          path, d, Either.x(d2));
    case ABSTRACTION:
    case LABEL:
    case PRODUCT:
    case PROJECTION:
    case UNION:
      return insertIndexes(
          replaceRelationOrPathAt(
              relation,
              path,
              Either.x(Relation.composition(new Composition(fromArray(
                  Either.x(t), Either.x(r)), d2, c)))), 0, path);
    default:
      throw boom();
    }
  }

  public static LinkTree<Either3<Label, Integer, Unit>, RVertex> linkTree(
      Relation r) {
    return new LinkTree<Either3<Label, Integer, Unit>, RVertex>() {
      public
          List<Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>>>
          edges() {
        switch (r.tag) {
        case ABSTRACTION:
          Abstraction a = r.abstraction();
          return fromArray(pair(Either3.z(unit()), f(a.r)));
        case COMPOSITION: {
          List<Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>>> l =
              nil();
          int i = 0;
          Composition c = r.composition();
          for (Either<Relation, List<Either3<Label, Integer, Unit>>> r : iter(c.l))
            l = cons(pair(Either3.y(i++), f(r)), l);
          return l;
        }
        case LABEL:
          return fromArray(pair(Either3.z(unit()), f(r.label().r)));
        case PRODUCT: {
          List<Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>>> l =
              nil();
          for (Pair<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(r
              .product().m.entrySet()))
            l = cons(pair(Either3.x(e.x), f(e.y)), l);
          return l;
        }
        case PROJECTION:
          return nil();
        case UNION:
          List<Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>>> l =
              nil();
          int i = 0;
          for (Either<Relation, List<Either3<Label, Integer, Unit>>> r_ : iter(r
              .union().l))
            l = cons(pair(Either3.y(i++), f(r_)), l);
          return l;
        default:
          throw boom();
        }
      }

      private
          Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>
          f(Either<Relation, List<Either3<Label, Integer, Unit>>> rp) {
        switch (rp.tag) {
        case X:
          return Either.x(linkTree(rp.x()));
        case Y:
          return Either.y(rp.y());
        default:
          throw boom();
        }
      }

      public RVertex vertex() {
        switch (r.tag) {
        case ABSTRACTION:
          return RVertex.abstraction(new RVertex.Abstraction(
              r.abstraction().pattern, r.abstraction().i, r.abstraction().o));
        case COMPOSITION:
          return RVertex.composition(new RVertex.Composition(r.composition().i,
              r.composition().o));
        case LABEL:
          return RVertex.label(new RVertex.Label(r.label().label, r.label().o));
        case PRODUCT:
          return RVertex.product(new RVertex.Product(r.product().o));
        case PROJECTION:
          return RVertex.projection(new RVertex.Projection(r.projection().path,
              r.projection().o));
        case UNION:
          return RVertex.union(new RVertex.Union(r.union().i, r.union().o));
        default:
          throw boom();
        }
      }
    };
  }

  public static Relation linkTreeToRelation(
      LinkTree<Either3<Label, Integer, Unit>, RVertex> lt) {
    switch (lt.vertex().tag) {
    case ABSTRACTION:
      if (!lt.edges().cons().tail.isEmpty())
        throw boom();
      lt.edges().cons().x.x.z();
      RVertex.Abstraction a = lt.vertex().abstraction();
      return Relation.abstraction(new Abstraction(a.pattern, f(lt.edges()
          .cons().x.y), a.i, a.o));
    case COMPOSITION:
    case UNION: {
      SortedMap<Integer, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>> sm =
          new TreeMap<>();
      for (Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>> p : iter(lt
          .edges()))
        if (sm.put(p.x.y(), p.y) != null)
          throw new RuntimeException("duplicate index");
      List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l = nil();
      int n = -1;
      for (java.util.Map.Entry<Integer, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>> e : sm
          .entrySet()) {
        if (n++ + 1 != e.getKey())
          throw new RuntimeException("missing an index");
        l = append(f(e.getValue()), l);
      }
      switch (lt.vertex().tag) {
      case COMPOSITION:
        RVertex.Composition c = lt.vertex().composition();
        return Relation.composition(new Composition(l, c.i, c.o));
      case UNION:
        RVertex.Union u = lt.vertex().union();
        return Relation.union(new Union(l, u.i, u.o));
      }
    }
    case LABEL:
      if (!lt.edges().cons().tail.isEmpty())
        throw boom();
      RVertex.Label l = lt.vertex().label();
      return Relation.label(new Label_(l.l, f(lt.edges().cons().x.y), l.o));
    case PRODUCT: {
      RVertex.Product p = lt.vertex().product();
      Map<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> m =
          Map.empty();
      for (Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>> e : iter(lt
          .edges())) {
        if (containsKey(m, e.x.x()))
          throw new RuntimeException("duplicate label");
        m = m.put(e.x.x(), f(e.y));
      }
      return Relation.product(new Product(m, p.o));
    }
    case PROJECTION:
      if (!lt.edges().isEmpty())
        throw boom();
      RVertex.Projection p = lt.vertex().projection();
      return Relation.projection(new Projection(p.path, p.o));
    default:
      throw boom();
    }
  }

  private static
      Either<Relation, List<Either3<Label, Integer, Unit>>>
      f(Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>> e) {
    switch (e.tag) {
    case X:
      return Either.x(linkTreeToRelation(e.x()));
    case Y:
      return Either.y(e.y());
    default:
      throw boom();
    }
  }

  public static Relation defaultValue(Code i, Code o) {
    if (equal(i, unit)) {
      Optional<Relation> od = defaultValue(new CanonicalCode(o, nil()).code);
      return od.isNothing() ? dummy(i, o) : od.some().x;
    }
    return dummy(i, o);
  }

  /** if <code>c</code> has only one possible value, return that value */
  public static Optional<Relation> defaultValue(Code c) {
    switch (c.tag) {
    case PRODUCT:
      Map<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> fields =
          Map.empty();
      for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels.entrySet()))
        switch (e.y.tag) {
        case X:
          Optional<Relation> od = defaultValue(e.y.x());
          if (od.isNothing())
            return nothing();
          fields = fields.put(e.x, Either.x(od.some().x));
          break;
        case Y:
          return nothing();
        }
      return some(Relation.product(new Product(fields, c)));
    case UNION:
      List<Pair<Label, Either<Code, List<Label>>>> es = c.labels.entrySet();
      if (!es.isEmpty() && es.cons().tail.isEmpty()) {
        Pair<Label, Either<Code, List<Label>>> e = es.cons().x;
        switch (e.y.tag) {
        case X:
          Optional<Relation> od = defaultValue(e.y.x());
          if (od.isNothing())
            return nothing();
          return some(Relation.label(new Label_(e.x, Either.x(od.some().x), c)));
        case Y:
          return nothing();
        }
      }
      return nothing();
    default:
      throw boom();
    }
  }

  public static RelationView.Listener emptyRelationViewListener =
      new RelationView.Listener() {
        public void selectPath(List<Either3<Label, Integer, Unit>> path) {
        }

        public void select(List<Either3<Label, Integer, Unit>> path) {
        }

        public void
            extendUnion(List<Either3<Label, Integer, Unit>> path, Integer i,
                Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
        }

        public void
            extendComposition(List<Either3<Label, Integer, Unit>> path,
                Integer i,
                Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
        }

        public boolean dontAbbreviate(List<Either3<Label, Integer, Unit>> path) {
          return false;
        }

        public void replaceRelation(List<Either3<Label, Integer, Unit>> path,
            Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
        }
      };

  /**
   * Replace the relation at <code>path</code> in <code>relation</code> with
   * <code>er</code>, adapting the domain/codomain of <code>er</code> if
   * necessary. <code>replaceRelationOrPathAt(relation, path, er)</code> must
   * make <code>relation</code> a valid link tree
   */
  public static Relation replaceRelation(Relation relation,
      List<Either3<Label, Integer, Unit>> path,
      Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
    Either<Relation, List<Either3<Label, Integer, Unit>>> rp =
        relationOrPathAt(path, relation);
    Relation r = resolve(relation, rp);
    Relation r2 = resolve(relation, er);
    return !equal(domain(r), domain(r2)) | !equal(codomain(r), codomain(r2)) ? adaptComposition(
        insertIndexes(
            replaceRelationOrPathAt(relation, path, Either.x(Relation
                .composition(new Composition(fromArray(er), domain(r),
                    codomain(r))))), 1, path), path) : replaceRelationOrPathAt(
        relation, path, er);
  }

}