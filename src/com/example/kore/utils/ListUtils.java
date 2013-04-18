package com.example.kore.utils;

import java.util.Iterator;
import java.util.List;

public class ListUtils {
  public static <T> boolean isSubList(List<T> xs, List<T> ys) {
    Iterator<T> yi = ys.iterator();
    for (T x : xs) {
      if (!yi.hasNext())
        return false;
      if (!x.equals(yi.next()))
        return false;
    }
    return true;
  }
}
