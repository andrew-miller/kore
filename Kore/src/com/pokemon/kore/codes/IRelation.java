package com.pokemon.kore.codes;

import static com.pokemon.kore.utils.Null.notNull;

import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public final class IRelation {
  public enum Tag {
    UNION, PRODUCT, PROJECTION, ABSTRACTION, COMPOSITION, LABEL
  }

  public final Tag tag;
  private final Union union;
  private final Product product;
  private final Projection projection;
  private final Abstraction abstraction;
  private final Composition composition;
  private final Label_ label;
  /**
   * <code>(r, p)</code> where <code>r</code> is the root of the SCC containing
   * this relation and <code>p</code> is the path from <code>r</code> to this
   * relation
   */
  public final Pair<Relation2, List<Either3<Label, Integer, Unit>>> link;

  private IRelation(Tag tag, Union union, Product product,
      Projection projection, Abstraction abstraction, Composition composition,
      Label_ label, Pair<Relation2, List<Either3<Label, Integer, Unit>>> link) {
    this.tag = tag;
    this.union = union;
    this.product = product;
    this.projection = projection;
    this.abstraction = abstraction;
    this.composition = composition;
    this.label = label;
    this.link = link;
  }

  public final static class Union {
    public final F<Unit, List<Either<IRelation, List<Either3<Label, Integer, Unit>>>>> l;
    public final Code2 i;
    public final Code2 o;

    public Union(
        F<Unit, List<Either<IRelation, List<Either3<Label, Integer, Unit>>>>> l,
        Code2 i, Code2 o) {
      this.l = l;
      this.i = i;
      this.o = o;
    }
  }

  public final static class Product {
    public final F<Unit, Map<Label, Either<IRelation, List<Either3<Label, Integer, Unit>>>>> m;
    public final Code2 o;

    public Product(
        F<Unit, Map<Label, Either<IRelation, List<Either3<Label, Integer, Unit>>>>> m,
        Code2 o) {
      this.m = m;
      this.o = o;
    }
  }

  public final static class Projection {
    public final List<Label> path;
    public final Code2 o;

    public Projection(List<Label> path, Code2 o) {
      this.path = path;
      this.o = o;
    }
  }

  public final static class Abstraction {
    public final Pattern pattern;
    public final F<Unit, Either<IRelation, List<Either3<Label, Integer, Unit>>>> r;
    public final Code2 i;
    public final Code2 o;

    public Abstraction(Pattern pattern,
        F<Unit, Either<IRelation, List<Either3<Label, Integer, Unit>>>> r,
        Code2 i, Code2 o) {
      this.pattern = pattern;
      this.r = r;
      this.i = i;
      this.o = o;
    }
  }

  public final static class Composition {
    public final F<Unit, List<Either<IRelation, List<Either3<Label, Integer, Unit>>>>> l;
    public final Code2 i;
    public final Code2 o;

    public Composition(
        F<Unit, List<Either<IRelation, List<Either3<Label, Integer, Unit>>>>> l,
        Code2 i, Code2 o) {
      this.l = l;
      this.i = i;
      this.o = o;
    }
  }

  public final static class Label_ {
    public final Label label;
    public final Code2 o;
    public final F<Unit, Either<IRelation, List<Either3<Label, Integer, Unit>>>> r;

    public Label_(Label label,
        F<Unit, Either<IRelation, List<Either3<Label, Integer, Unit>>>> r,
        Code2 o) {
      this.label = label;
      this.r = r;
      this.o = o;
    }
  }

  public static IRelation union(Union u,
      Pair<Relation2, List<Either3<Label, Integer, Unit>>> link) {
    notNull(u);
    return new IRelation(Tag.UNION, u, null, null, null, null, null, link);
  }

  public static IRelation product(Product p,
      Pair<Relation2, List<Either3<Label, Integer, Unit>>> link) {
    notNull(p);
    return new IRelation(Tag.PRODUCT, null, p, null, null, null, null, link);
  }

  public static IRelation projection(Projection p,
      Pair<Relation2, List<Either3<Label, Integer, Unit>>> link) {
    notNull(p);
    return new IRelation(Tag.PROJECTION, null, null, p, null, null, null, link);
  }

  public static IRelation abstraction(Abstraction a,
      Pair<Relation2, List<Either3<Label, Integer, Unit>>> link) {
    notNull(a);
    return new IRelation(Tag.ABSTRACTION, null, null, null, a, null, null, link);
  }

  public static IRelation composition(Composition c,
      Pair<Relation2, List<Either3<Label, Integer, Unit>>> link) {
    notNull(c);
    return new IRelation(Tag.COMPOSITION, null, null, null, null, c, null, link);
  }

  public static IRelation label(Label_ l,
      Pair<Relation2, List<Either3<Label, Integer, Unit>>> link) {
    notNull(l);
    return new IRelation(Tag.LABEL, null, null, null, null, null, l, link);
  }

  public Union union() {
    if (union == null)
      throw new RuntimeException("not union");
    return union;
  }

  public Product product() {
    if (product == null)
      throw new RuntimeException("not product");
    return product;
  }

  public Projection projection() {
    if (projection == null)
      throw new RuntimeException("not projection");
    return projection;
  }

  public Abstraction abstraction() {
    if (abstraction == null)
      throw new RuntimeException("not abstraction");
    return abstraction;
  }

  public Composition composition() {
    if (composition == null)
      throw new RuntimeException("not composition");
    return composition;
  }

  public Label_ label() {
    if (label == null)
      throw new RuntimeException("not label");
    return label;
  }
}
