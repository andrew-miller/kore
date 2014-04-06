package com.example.kore.ui;

import com.example.kore.utils.UnitComparer;
import static com.example.kore.ui.PatternUtils.emptyPattern;
import static com.example.kore.ui.PatternUtils.renderPattern;
import static com.example.kore.utils.CodeUtils.codeToGraph;
import static com.example.kore.utils.CodeUtils.directPath;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.drop;
import static com.example.kore.utils.ListUtils.insert;
import static com.example.kore.utils.ListUtils.isSubList;
import static com.example.kore.utils.ListUtils.length;
import static com.example.kore.utils.ListUtils.map;
import static com.example.kore.utils.ListUtils.nth;
import static com.example.kore.utils.ListUtils.replace;
import static com.example.kore.utils.MapUtils.map;
import static com.example.kore.utils.OptionalUtils.nothing;
import static com.example.kore.utils.OptionalUtils.some;

import java.util.HashSet;
import java.util.Set;

import org.jgrapht.graph.DirectedMultigraph;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.codes.RVertex;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Abstraction;
import com.example.kore.codes.Relation.Composition;
import com.example.kore.codes.Relation.Label_;
import com.example.kore.codes.Relation.Product;
import com.example.kore.codes.Relation.Projection;
import com.example.kore.codes.Relation.Union;
import static com.example.kore.utils.Boom.boom;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.Either3.Tag;
import com.example.kore.utils.Either3Comparer;
import com.example.kore.utils.F;
import com.example.kore.utils.Identity;
import com.example.kore.utils.IntegerComparer;
import com.example.kore.utils.LabelComparer;
import static com.example.kore.utils.LinkTreeUtils.canonicalLinkTree;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.fromArray;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;
import static com.example.kore.utils.MapUtils.containsKey;
import com.example.kore.utils.Optional;
import com.example.kore.utils.OptionalUtils;
import com.example.kore.utils.Pair;
import static com.example.kore.utils.Pair.pair;
import com.example.kore.utils.Unit;
import static com.example.kore.utils.Unit.unit;
import java.util.SortedMap;
import java.util.TreeMap;

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

  public static boolean inAbstraction(Relation relation,
      List<Either3<Label, Integer, Unit>> path) {
    if (path.isEmpty())
      return false;
    if (relation.tag == Relation.Tag.ABSTRACTION)
      return true;
    Either<Relation, List<Either3<Label, Integer, Unit>>> zz =
        subRelationOrPath(relation, path.cons().x).some().x;
    if (zz.isY())
      return false;
    return inAbstraction(zz.x(), path.cons().tail);
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
   * Insert transformations between elements of the composition that are not
   * compatible. Also pads the start and end of the composition if the first
   * element's domain is not the same as the composition's domain, or the last
   * element's codomain is not the same as the composition's codomain,
   * respectively.
   */
  public static Relation adaptComposition(Relation root,
      final List<Either3<Label, Integer, Unit>> path) {
    Composition c = relationAt(path, root).some().x.composition();
    final Pair<List<Boolean>, List<Either<Relation, List<Either3<Label, Integer, Unit>>>>> p =
        adaptComposition_(root, c.i, c.l);
    Either<Relation, List<Either3<Label, Integer, Unit>>> elast =
        nth(c.l, length(c.l) - 1).some().x;
    Code last =
        codomain(elast.isY() ? relationAt(elast.y(), root).some().x : elast.x());
    return mapPaths(
        replaceRelationOrPathAt(root, path,
            x(Relation.composition(new Composition(equal(last, c.o) ? p.y
                : append(x(dummy(last, c.o)), c.l), c.i, c.o)))),
        new F<List<Either3<Label, Integer, Unit>>, List<Either3<Label, Integer, Unit>>>() {
          public List<Either3<Label, Integer, Unit>> f(
              List<Either3<Label, Integer, Unit>> p2) {
            if (isSubList(path, p2)) {
              List<Either3<Label, Integer, Unit>> l = drop(p2, length(path));
              if (!l.isEmpty()) {
                Integer o = 0;
                Integer i = 0;
                for (Boolean b_ : iter(p.x)) {
                  if (i++ > l.cons().x.y())
                    break;
                  if (b_)
                    o++;
                }
                return append(
                    path,
                    cons(Either3.<Label, Integer, Unit> y(l.cons().x.y() + o),
                        l.cons().tail));
              }
            }
            return p2;
          }
        });
  }

  private static
      Pair<List<Boolean>, List<Either<Relation, List<Either3<Label, Integer, Unit>>>>>
      adaptComposition_(Relation root, Code c,
          List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l) {
    if (l.isEmpty())
      return pair(ListUtils.<Boolean> nil(), l);
    Either<Relation, List<Either3<Label, Integer, Unit>>> er = l.cons().x;
    Relation r = er.isY() ? relationAt(er.y(), root).some().x : er.x();
    Pair<List<Boolean>, List<Either<Relation, List<Either3<Label, Integer, Unit>>>>> p =
        adaptComposition_(root, codomain(r), l.cons().tail);
    return equal(c, domain(r)) ? pair(cons(false, p.x), cons(er, p.y)) : pair(
        cons(true, p.x), cons(x(dummy(c, domain(r))), cons(er, p.y)));
  }

  /** Empty relation from <tt>i</tt> to <tt>o</tt> */
  public static Relation dummy(Code i, Code o) {
    return Relation.union(new Union(ListUtils
        .<Either<Relation, List<Either3<Label, Integer, Unit>>>> nil(), i, o));
  }

  private static Either<Relation, List<Either3<Label, Integer, Unit>>> x(
      Relation r) {
    return Either.x(r);
  }

  private static Either<Relation, List<Either3<Label, Integer, Unit>>> y(
      List<Either3<Label, Integer, Unit>> x) {
    return Either.y(x);
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
    return linkTreeToRelation(canonicalLinkTree(linkTree(r), path,
        new Either3Comparer<Label, Integer, Unit>(
            new LabelComparer(), new IntegerComparer(), new UnitComparer())));
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

  public static
      Relation
      mapPaths(
          Relation r,
          final F<List<Either3<Label, Integer, Unit>>, List<Either3<Label, Integer, Unit>>> f) {
    F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Either<Relation, List<Either3<Label, Integer, Unit>>>> ff =
        new F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Either<Relation, List<Either3<Label, Integer, Unit>>>>() {
          public Either<Relation, List<Either3<Label, Integer, Unit>>> f(
              Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
            return mapPaths_(f, er);
          }
        };
    switch (r.tag) {
    case ABSTRACTION:
      Abstraction a = r.abstraction();
      return Relation.abstraction(new Relation.Abstraction(a.pattern,
          mapPaths_(f, a.r), a.i, a.o));
    case COMPOSITION:
      Composition c = r.composition();
      return Relation.composition(new Composition(map(ff, c.l), c.i, c.o));
    case LABEL:
      Label_ l = r.label();
      return Relation.label(new Label_(l.label, mapPaths_(f, l.r), l.o));
    case PRODUCT:
      Product p = r.product();
      return Relation.product(new Product(map(ff, p.m), p.o));
    case PROJECTION:
      return r;
    case UNION:
      Union u = r.union();
      return Relation.union(new Union(map(ff, u.l), u.i, u.o));
    default:
      throw boom();
    }
  }

  private static
      Either<Relation, List<Either3<Label, Integer, Unit>>>
      mapPaths_(
          final F<List<Either3<Label, Integer, Unit>>, List<Either3<Label, Integer, Unit>>> f,
          Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
    return er.isY() ? y(f.f(er.y())) : x(mapPaths(er.x(), f));
  }

  public static Relation extendComposition(Relation relation,
      List<Either3<Label, Integer, Unit>> path, final Integer i, Relation r2) {
    if (i < 0)
      throw new RuntimeException("index can't be negative");
    Either<Relation, List<Either3<Label, Integer, Unit>>> er =
        relationOrPathAt(path, relation);
    Relation r1 = er.isY() ? relationAt(er.y(), relation).some().x : er.x();
    Code d = domain(r1);
    Code c = codomain(r1);

    Composition comp;
    if (!er.isY() && er.x().tag == Relation.Tag.COMPOSITION) {
      comp = new Composition(insert(er.x().composition().l, i, x(r2)), d, c);
      return adaptComposition(
        bumpIndexes(
          replaceRelationOrPathAt(
              relation, path, x(Relation.composition(comp))),
          i, path),
        path);
    } else {
      switch (i) {
      case 0:
        comp = new Composition(fromArray(x(r2), er), d, c);
        break;
      case 1:
        comp = new Composition(fromArray(er, x(r2)), d, c);
        break;
      default:
        throw new RuntimeException("invalid index");
      }
      return adaptComposition(
        insertIndexes(
          replaceRelationOrPathAt(
              relation, path, x(Relation.composition(comp))),
          i, path),
        path);
    }
  }

  public static Relation extendUnion(Relation relation,
      List<Either3<Label, Integer, Unit>> path, final Integer i, Relation r2) {
    if (i < 0)
      throw new RuntimeException("index can't be negative");
    Either<Relation, List<Either3<Label, Integer, Unit>>> er =
        relationOrPathAt(path, relation);
    Relation r1 = er.isY() ? relationAt(er.y(), relation).some().x : er.x();
    Code d = domain(r1);
    Code c = codomain(r1);
    Code d2 = domain(r2);
    Code c2 = codomain(r2);
    if (!equal(d, d2)) {
      Relation t = dummy(d, d2);
      if (r2.tag == Relation.Tag.COMPOSITION)
        r2 =
            Relation.composition(new Composition(
                cons(x(t), r2.composition().l), d, c2));
      else
        r2 =
            Relation
                .composition(new Composition(fromArray(x(t), x(r2)), d, c2));
    }
    if (!equal(c, c2)) {
      Relation t = dummy(c2, c);
      if (r2.tag == Relation.Tag.COMPOSITION)
        r2 =
            Relation.composition(new Composition(append(x(t),
                r2.composition().l), d, c));
      else
        r2 =
            Relation.composition(new Composition(fromArray(x(r2), x(t)), d, c));
    }
    Union union;
    if (!er.isY() && er.x().tag == Relation.Tag.UNION) {
      union =
          new Union(insert(er.x().union().l, i, x(r2)), er.x().union().i, er
              .x().union().o);
      return bumpIndexes(
          replaceRelationOrPathAt(relation,path,x(Relation.union(union))),
          i, path);
    } else {
      switch (i) {
      case 0:
        union = new Union(fromArray(x(r2), er), d, c);
        break;
      case 1:
        union = new Union(fromArray(er, x(r2)), d, c);
        break;
      default:
        throw new RuntimeException("invalid index");
      }
      return insertIndexes(
          replaceRelationOrPathAt(relation,path,x(Relation.union(union))),
          i, path);
    }
  }

  private static Relation bumpIndexes(Relation r, final Integer i,
      final List<Either3<Label, Integer, Unit>> path) {
    return mapPaths(
        r,
        new F<List<Either3<Label, Integer, Unit>>, List<Either3<Label, Integer, Unit>>>() {
          public List<Either3<Label, Integer, Unit>> f(
              List<Either3<Label, Integer, Unit>> p) {
            if (isSubList(path, p)) {
              List<Either3<Label, Integer, Unit>> l = drop(p, length(path));
              if (!l.isEmpty() && l.cons().x.y() >= i)
                return append(
                    path,
                    cons(Either3.<Label, Integer, Unit> y(l.cons().x.y() + 1),
                        l.cons().tail));
            }
            return p;
          }
        });
  }

  private static Relation insertIndexes(Relation r, final Integer i,
      final List<Either3<Label, Integer, Unit>> path) {
    return mapPaths(
        r,
        new F<List<Either3<Label, Integer, Unit>>, List<Either3<Label, Integer, Unit>>>() {
          public List<Either3<Label, Integer, Unit>> f(
              List<Either3<Label, Integer, Unit>> p) {
            if (isSubList(path, p)) {
              List<Either3<Label, Integer, Unit>> l = drop(p, length(path));
                return append(path,
                    cons(Either3.<Label, Integer, Unit> y((i + 1) % 2), l));
            }
            return p;
          }
        });
  }

  public static Relation changeRelationType(Relation relation,
      List<Either3<Label, Integer, Unit>> path, Relation.Tag t) {
    Either<Relation, List<Either3<Label, Integer, Unit>>> rp =
        relationOrPathAt(path, relation);
    Relation r = rp.isY() ? relationAt(rp.y(), relation).some().x : rp.x();
    Code d = domain(r);
    Code c = codomain(r);
    Relation r2;
    switch (t) {
    case ABSTRACTION:
      r2 =
          Relation.abstraction(new Abstraction(emptyPattern, x(dummy(unit, c)),
              d, c));
      break;
    case COMPOSITION:
      r2 = Relation.composition(new Composition(nil, d, c));
      break;
    case PRODUCT:
      if (!equal(d, unit))
        throw new RuntimeException("cannot make product with non-unit domain");
      Map<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> m =
          Map.empty();
      for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet()))
        m = m.put(e.k, x(dummy(unit, reroot(c, fromArray(e.k)))));
      r2 = Relation.product(new Product(m, c));
      break;
    case LABEL:
      if (!equal(d, unit))
        throw new RuntimeException("cannot make label with non-unit domain");
      notEmpty: {
        for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet())) {
          r2 =
              Relation.label(new Label_(e.k, x(dummy(unit,
                  reroot(c, fromArray(e.k)))), c));
          break notEmpty;
        }
        r2 = dummy(d, c);
      }
      break;
    case PROJECTION:
      Optional<Abstraction> oa = enclosingAbstraction(path, relation);
      if (oa.isNothing())
        throw new RuntimeException(
            "attempt to make projection that isn't contained within an abstraction");
      Code arg = oa.some().x.i;
      Optional<Projection> or2 = projection(arg, c);
      r2 = or2.isNothing() ? dummy(d, c) : Relation.projection(or2.some().x);
      break;
    case UNION:
      r2 = dummy(d, c);
      break;
    default:
      throw boom();
    }
    return replaceRelationOrPathAt(relation, path, x(r2));
  }

  private static List<Either<Relation, List<Either3<Label, Integer, Unit>>>> nil =
      nil();

  public static Relation changeCodomain(Relation relation,
      List<Either3<Label, Integer, Unit>> path, Code c2) {
    Relation r = relationAt(path, relation).some().x;
    Composition comp;
    Code d = domain(r);
    Code c = codomain(r);
    Relation t = dummy(c, c2);
    switch (r.tag) {
    case COMPOSITION:
      comp = new Composition(append(x(t), r.composition().l), d, c2);
      break;
    case ABSTRACTION:
    case LABEL:
    case PRODUCT:
    case PROJECTION:
    case UNION:
      comp = new Composition(cons(x(r), cons(x(t), nil)), d, c2);
      break;
    default:
      throw boom();
    }
    return replaceRelationOrPathAt(relation, path,
        x(Relation.composition(comp)));
  }

  public static Relation changeDomain(Relation relation,
      List<Either3<Label, Integer, Unit>> path, Code d2) {
    Relation r = relationAt(path, relation).some().x;
    Composition comp;
    Code d = domain(r);
    Code c = codomain(r);
    Relation t = dummy(d2, d);
    switch (r.tag) {
    case COMPOSITION:
      comp = new Composition(cons(x(t), r.composition().l), d2, c);
      break;
    case ABSTRACTION:
    case LABEL:
    case PRODUCT:
    case PROJECTION:
    case UNION:
      comp = new Composition(cons(x(t), cons(x(r), nil)), d2, c);
      break;
    default:
      throw boom();
    }
    return replaceRelationOrPathAt(relation, path,
        x(Relation.composition(comp)));
  }

  public static LinkTree<Either3<Label, Integer, Unit>, RVertex> linkTree(final Relation r) {
    return new LinkTree<Either3<Label, Integer, Unit>, RVertex>() {
      public
          List<Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>>>
          edges() {
        switch (r.tag) {
        case ABSTRACTION:
          Abstraction a = r.abstraction();
          return fromArray(pair(Either3.<Label, Integer, Unit> z(unit()),
              f(a.r)));
        case COMPOSITION: {
          List<Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>>> l =
              nil();
          int i = 0;
          Composition c = r.composition();
          for (Either<Relation, List<Either3<Label, Integer, Unit>>> r : iter(c.l))
            l =
                cons(pair(Either3.<Label, Integer, Unit> y(i++), f(r)),
                    l);
          return l;
        }
        case LABEL:
          return fromArray(pair(
              Either3.<Label, Integer, Unit> z(unit()),
              f(r.label().r)));
        case PRODUCT: {
          List<Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>>> l =
              nil();
          for (Entry<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(r
              .product().m.entrySet()))
            l =
                cons(
                    pair(Either3.<Label, Integer, Unit> x(e.k), f(e.v)),
                    l);
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
            l =
                cons(pair(Either3.<Label, Integer, Unit> y(i++), f(r_)),
                    l);
          return l;
        default:
          throw boom();
        }
      }

      private
          Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>
          f(Either<Relation, List<Either3<Label, Integer, Unit>>> rp) {
        return rp.isY() ? Either
            .<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>> y(rp
                .y())
            : Either
                .<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>> x(linkTree(rp
                    .x()));
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
          return RVertex.projection(new RVertex.Projection(r
              .projection().path, r.projection().o));
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
        if (!lt.edges().cons().tail.isEmpty()) throw boom();
        lt.edges().cons().x.x.z();
        RVertex.Abstraction a = lt.vertex().abstraction();
        return Relation.abstraction(new Abstraction(
            a.pattern, f(lt.edges().cons().x.y), a.i, a.o));
      case COMPOSITION:
      case UNION: {
        SortedMap<
            Integer,
            Either<
              LinkTree<Either3<Label, Integer, Unit>, RVertex>,
              List<Either3<Label, Integer, Unit>>>> sm =
          new TreeMap<Integer,Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>,
                      List<Either3<Label, Integer, Unit>>>>();
        for (Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>,
          List<Either3<Label, Integer, Unit>>>> p : iter(lt.edges()))
          if (sm.put(p.x.y(), p.y) != null) throw new RuntimeException("duplicate index");
        List<Either<Relation, List<Either3<Label, Integer, Unit>>>> l = nil();
        int n = -1;
        for (java.util.Map.Entry<Integer, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>,
            List<Either3<Label, Integer, Unit>>>> e : sm.entrySet()) {
          if (n+++1 != e.getKey()) throw new RuntimeException("missing an index");
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
        if (!lt.edges().cons().tail.isEmpty()) throw boom();
        RVertex.Label l = lt.vertex().label();
        return Relation.label(new Label_(l.l, f(lt.edges().cons().x.y), l.o));
      case PRODUCT: {
        RVertex.Product p = lt.vertex().product();
        Map<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> m = Map.empty();
        for (Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>,
                  List<Either3<Label, Integer, Unit>>>> e : iter(lt.edges())) {
          if (containsKey(m,e.x.x())) throw new RuntimeException("duplicate label");
          m = m.put(e.x.x(), f(e.y));
        }
        return Relation.product(new Product(m, p.o));
      }
      case PROJECTION:
        if (!lt.edges().isEmpty()) throw boom();
        RVertex.Projection p = lt.vertex().projection();
        return Relation.projection(new Projection(p.path, p.o));
      default: throw boom();
    }
  }

  private static Either<Relation, List<Either3<Label, Integer, Unit>>>
      f(Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>,
               List<Either3<Label, Integer, Unit>>> e) {
    return e.isY()
        ? Either.<Relation, List<Either3<Label, Integer, Unit>>> y(e.y())
        : Either.<Relation, List<Either3<Label, Integer, Unit>>> x(linkTreeToRelation(e.x()));
  }

}