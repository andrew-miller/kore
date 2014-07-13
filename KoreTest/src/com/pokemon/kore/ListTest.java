package com.pokemon.kore;

import static com.pokemon.kore.utils.ListUtils.cons;
import static com.pokemon.kore.utils.ListUtils.move;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.ListUtils.sort;
import junit.framework.TestCase;

import com.pokemon.kore.utils.CharacterComparer;
import com.pokemon.kore.utils.IntegerComparer;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.ListUtils;

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

  private static void tsi(List<Integer> a, List<Integer> b) {
    assertEquals(b, sort(a, new IntegerComparer()));
  }

  private static void tsc(List<Character> a, List<Character> b) {
    assertEquals(b, sort(a, new CharacterComparer()));
  }

  private static <T> List<Integer> li(Integer... a) {
    return ListUtils.fromArray(a);
  }

  private static <T> List<Character> lc(Character... a) {
    return ListUtils.fromArray(a);
  }

  public static void testSortIntegers() {
    tsi(li(), li());
    tsi(li(1), li(1));
    tsi(li(2), li(2));
    tsi(li(3), li(3));
    tsi(li(1, 1), li(1, 1));
    tsi(li(1, 2), li(1, 2));
    tsi(li(2, 1), li(1, 2));
    tsi(li(2, 2), li(2, 2));
    tsi(li(1, 1, 1), li(1, 1, 1));
    tsi(li(1, 1, 2), li(1, 1, 2));
    tsi(li(1, 1, 3), li(1, 1, 3));
    tsi(li(1, 2, 1), li(1, 1, 2));
    tsi(li(1, 2, 2), li(1, 2, 2));
    tsi(li(1, 2, 3), li(1, 2, 3));
    tsi(li(1, 3, 1), li(1, 1, 3));
    tsi(li(1, 3, 2), li(1, 2, 3));
    tsi(li(1, 3, 3), li(1, 3, 3));
    tsi(li(2, 1, 1), li(1, 1, 2));
    tsi(li(2, 1, 2), li(1, 2, 2));
    tsi(li(2, 1, 3), li(1, 2, 3));
    tsi(li(2, 2, 1), li(1, 2, 2));
    tsi(li(2, 2, 2), li(2, 2, 2));
    tsi(li(2, 2, 3), li(2, 2, 3));
    tsi(li(2, 3, 1), li(1, 2, 3));
    tsi(li(2, 3, 2), li(2, 2, 3));
    tsi(li(2, 3, 3), li(2, 3, 3));
    tsi(li(3, 1, 1), li(1, 1, 3));
    tsi(li(3, 1, 2), li(1, 2, 3));
    tsi(li(3, 1, 3), li(1, 3, 3));
    tsi(li(3, 2, 1), li(1, 2, 3));
    tsi(li(3, 2, 2), li(2, 2, 3));
    tsi(li(3, 2, 3), li(2, 3, 3));
    tsi(li(3, 3, 1), li(1, 3, 3));
    tsi(li(3, 3, 2), li(2, 3, 3));
    tsi(li(3, 3, 3), li(3, 3, 3));
  }

  public static void testSortCharacters() {
    tsc(lc(), lc());
    tsc(lc('x'), lc('x'));
    tsc(lc('y'), lc('y'));
    tsc(lc('z'), lc('z'));
    tsc(lc('x', 'x'), lc('x', 'x'));
    tsc(lc('x', 'y'), lc('x', 'y'));
    tsc(lc('y', 'x'), lc('x', 'y'));
    tsc(lc('y', 'y'), lc('y', 'y'));
    tsc(lc('x', 'x', 'x'), lc('x', 'x', 'x'));
    tsc(lc('x', 'x', 'y'), lc('x', 'x', 'y'));
    tsc(lc('x', 'x', 'z'), lc('x', 'x', 'z'));
    tsc(lc('x', 'y', 'x'), lc('x', 'x', 'y'));
    tsc(lc('x', 'y', 'y'), lc('x', 'y', 'y'));
    tsc(lc('x', 'y', 'z'), lc('x', 'y', 'z'));
    tsc(lc('x', 'z', 'x'), lc('x', 'x', 'z'));
    tsc(lc('x', 'z', 'y'), lc('x', 'y', 'z'));
    tsc(lc('x', 'z', 'z'), lc('x', 'z', 'z'));
    tsc(lc('y', 'x', 'x'), lc('x', 'x', 'y'));
    tsc(lc('y', 'x', 'y'), lc('x', 'y', 'y'));
    tsc(lc('y', 'x', 'z'), lc('x', 'y', 'z'));
    tsc(lc('y', 'y', 'x'), lc('x', 'y', 'y'));
    tsc(lc('y', 'y', 'y'), lc('y', 'y', 'y'));
    tsc(lc('y', 'y', 'z'), lc('y', 'y', 'z'));
    tsc(lc('y', 'z', 'x'), lc('x', 'y', 'z'));
    tsc(lc('y', 'z', 'y'), lc('y', 'y', 'z'));
    tsc(lc('y', 'z', 'z'), lc('y', 'z', 'z'));
    tsc(lc('z', 'x', 'x'), lc('x', 'x', 'z'));
    tsc(lc('z', 'x', 'y'), lc('x', 'y', 'z'));
    tsc(lc('z', 'x', 'z'), lc('x', 'z', 'z'));
    tsc(lc('z', 'y', 'x'), lc('x', 'y', 'z'));
    tsc(lc('z', 'y', 'y'), lc('y', 'y', 'z'));
    tsc(lc('z', 'y', 'z'), lc('y', 'z', 'z'));
    tsc(lc('z', 'z', 'x'), lc('x', 'z', 'z'));
    tsc(lc('z', 'z', 'y'), lc('y', 'z', 'z'));
    tsc(lc('z', 'z', 'z'), lc('z', 'z', 'z'));
  }

  public static void testMove() {
    assertEquals(lc('a'), move(lc('a'), 0, 0));
    assertEquals(lc('a', 'b'), move(lc('a', 'b'), 0, 0));
    assertEquals(lc('a', 'b', 'c'), move(lc('a', 'b', 'c'), 0, 0));
    assertEquals(lc('a'), move(lc('a'), 0, 1));
    assertEquals(lc('a', 'b'), move(lc('a', 'b'), 1, 2));
    assertEquals(lc('b', 'a'), move(lc('a', 'b'), 0, 2));
    assertEquals(lc('b', 'a'), move(lc('a', 'b'), 1, 0));
    assertEquals(lc('b', 'c', 'a'), move(lc('a', 'b', 'c'), 0, 3));
  }
}
