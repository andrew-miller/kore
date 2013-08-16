package com.example.kore.utils;

import java.util.Iterator;
import java.util.LinkedList;

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
            throw new UnsupportedOperationException();
          }

        };
      }
    };
  }

  public static <T> List<T> singleton(T x) {
    return cons(x, ListUtils.<T> nil());
  }

  public static <T> List<T> reverse(List<T> l) {
    List<T> l2 = nil();
    while (!l.isEmpty()) {
      l2 = cons(l.cons().x, l2);
      l = l.cons().tail;
    }
    return l2;
  }

  public static <T> List<T> sort(List<T> l, Comparer<T> c) {
    LinkedList<LinkedList<T>> p = new LinkedList<LinkedList<T>>();
    for (T t : iter(l)) {
      LinkedList<T> s = new LinkedList<T>();
      s.add(t);
      p.add(s);
    }
    return fromLinkedList(sort_(p, c));
  }

  private static <T> LinkedList<T> sort_(LinkedList<LinkedList<T>> p,
      Comparer<T> c) {
    if (p.isEmpty())
      return new LinkedList<T>();
    if (p.size() == 1)
      return p.remove(0);
    LinkedList<LinkedList<T>> p2 = new LinkedList<LinkedList<T>>();
    while (p.size() >= 2) {
      LinkedList<T> l = new LinkedList<T>();
      LinkedList<T> a = p.remove(0);
      LinkedList<T> b = p.remove(0);
      while (!a.isEmpty() && !b.isEmpty()) {
        switch (c.compare(a.get(0), b.get(0))) {
        case LT:
        case EQ:
          l.add(a.remove(0));
          break;
        case GT:
          l.add(b.remove(0));
        }
      }
      if (!a.isEmpty())
        l.addAll(a);
      if (!b.isEmpty())
        l.addAll(b);
      p2.add(l);
    }
    if (!p.isEmpty())
      p2.add(p.remove(0));
    return sort_(p2, c);
  }

  public static <T> List<T> fromArray(T... a) {
    List<T> l = nil();
    for (int i = a.length - 1; i >= 0; i--) {
      l = cons(a[i], l);
    }
    return l;
  }

  public static <T> List<T> fromLinkedList(LinkedList<T> ll) {
    List<T> l = nil();
    Iterator<T> i = ll.descendingIterator();
    while (i.hasNext()) {
      l = cons(i.next(), l);
    }
    return l;
  }
}
