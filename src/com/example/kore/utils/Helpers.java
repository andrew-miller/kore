package com.example.kore.utils;

import java.util.Map;

import com.example.kore.codes.Label;

public class Helpers {

  public static <T> String labelMap(Map<Label, T> labels) {
    int size = labels.size();
    if (size == 0)
      return "";
    StringBuilder result = null;
    for (Label l : labels.keySet()) {
      if (result == null) {
        result = new StringBuilder("'" + l.label + " "
            + labels.get(l).toString());
      } else {
        result.append(", '" + l.label + " " + labels.get(l).toString());
      }
    }
    return result.toString();
  }
}
