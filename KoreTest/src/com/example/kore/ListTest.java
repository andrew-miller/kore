package com.example.kore;

import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.nil;

import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;

import junit.framework.TestCase;

public class ListTest extends TestCase {
  public static void testIsEmptyIsEmpty() {
    assertEquals(true, nil().isEmpty());
    assertEquals(true, nil().isEmpty());
    assertEquals(true, nil().isEmpty());
  }

  public static void testNonEmptyIsNotEmpty() {
    assertFalse(cons(1, nil()).isEmpty());
    assertFalse(cons(2, cons(1, nil())).isEmpty());
    assertFalse(cons(3, cons(2, cons(1, nil()))).isEmpty());
  }

  public static void testConstructorsRejectNull() {
    boolean fail = false;
    try {
      new List.Cons<Void>(null, ListUtils.<Void> nil());
    } catch (Exception e) {
      fail = true;
    }
    assertTrue(fail);

    fail = false;
    try {
      new List.Cons<Integer>(1, null);
    } catch (Exception e) {
      fail = true;
    }
    assertTrue(fail);
  }

  public static void testHead() {
    assertTrue(cons(true, ListUtils.<Boolean> nil()).cons().x);
    assertEquals(Integer.valueOf(1), cons(1, nil()).cons().x);
  }

}
