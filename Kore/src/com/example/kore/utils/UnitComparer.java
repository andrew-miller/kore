package com.example.kore.utils;

import static com.example.kore.utils.Comparison.EQ;

public class UnitComparer implements Comparer<Unit> {
  public Comparison compare(Unit a, Unit b) {
    return EQ;
  }
}
