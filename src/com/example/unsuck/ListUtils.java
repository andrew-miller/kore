package com.example.unsuck;

import java.util.Iterator;
import java.util.LinkedList;

import fj.data.List;

public final class ListUtils {

  public static <T> LinkedList<T> toLinkedList(List<T> l) {
    LinkedList<T> ll = new LinkedList<T>();
    for (T e : l)
      ll.add(e);
    return ll;
  }

  public static <T> List<T> fromLinkedList(LinkedList<T> ll) {
    Iterator<T> i = ll.descendingIterator();
    List<T> l = List.nil();
    while (i.hasNext())
      l = List.cons(i.next(), l);
    return l;
  }
}
