package com.example.kore.utils;

import static com.example.kore.utils.OptionalUtils.nothing;
import static com.example.kore.utils.OptionalUtils.some;
import static com.example.kore.utils.PairUtils.pair;

import java.util.Iterator;
import java.util.LinkedList;

public class ListUtils {
  public static <T> List<T> nil() {
    return List.nil(new List.Nil<T>());
  }

  public static <T> List<T> cons(T x, List<T> l) {
    return List.cons(new List.Cons<T>(x, l));
  }

  public static <T, T2> List<T2> map(F<T, T2> f, List<T> l) {
    return l.isEmpty() ? ListUtils.<T2> nil() : cons(f.f(l.cons().x),
        map(f, l.cons().tail));
  }

  public static <T> List<T> append(T x, List<T> l) {
    return l.isEmpty() ? cons(x, l)
        : cons(l.cons().x, append(x, l.cons().tail));
  }

  public static <T> List<T> append(List<T> l1, List<T> l2) {
    return l1.isEmpty() ? l2 : cons(l1.cons().x, append(l1.cons().tail, l2));
  }

  public static <T> boolean isPrefix(List<T> xs, List<T> ys) {
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
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          List<T> l_ = l;

          public boolean hasNext() {
            return !l_.isEmpty();
          }

          public T next() {
            if (l_.isEmpty())
              throw new RuntimeException("empty");
            T x = l_.cons().x;
            l_ = l_.cons().tail;
            return x;
          }

          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }
    };
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

  public static <T> Optional<T> nth(List<T> l, Integer n) {
    for (; n != 0; n--) {
      if (l.isEmpty())
        return nothing();
      l = l.cons().tail;
    }
    if (l.isEmpty())
      return nothing();
    return some(l.cons().x);
  }

  public static <T> List<T> replace(List<T> l, int i, T x) {
    return i == 0 ? l.isEmpty() ? fromArray(x) : cons(x, l.cons().tail) : cons(
        l.cons().x, replace(l.cons().tail, i - 1, x));
  }

  public static <T> Integer length(List<T> l) {
    Integer n = 0;
    while (!l.isEmpty()) {
      n++;
      l = l.cons().tail;
    }
    return n;
  }

  public static <T> Iterable<T> cycle(final List<T> l) {
    return new Iterable<T>() {
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          List<T> l2 = l;

          public boolean hasNext() {
            return true;
          }

          public T next() {
            T x = l2.cons().x;
            l2 = l2.cons().tail;
            if (l2.isEmpty())
              l2 = l;
            return x;
          }

          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

  public static <A, B> List<Pair<A, B>> zip(Iterator<A> a, Iterator<B> b) {
    if (!a.hasNext())
      return nil();
    if (!b.hasNext())
      return nil();
    return cons(pair(a.next(), b.next()), zip(a, b));
  }

  public static <T> List<T> insert(List<T> l, Integer i, T x) {
    return i == 0 ? cons(x, l) : cons(l.cons().x,
        insert(l.cons().tail, i - 1, x));
  }

  public static <T> List<T> remove(List<T> l, Integer i) {
    return i == 0 ? l.cons().tail : cons(l.cons().x,
        remove(l.cons().tail, i - 1));
  }

  public static <T> List<T> move(List<T> l, Integer src, Integer dest) {
    return insert(remove(l, src), dest > src ? dest - 1 : dest, nth(l, src)
        .some().x);
  }

  public static <T> List<T> drop(List<T> l, Integer n) {
    return n == 0 ? l : drop(l.cons().tail, n - 1);
  }

  public static <T> List<T> take(List<T> l, Integer n) {
    return n == 0 ? ListUtils.<T> nil() : cons(l.cons().x,
        take(l.cons().tail, n - 1));
  }
}