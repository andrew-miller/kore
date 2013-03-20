package com.example.kore.codes;

import fj.F2;
import fj.Ord;
import fj.Ordering;

public final class CodeOrd {

  public static Ord<Code> ord() {
    return Ord.ord(new F2<Code, Code, Ordering>() {
      @Override
      public Ordering f(Code a, Code b) {
        int hcA = System.identityHashCode(a);
        int hcB = System.identityHashCode(b);
        return hcA < hcB ? Ordering.LT
            : (hcA > hcB ? Ordering.GT : Ordering.EQ);
      }
    }.curry());
  }
}
