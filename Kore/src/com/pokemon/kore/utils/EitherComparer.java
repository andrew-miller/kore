package com.pokemon.kore.utils;

import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.Comparison.EQ;

import com.pokemon.kore.codes.EnumComparer;

public class EitherComparer<X, Y> implements Comparer<Either<X, Y>> {
  private final Comparer<X> xc;
  private final Comparer<Y> yc;

  public EitherComparer(Comparer<X> xc, Comparer<Y> yc) {
    this.xc = xc;
    this.yc = yc;
  }

  public Comparison compare(Either<X, Y> a, Either<X, Y> b) {
    Comparison c = new EnumComparer<Either.Tag>().compare(a.tag, b.tag);
    if (c != EQ)
      return c;
    switch (a.tag) {
    case X:
      return xc.compare(a.x(), b.x());
    case Y:
      return yc.compare(a.y(), b.y());
    default:
      throw boom();
    }
  }
}
