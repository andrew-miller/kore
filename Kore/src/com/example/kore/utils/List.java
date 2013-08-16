package com.example.kore.utils;

import java.io.Serializable;

/**
 * Immutable thread-safe null-free list.
 * 
 * default serialization
 */
public final class List<T> implements Serializable {

  @Override
  public String toString() {
    if (isEmpty())
      return "<>";
    String s = "<" + cons().x;
    List<T> head = cons().tail;
    while (!head.isEmpty()) {
      s += "," + head.cons().x;
      head = head.cons().tail;
    }
    return s + ">";
  }

  public static final class Nil<T> implements Serializable {

    @Override
    public String toString() {
      return "Nil []";
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      return true;
    }
  }

  public static final class Cons<T> implements Serializable {
    public final T x;
    public final List<T> tail;

    @Override
    public String toString() {
      return "Cons [x=" + x + ", tail=" + tail + "]";
    }

    public Cons(T x, List<T> tail) {
      if (x == null)
        throw new RuntimeException("null head");
      if (tail == null)
        throw new RuntimeException("null tail");
      this.x = x;
      this.tail = tail;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((tail == null) ? 0 : tail.hashCode());
      result = prime * result + ((x == null) ? 0 : x.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      Cons other = (Cons) obj;
      if (tail == null) {
        if (other.tail != null)
          return false;
      } else if (!tail.equals(other.tail))
        return false;
      if (x == null) {
        if (other.x != null)
          return false;
      } else if (!x.equals(other.x))
        return false;
      return true;
    }
  }

  private final Nil<T> nil;
  private final Cons<T> cons;

  private List(Nil<T> nil, Cons<T> cons) {
    this.nil = nil;
    this.cons = cons;
  }

  public boolean isEmpty() {
    return nil != null;
  }

  public Nil<T> nil() {
    if (nil == null)
      throw new RuntimeException("not nil");
    return nil;
  }

  public Cons<T> cons() {
    if (cons == null)
      throw new RuntimeException("not cons");
    return cons;
  }

  public static <T> List<T> cons(Cons<T> cons) {
    if (cons == null)
      throw new RuntimeException("constructor received null");
    return new List<T>(null, cons);
  }

  public static <T> List<T> nil(Nil<T> nil) {
    if (nil == null)
      throw new RuntimeException("constructor received null");
    return new List<T>(nil, null);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((cons == null) ? 0 : cons.hashCode());
    result = prime * result + ((nil == null) ? 0 : nil.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    List other = (List) obj;
    if (cons == null) {
      if (other.cons != null)
        return false;
    } else if (!cons.equals(other.cons))
      return false;
    if (nil == null) {
      if (other.nil != null)
        return false;
    } else if (!nil.equals(other.nil))
      return false;
    return true;
  }

}