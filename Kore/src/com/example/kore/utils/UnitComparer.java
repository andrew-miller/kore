package com.example.kore.utils;

import com.example.kore.utils.Comparer;
import com.example.kore.utils.Comparison;
import static com.example.kore.utils.Comparison.EQ;
import com.example.kore.utils.Unit;

public class UnitComparer implements Comparer<Unit> {
  public Comparison compare(Unit a, Unit b) {
    return EQ;
  }
}
