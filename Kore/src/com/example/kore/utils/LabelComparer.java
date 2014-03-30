package com.example.kore.utils;

import com.example.kore.codes.Label;
import static com.example.kore.utils.Comparison.EQ;
import static com.example.kore.utils.Comparison.GT;
import static com.example.kore.utils.Comparison.LT;

public class LabelComparer implements Comparer<Label> {
  public Comparison compare(Label a, Label b) {
    int c = a.label.compareTo(b.label);
    if (c < 0) return LT;
    if (c > 0) return GT;
    return EQ;
  }
}