package com.example.kore.ui;

import static com.example.kore.utils.OptionalUtils.nothing;
import static com.example.kore.utils.OptionalUtils.some;

import java.io.Serializable;

import com.example.kore.utils.Map;
import com.example.kore.utils.Optional;

public class Bijection<X, Y> implements Serializable {
  public final Map<X, Y> xy;
  public final Map<Y, X> yx;

  private Bijection(Map<X, Y> xy, Map<Y, X> yx) {
    this.xy = xy;
    this.yx = yx;
  }

  public Optional<Bijection<X, Y>> putX(X x, Y y) {
    if (!yx.get(y).isNothing())
      return nothing();
    Optional<Y> oy = xy.get(x);
    return some(new Bijection<X, Y>(xy.put(x, y), (oy.isNothing() ? yx
        : yx.delete(oy.some().x)).put(y, x)));
  }

  public Optional<Bijection<X, Y>> putY(Y y, X x) {
    if (!xy.get(x).isNothing())
      return nothing();
    Optional<X> ox = yx.get(y);
    return some(new Bijection<X, Y>((ox.isNothing() ? xy
        : xy.delete(ox.some().x)).put(x, y), yx.put(y, x)));
  }

  public Bijection<X, Y> deleteX(X x) {
    Map<X, Y> xy2 = xy.delete(x);
    Optional<Y> oy = xy.get(x);
    Map<Y, X> yx2 = oy.isNothing() ? yx : yx.delete(oy.some().x);
    return new Bijection<X, Y>(xy2, yx2);
  }

  public Bijection<X, Y> deleteY(Y y) {
    Map<Y, X> yx2 = yx.delete(y);
    Optional<X> oy = yx.get(y);
    Map<X, Y> xy2 = oy.isNothing() ? xy : xy.delete(oy.some().x);
    return new Bijection<X, Y>(xy2, yx2);
  }

  public static <X, Y> Bijection<X, Y> empty() {
    return new Bijection<X, Y>(Map.<X, Y> empty(), Map.<Y, X> empty());
  }

  @Override
  public String toString() {
    return "Bijection [xy=" + xy + ", yx=" + yx + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((xy == null) ? 0 : xy.hashCode());
    result = prime * result + ((yx == null) ? 0 : yx.hashCode());
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
    Bijection other = (Bijection) obj;
    if (xy == null) {
      if (other.xy != null)
        return false;
    } else if (!xy.equals(other.xy))
      return false;
    if (yx == null) {
      if (other.yx != null)
        return false;
    } else if (!yx.equals(other.yx))
      return false;
    return true;
  }
}
