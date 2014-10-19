package com.pokemon.kore.codes;

import static com.pokemon.kore.utils.Null.notNull;

import java.io.Serializable;

import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Unit;

public final class Relation2 implements Serializable {
  public enum Tag {
    UNION, PRODUCT, PROJECTION, ABSTRACTION, COMPOSITION, LABEL
  }

  public static final class Link implements Serializable {
    public final З2Bytes hash;
    public final List<Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link>> path;

    public Link(З2Bytes hash,
        List<Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link>> path) {
      this.hash = hash;
      this.path = path;
    }

    @Override
    public String toString() {
      return "Link [hash=" + hash + ", path=" + path + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((hash == null) ? 0 : hash.hashCode());
      result = prime * result + ((path == null) ? 0 : path.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Link other = (Link) obj;
      if (hash == null) {
        if (other.hash != null)
          return false;
      } else if (!hash.equals(other.hash))
        return false;
      if (path == null) {
        if (other.path != null)
          return false;
      } else if (!path.equals(other.path))
        return false;
      return true;
    }
  }

  public final Tag tag;
  private final Union union;
  private final Product product;
  private final Projection projection;
  private final Abstraction abstraction;
  private final Composition composition;
  private final Label_ label;

  private Relation2(Tag tag, Union union, Product product,
      Projection projection, Abstraction abstraction, Composition composition,
      Label_ label) {
    this.tag = tag;
    this.union = union;
    this.product = product;
    this.projection = projection;
    this.abstraction = abstraction;
    this.composition = composition;
    this.label = label;
  }

  public final static class Union implements Serializable {
    public final List<Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link>> l;
    public final Code2.Link i;
    public final Code2.Link o;

    public Union(
        List<Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link>> l,
        Code2.Link i, Code2.Link o) {
      this.l = l;
      this.i = i;
      this.o = o;
    }

    @Override
    public String toString() {
      return "Union [l=" + l + ", i=" + i + ", o=" + o + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((i == null) ? 0 : i.hashCode());
      result = prime * result + ((l == null) ? 0 : l.hashCode());
      result = prime * result + ((o == null) ? 0 : o.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Union other = (Union) obj;
      if (i == null) {
        if (other.i != null)
          return false;
      } else if (!i.equals(other.i))
        return false;
      if (l == null) {
        if (other.l != null)
          return false;
      } else if (!l.equals(other.l))
        return false;
      if (o == null) {
        if (other.o != null)
          return false;
      } else if (!o.equals(other.o))
        return false;
      return true;
    }
  }

  public final static class Product implements Serializable {
    public final Map<Label, Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link>> m;
    public final Code2.Link o;

    public Product(
        Map<Label, Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link>> m,
        Code2.Link o) {
      this.m = m;
      this.o = o;
    }

    @Override
    public String toString() {
      return "Product [m=" + m + ", o=" + o + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((m == null) ? 0 : m.hashCode());
      result = prime * result + ((o == null) ? 0 : o.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Product other = (Product) obj;
      if (m == null) {
        if (other.m != null)
          return false;
      } else if (!m.equals(other.m))
        return false;
      if (o == null) {
        if (other.o != null)
          return false;
      } else if (!o.equals(other.o))
        return false;
      return true;
    }
  }

  public final static class Projection implements Serializable {
    public final List<Label> path;
    public final Code2.Link o;

    public Projection(List<Label> path, Code2.Link o) {
      this.path = path;
      this.o = o;
    }

    @Override
    public String toString() {
      return "Projection [path=" + path + ", o=" + o + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((o == null) ? 0 : o.hashCode());
      result = prime * result + ((path == null) ? 0 : path.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Projection other = (Projection) obj;
      if (o == null) {
        if (other.o != null)
          return false;
      } else if (!o.equals(other.o))
        return false;
      if (path == null) {
        if (other.path != null)
          return false;
      } else if (!path.equals(other.path))
        return false;
      return true;
    }
  }

  public final static class Abstraction implements Serializable {
    public final Pattern pattern;
    public final Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link> r;
    public final Code2.Link i;
    public final Code2.Link o;

    public Abstraction(Pattern pattern,
        Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link> r,
        Code2.Link i, Code2.Link o) {
      this.pattern = pattern;
      this.r = r;
      this.i = i;
      this.o = o;
    }

    @Override
    public String toString() {
      return "Abstraction [pattern=" + pattern + ", r=" + r + ", i=" + i
          + ", o=" + o + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((i == null) ? 0 : i.hashCode());
      result = prime * result + ((o == null) ? 0 : o.hashCode());
      result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
      result = prime * result + ((r == null) ? 0 : r.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Abstraction other = (Abstraction) obj;
      if (i == null) {
        if (other.i != null)
          return false;
      } else if (!i.equals(other.i))
        return false;
      if (o == null) {
        if (other.o != null)
          return false;
      } else if (!o.equals(other.o))
        return false;
      if (pattern == null) {
        if (other.pattern != null)
          return false;
      } else if (!pattern.equals(other.pattern))
        return false;
      if (r == null) {
        if (other.r != null)
          return false;
      } else if (!r.equals(other.r))
        return false;
      return true;
    }
  }

