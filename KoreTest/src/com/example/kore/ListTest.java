package com.example.kore;

import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.nil;

import com.example.kore.utils.List;

import junit.framework.TestCase;

public class ListTest extends TestCase {
  public static void testIsEmptyIsEmpty() {
    assertEquals(true, nil(Void.class).isEmpty());
    assertEquals(true, nil(Integer.class).isEmpty());
    assertEquals(true, nil(Boolean.class).isEmpty());
  }

  public static void testNonEmptyIsNotEmpty() {
    assertFalse(cons(1, nil(Integer.class)).isEmpty());
    assertFalse(cons(2, cons(1, nil(Integer.class))).isEmpty());
    assertFalse(cons(3, cons(2, cons(1, nil(Integer.class)))).isEmpty());
  }

  public static void testConstructorsRejectNull() {
    boolean fail = false;
    try {
      new List.Nil<Void>(null);
    } catch (Exception e) {
      fail = true;
    }
    assertTrue(fail);

    fail = false;
    try {
      new List.Cons<Void>(null, nil(Void.class));
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
    assertTrue(cons(true, nil(Boolean.class)).cons().x);
    assertEquals(Integer.valueOf(1), cons(1, nil(Integer.class)).cons().x);
  }

  public static void testCheckType() {
    List<Boolean> l = nil(Boolean.class);
    l.checkType(Boolean.class);
    boolean fail = false;
    try {
      ((List<Integer>) (Object) l).checkType(Integer.class);
    } catch (Exception e) {
      fail = true;
    }
    assertTrue(fail);
  }

  public static void testHeapPollutionPrevention() {
    List<Boolean> x = nil(Boolean.class);
    List<Integer> y = (List<Integer>) (Object) x;
    boolean fail = false;
    try {
      cons(1, y);
    } catch (Exception e) {
      fail = true;
    }
    assertTrue(fail);
  }

}
