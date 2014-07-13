package com.pokemon.kore.utils;

import static com.pokemon.kore.utils.Comparison.EQ;

public class UnitComparer implements Comparer<Unit> {
  public Comparison compare(Unit a, Unit b) {
    return EQ;
  }
}
