package com.pokemon.kore.codes;

import static com.pokemon.kore.utils.Null.notNull;

import java.io.Serializable;

import com.pokemon.kore.codes.Code2.Link;
import com.pokemon.kore.utils.List;

/** A vertex of a relation without the subrelations */
public final class RVertex2 implements Serializable {
  public enum Tag {
    UNION, PRODUCT, PROJECTION, ABSTRACTION, COMPOSITION, LABEL
  }

  public final Tag tag;
  private final Union union;
  private final Product product;
  private final Projection projection;
  private final Abstraction abstraction;
  private final Composition composition;
  private final Label label;

  private RVertex2(Tag tag, Union union, Product product,
      Projection projection, Abstraction abstraction, Composition composition,
      Label label) {
    this.tag = tag;
    this.union = union;
    this.product = product;
    this.projection = projection;
    this.abstraction = abstraction;
    this.composition = composition;
    this.label = label;
  }

  public final static class Union implements Serializable {
    public final Link i;
    public final Link o;

    public Union(Link i, Link o) {
      this.i = i;
      this.o = o;
    }

    @Override
    public String toString() {
      return "Union [o=" + o + ", i=" + i + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((i == null) ? 0 : i.hashCode());
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
      if (o == null) {
        if (other.o != null)
          return false;
      } else if (!o.equals(other.o))
        return false;
      return true;
    }
  }

  public final static class Product implements Serializable {
    public final Link o;

    public Product(Link o) {
      this.o = o;
    }

    @Override
    public String toString() {
      return "Product [o=" + o + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
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
      if (o == null) {
        if (other.o != null)
          return false;
      } else if (!o.equals(other.o))
        return false;
      return true;
    }
  }

  public final static class Projection implements Serializable {
    public final List<com.pokemon.kore.codes.Label> path;
    public final Link o;

    public Projection(List<com.pokemon.kore.codes.Label> path, Link o) {
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
    public final Link i;
    public final Link o;

    public Abstraction(Pattern pattern, Link i, Link o) {
      this.pattern = pattern;
      this.i = i;
      this.o = o;
    }

    @Override
    public String toString() {
      return "Abstraction [pattern=" + pattern + ", i=" + i + ", o=" + o + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((i == null) ? 0 : i.hashCode());
      result = prime * result + ((o == null) ? 0 : o.hashCode());
      result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
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
      return true;
    }
  }

  public final static class Composition implements Serializable {
    public final Link i;
    public final Link o;

    public Composition(Link i, Link o) {
      this.i = i;
      this.o = o;
    }

    @Override
    public String toString() {
      return "Composition [i=" + i + ", o=" + o + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((i == null) ? 0 : i.hashCode());
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
      if (o == null) {
        if (other.o != null)
          return false;
      } else if (!o.equals(other.o))
        return false;
      return true;
    }
  }

  public final static class Label implements Serializable {
    public final com.pokemon.kore.codes.Label l;
    public final Link o;

    public Label(com.pokemon.kore.codes.Label l, Link o) {
      this.l = l;
      this.o = o;
    }

    @Override
    public String toString() {
      return "Label [l=" + l + ", o=" + o + "]";
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
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
      Label other = (Label) obj;
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

  public static RVertex2 union(Union u) {
    notNull(u);
    return new RVertex2(Tag.UNION, u, null, null, null, null, null);
  }

  public static RVertex2 product(Product p) {
    notNull(p);
    return new RVertex2(Tag.PRODUCT, null, p, null, null, null, null);
  }

  public static RVertex2 projection(Projection p) {
    notNull(p);
    return new RVertex2(Tag.PROJECTION, null, null, p, null, null, null);
  }

  public static RVertex2 abstraction(Abstraction a) {
    notNull(a);
    return new RVertex2(Tag.ABSTRACTION, null, null, null, a, null, null);
  }

  public static RVertex2 composition(Composition c) {
    notNull(c);
    return new RVertex2(Tag.COMPOSITION, null, null, null, null, c, null);
  }

  public static RVertex2 label(Label l) {
    notNull(l);
    return new RVertex2(Tag.LABEL, null, null, null, null, null, l);
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

  public Label label() {
    if (label == null)
      throw new RuntimeException("not label");
    return label;
  }

  @Override
  public String toString() {
    return "RVertex [tag=" + tag + ", union=" + union + ", product=" + product
        + ", projection=" + projection + ", abstraction=" + abstraction
        + ", composition=" + composition + ", label=" + label + "]";
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
    RVertex2 other = (RVertex2) obj;
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
