package com.example.kore.codes;

import fj.F2;
import fj.Ord;
import fj.Ordering;

public final class LabelOrd {

  public static Ord<Label> ord() {
    return Ord.ord(new F2<Label, Label, Ordering>() {
      @Override
      public Ordering f(Label a, Label b) {
        int x = a.label.compareTo(b.label);
        return x < 0 ? Ordering.LT : (x > 0 ? Ordering.GT : Ordering.EQ);
      }
    }.curry());
  }
}