  public final static class Composition implements Serializable {
    public final List<Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link>> l;
    public final Code2.Link i;
    public final Code2.Link o;

    public Composition(
        List<Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link>> l,
        Code2.Link i, Code2.Link o) {
      this.l = l;
      this.i = i;
      this.o = o;
    }

    @Override
    public String toString() {
      return "Composition [l=" + l + ", i=" + i + ", o=" + o + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((i == null) ? 0 : i.hashCode());
      result = prime * result + ((l == null) ? 0 : l.hashCode());
      result = prime * result + ((o == null) ? 0 : o.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Composition other = (Composition) obj;
      if (i == null) {
        if (other.i != null)
          return false;
      } else if (!i.equals(other.i))
        return false;
      if (l == null) {
        if (other.l != null)
          return false;
      } else if (!l.equals(other.l))
        return false;
      if (o == null) {
        if (other.o != null)
          return false;
      } else if (!o.equals(other.o))
        return false;
      return true;
    }
  }

  public final static class Label_ implements Serializable {
    public final Label label;
    public final Code2.Link o;
    public final Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link> r;

    public Label_(Label label,
        Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link> r,
        Code2.Link o) {
      this.label = label;
      this.r = r;
      this.o = o;
    }

    @Override
    public String toString() {
      return "Label_ [label=" + label + ", o=" + o + ", r=" + r + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((label == null) ? 0 : label.hashCode());
      result = prime * result + ((o == null) ? 0 : o.hashCode());
      result = prime * result + ((r == null) ? 0 : r.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Label_ other = (Label_) obj;
      if (label == null) {
        if (other.label != null)
          return false;
      } else if (!label.equals(other.label))
        return false;
      if (o == null) {
        if (other.o != null)
          return false;
      } else if (!o.equals(other.o))
        return false;
      if (r == null) {
        if (other.r != null)
          return false;
      } else if (!r.equals(other.r))
        return false;
      return true;
    }
  }

  public static Relation2 union(Union u) {
    notNull(u);
    return new Relation2(Tag.UNION, u, null, null, null, null, null);
  }

  public static Relation2 product(Product p) {
    notNull(p);
    return new Relation2(Tag.PRODUCT, null, p, null, null, null, null);
  }

  public static Relation2 projection(Projection p) {
    notNull(p);
    return new Relation2(Tag.PROJECTION, null, null, p, null, null, null);
  }

  public static Relation2 abstraction(Abstraction a) {
    notNull(a);
    return new Relation2(Tag.ABSTRACTION, null, null, null, a, null, null);
  }

  public static Relation2 composition(Composition c) {
    notNull(c);
    return new Relation2(Tag.COMPOSITION, null, null, null, null, c, null);
  }

  public static Relation2 label(Label_ l) {
    notNull(l);
    return new Relation2(Tag.LABEL, null, null, null, null, null, l);
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

  @Override
  public String toString() {
    return "Relation2 [tag=" + tag + ", union=" + union + ", product="
        + product + ", projection=" + projection + ", abstraction="
        + abstraction + ", composition=" + composition + ", label=" + label
        + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((abstraction == null) ? 0 : abstraction.hashCode());
    result =
        prime * result + ((composition == null) ? 0 : composition.hashCode());
    result = prime * result + ((label == null) ? 0 : label.hashCode());
    result = prime * result + ((product == null) ? 0 : product.hashCode());
    result =
        prime * result + ((projection == null) ? 0 : projection.hashCode());
    result = prime * result + ((tag == null) ? 0 : tag.hashCode());
    result = prime * result + ((union == null) ? 0 : union.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Relation2 other = (Relation2) obj;
    if (abstraction == null) {
      if (other.abstraction != null)
        return false;
    } else if (!abstraction.equals(other.abstraction))
      return false;
    if (composition == null) {
      if (other.composition != null)
        return false;
    } else if (!composition.equals(other.composition))
      return false;
    if (label == null) {
      if (other.label != null)
        return false;
    } else if (!label.equals(other.label))
      return false;
    if (product == null) {
      if (other.product != null)
        return false;
    } else if (!product.equals(other.product))
      return false;
    if (projection == null) {
      if (other.projection != null)
        return false;
    } else if (!projection.equals(other.projection))
      return false;
    if (tag != other.tag)
      return false;
    if (union == null) {
      if (other.union != null)
        return false;
    } else if (!union.equals(other.union))
      return false;
    return true;
  }

}
