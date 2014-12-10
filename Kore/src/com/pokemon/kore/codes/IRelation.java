package com.pokemon.kore.codes;

import static com.pokemon.kore.utils.Null.notNull;

import com.pokemon.kore.codes.Relation2.Tag;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.ICode;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public final class IRelation {
  public static final class IR {

    public final Tag tag;
    private final Union union;
    private final Product product;
    private final Projection projection;
    private final Abstraction abstraction;
    private final Composition composition;
    private final Label_ label;

    private IR(Tag tag, Union union, Product product, Projection projection,
        Abstraction abstraction, Composition composition, Label_ label) {
      this.tag = tag;
      this.union = union;
      this.product = product;
      this.projection = projection;
      this.abstraction = abstraction;
      this.composition = composition;
      this.label = label;
    }

    public final static class Union {
      public final F<Unit, List<Either<IRelation, List<Either3<Label, Integer, Unit>>>>> l;
      public final ICode i;
      public final ICode o;

      public Union(
          F<Unit, List<Either<IRelation, List<Either3<Label, Integer, Unit>>>>> l,
          ICode i, ICode o) {
        this.l = l;
        this.i = i;
        this.o = o;
      }
    }

    public final static class Product {
      public final F<Unit, Map<Label, Either<IRelation, List<Either3<Label, Integer, Unit>>>>> m;
      public final ICode o;

      public Product(
          F<Unit, Map<Label, Either<IRelation, List<Either3<Label, Integer, Unit>>>>> m,
          ICode o) {
        this.m = m;
        this.o = o;
      }
    }

    public final static class Projection {
      public final List<Label> path;
      public final ICode o;

      public Projection(List<Label> path, ICode o) {
        this.path = path;
        this.o = o;
      }
    }

    public final static class Abstraction {
      public final Pattern pattern;
      public final F<Unit, Either<IRelation, List<Either3<Label, Integer, Unit>>>> r;
      public final ICode i;
      public final ICode o;

      public Abstraction(Pattern pattern,
          F<Unit, Either<IRelation, List<Either3<Label, Integer, Unit>>>> r,
          ICode i, ICode o) {
        this.pattern = pattern;
        this.r = r;
        this.i = i;
        this.o = o;
      }
    }

    public final static class Composition {
      public final F<Unit, List<Either<IRelation, List<Either3<Label, Integer, Unit>>>>> l;
      public final ICode i;
      public final ICode o;

      public Composition(
          F<Unit, List<Either<IRelation, List<Either3<Label, Integer, Unit>>>>> l,
          ICode i, ICode o) {
        this.l = l;
        this.i = i;
        this.o = o;
      }
    }

    public final static class Label_ {
      public final Label label;
      public final ICode o;
      public final F<Unit, Either<IRelation, List<Either3<Label, Integer, Unit>>>> r;

      public Label_(Label label, ICode o,
          F<Unit, Either<IRelation, List<Either3<Label, Integer, Unit>>>> r) {
        this.label = label;
        this.o = o;
        this.r = r;
      }
    }

    public static IR union(Union u) {
      notNull(u);
      return new IR(Tag.UNION, u, null, null, null, null, null);
    }

    public static IR product(Product p) {
      notNull(p);
      return new IR(Tag.PRODUCT, null, p, null, null, null, null);
    }

    public static IR projection(Projection p) {
      notNull(p);
      return new IR(Tag.PROJECTION, null, null, p, null, null, null);
    }

    public static IR abstraction(Abstraction a) {
      notNull(a);
      return new IR(Tag.ABSTRACTION, null, null, null, a, null, null);
    }

    public static IR composition(Composition c) {
      notNull(c);
      return new IR(Tag.COMPOSITION, null, null, null, null, c, null);
    }

    public static IR label(Label_ l) {
      notNull(l);
      return new IR(Tag.LABEL, null, null, null, null, null, l);
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

  /**
   * <code>(r, p)</code> where <code>r</code> is the root of the SCC containing
   * this relation and <code>p</code> is the path from <code>r</code> to this
   * relation
   */
  public final Pair<Relation2, List<Either3<Label, Integer, Unit>>> link;
  /**
   * The relation within this SCC at then end of the path starting from the root
   * of this SCC
   */
  public final F<List<Either3<Label, Integer, Unit>>, IRelation> relationAt;
  public final IR ir;

  public IRelation(Pair<Relation2, List<Either3<Label, Integer, Unit>>> link,
      F<List<Either3<Label, Integer, Unit>>, IRelation> relationAt, IR ir) {
    notNull(link, relationAt, ir);
    this.link = link;
    this.relationAt = relationAt;
    this.ir = ir;
  }

}
