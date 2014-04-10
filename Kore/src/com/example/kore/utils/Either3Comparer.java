package com.example.kore.utils;

import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.Comparison.EQ;

import com.example.kore.codes.EnumComparer;

public class Either3Comparer<X, Y, Z> implements Comparer<Either3<X, Y, Z>> {
  private final Comparer<X> xc;
  private final Comparer<Y> yc;
  private final Comparer<Z> zc;

  public Either3Comparer(Comparer<X> xc, Comparer<Y> yc, Comparer<Z> zc) {
    this.xc = xc;
    this.yc = yc;
    this.zc = zc;
  }

  public Comparison compare(Either3<X, Y, Z> a, Either3<X, Y, Z> b) {
    Comparison c = new EnumComparer<Either3.Tag>().compare(a.tag, b.tag);
    if (c != EQ)
      return c;
    switch (a.tag) {
    case X:
      return xc.compare(a.x(), b.x());
    case Y:
      return yc.compare(a.y(), b.y());
    case Z:
      return zc.compare(a.z(), b.z());
    default:
      throw boom();
    }
  }
}