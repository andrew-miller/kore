package com.example.kore.utils;

import java.io.Serializable;

public class CharacterComparer implements Comparer<Character>, Serializable {
  public Comparison compare(Character a, Character b) {
    if (a.equals(b))
      return Comparison.EQ;
    if (a < b)
      return Comparison.LT;
    return Comparison.GT;
  }
}
