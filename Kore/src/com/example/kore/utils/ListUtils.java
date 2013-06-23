package com.example.kore.utils;

import java.util.Iterator;

public class ListUtils {
  public static <T> List<T> nil() {
    return List.nil(new List.Nil<T>());
  }

  public static <T> List<T> cons(T x, List<T> l) {
    return List.cons(new List.Cons<T>(x, l));
  }

  public static <T, T2> List<T2> map(F<T, T2> f, Class<T2> t2, List<T> l) {
    return l.isEmpty() ? List.nil(new List.Nil<T2>()) : List
        .cons(new List.Cons<T2>(f.f(l.cons().x), map(f, t2, l.cons().tail)));
  }

  public static <T> List<T> append(T x, List<T> l) {
    if (l.isEmpty())
      return cons(x, l);
    return cons(l.cons().x, append(x, l.cons().tail));
  }

  public static <T> List<T> append(List<T> l1, List<T> l2) {
    if (l1.isEmpty())
      return l2;
    return cons(l1.cons().x, append(l1.cons().tail, l2));
  }

  public static <T> boolean isSubList(List<T> xs, List<T> ys) {
    while (!xs.isEmpty()) {
      if (ys.isEmpty())
        return false;
      if (!xs.cons().x.equals(ys.cons().x))
        return false;
      xs = xs.cons().tail;
      ys = ys.cons().tail;
    }
    return true;
  }

  public static <T> Iterable<T> iter(final List<T> l) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          List<T> l_ = l;

          @Override
          public boolean hasNext() {
            return !l_.isEmpty();
          }

          @Override
          public T next() {
            if (l_.isEmpty())
              throw new RuntimeException("empty");
            T x = l_.cons().x;
            l_ = l_.cons().tail;
            return x;
          }

          @Override
          public void remove() {
            throw new RuntimeException("wtf");
          }

        };
      }
    };
  }

}
