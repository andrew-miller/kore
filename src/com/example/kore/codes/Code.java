package com.example.kore.codes;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.example.kore.utils.Helpers;
import com.example.unsuck.Boom;
import com.example.unsuck.Null;
import com.example.unsuck.Variant;

public final class Code implements Serializable {
  private static final long serialVersionUID = 1L;

  public enum Tag {
    UNION, PRODUCT
  }

  public final Tag tag;
  private final Union union;
  private final Product product;

  private Code(Tag tag, Union union, Product product) {
    this.tag = tag;
    this.union = union;
    this.product = product;
  }

  public final static class Union {
    public final Map<Label, Code> labels;

    private Union(Map<Label, Code> labels) {
      Null.notNull(labels);
      this.labels = Collections.unmodifiableMap(labels);
    }

    @Override
    public String toString() {
      return "[" + Helpers.labelMap(labels) + "]";
    }

  }

  public final static Code newUnion(Map<Label, Code> labels) {
    return new Code(Tag.UNION, new Union(labels), null);
  }

  public Union getUnion() {
    return Variant.get(Tag.UNION, tag, union);
  }

  public final static class Product {
    public final Map<Label, Code> labels;

    private Product(Map<Label, Code> labels) {
      Null.notNull(labels);
      this.labels = Collections.unmodifiableMap(labels);
    }

    @Override
    public String toString() {
      return "{" + Helpers.labelMap(labels) + "}";
    }

  }

  public Product getProduct() {
    return Variant.get(Tag.PRODUCT, tag, product);
  }

  public static Code newProduct(Map<Label, Code> labels) {
    return new Code(Tag.PRODUCT, null, new Product(labels));
  }

  @Override
  public String toString() {
    switch (tag) {
    case PRODUCT:
      return getProduct().toString();
    case UNION:
      return getUnion().toString();
    default:
      throw Boom.boom();
    }
  }

}
