package com.example.kore.codes;

import static com.example.kore.utils.Boom.boom;
import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;

public class RVertexComparer implements Comparer<RVertex> {
  public Comparison compare(RVertex a, RVertex b) {
    Comparison x = new EnumComparer<RVertex.Tag>().compare(a.tag, b.tag);
    if (x != Comparison.EQ)
      return x;
    switch (a.tag) {
    case UNION:
      return new RVUnionComparer().compare(a.union(), b.union());
    case ABSTRACTION:
      return new RVAbstractionComparer().compare(a.abstraction(),
          b.abstraction());
    case COMPOSITION:
      return new RVCompositionComparer().compare(a.composition(),
          b.composition());
    case LABEL:
      return new RVLabelComparer().compare(a.label(), b.label());
    case PRODUCT:
      return new RVProductComparer().compare(a.product(), b.product());
    case PROJECTION:
      return new RVProjectionComparer().compare(a.projection(), b.projection());
    default:
      throw boom();
    }
  }
}