package com.pokemon.kore;

import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.PairUtils.pair;
import junit.framework.TestCase;

import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Pair;

public class MapTest extends TestCase {
  public static void testEmptyMap() {
    Map<Integer, Integer> m = Map.empty();
    assertEquals(nothing(), m.get(1));
  }

  public static void testReplace() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    Object o2 = new Object();
    m = m.put(1, o1);
    assertSame(o1, m.get(1).some().x);
    m = m.put(1, o1);
    assertSame(o1, m.get(1).some().x);
    m = m.put(1, o2);
    assertSame(o2, m.get(1).some().x);
  }

  public static void testPrefix() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    Object o2 = new Object();
    m = m.put(1, o1);
    assertSame(o1, m.get(1).some().x);
    m = m.put(11, o2);
    assertSame(o1, m.get(1).some().x);
    assertSame(o2, m.get(11).some().x);
  }

  public static void testPrefix2() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    Object o2 = new Object();
    m = m.put(11, o2);
    assertSame(o2, m.get(11).some().x);
    m = m.put(1, o1);
    assertSame(o2, m.get(11).some().x);
    assertSame(o1, m.get(1).some().x);
  }

  public static void testDelete() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    m = m.put(1, o1);
    assertSame(o1, m.get(1).some().x);
    m = m.delete(1);
    assertEquals(nothing(), m.get(1));
    m = m.delete(1);
    assertEquals(nothing(), m.get(1));
  }

  public static void testDelete2() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    Object o2 = new Object();
    m = m.put(1, o1);
    m = m.put(2, o2);
    assertSame(o1, m.get(1).some().x);
    assertSame(o2, m.get(2).some().x);
    m = m.delete(1);
    assertEquals(nothing(), m.get(1));
    assertSame(o2, m.get(2).some().x);
    m = m.delete(1);
    assertEquals(nothing(), m.get(1));
    assertSame(o2, m.get(2).some().x);
    m = m.delete(2);
    assertEquals(nothing(), m.get(1));
    assertEquals(nothing(), m.get(2));
    m = m.delete(2);
    assertEquals(nothing(), m.get(1));
    assertEquals(nothing(), m.get(2));
  }

  public static void testDeleteLeaf() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    Object o2 = new Object();
    m = m.put(1, o1);
    m = m.put(11, o2);
    assertSame(o1, m.get(1).some().x);
    assertSame(o2, m.get(11).some().x);
    m = m.delete(11);
    assertSame(o1, m.get(1).some().x);
    assertEquals(nothing(), m.get(11));
  }

  public static void testDelete2LeavesThenRoot() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    Object o2 = new Object();
    Object o3 = new Object();
    m = m.put(1, o1);
    m = m.put(11, o2);
    m = m.put(12, o3);
    assertSame(o1, m.get(1).some().x);
    assertSame(o2, m.get(11).some().x);
    assertSame(o3, m.get(12).some().x);
    m = m.delete(11);
    assertSame(o1, m.get(1).some().x);
    assertEquals(nothing(), m.get(11));
    assertSame(o3, m.get(12).some().x);
    m = m.delete(12);
    assertSame(o1, m.get(1).some().x);
    assertEquals(nothing(), m.get(11));
    assertEquals(nothing(), m.get(12));
    m = m.delete(1);
    assertEquals(nothing(), m.get(1));
    assertEquals(nothing(), m.get(11));
    assertEquals(nothing(), m.get(12));
  }

  public static void testDelete1Of2LeavesThenAddNewLeaf() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    Object o2 = new Object();
    Object o3 = new Object();
    m = m.put(1, o1);
    m = m.put(11, o2);
    m = m.put(12, o3);
    assertSame(o1, m.get(1).some().x);
    assertSame(o2, m.get(11).some().x);
    assertSame(o3, m.get(12).some().x);
    m = m.delete(12);
    assertSame(o1, m.get(1).some().x);
    assertSame(o2, m.get(11).some().x);
    assertEquals(nothing(), m.get(12));
    Object o4 = new Object();
    m = m.put(13, o4);
    assertSame(o1, m.get(1).some().x);
    assertSame(o2, m.get(11).some().x);
    assertEquals(nothing(), m.get(12));
    assertSame(o4, m.get(13).some().x);
  }

  public static void testAddLeavesThenRoot() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    Object o2 = new Object();
    Object o3 = new Object();
    m = m.put(11, o2);
    m = m.put(12, o3);
    m = m.put(1, o1);
    assertSame(o1, m.get(1).some().x);
    assertSame(o2, m.get(11).some().x);
    assertSame(o3, m.get(12).some().x);
  }

  public static void testDeleteRoot() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    Object o2 = new Object();
    m = m.put(1, o1);
    m = m.put(11, o2);
    assertSame(o1, m.get(1).some().x);
    assertSame(o2, m.get(11).some().x);
    m = m.delete(1);
    assertEquals(nothing(), m.get(1));
    assertSame(o2, m.get(11).some().x);
    m = m.delete(1);
    assertEquals(nothing(), m.get(1));
    assertSame(o2, m.get(11).some().x);
  }

  public static void testDeleteRoot2() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    Object o2 = new Object();
    Object o3 = new Object();
    m = m.put(1, o1);
    m = m.put(11, o2);
    m = m.put(12, o3);
    assertSame(o1, m.get(1).some().x);
    assertSame(o2, m.get(11).some().x);
    assertSame(o3, m.get(12).some().x);
    m = m.delete(1);
    assertEquals(nothing(), m.get(1));
    assertSame(o2, m.get(11).some().x);
    assertSame(o3, m.get(12).some().x);
    m = m.delete(1);
    assertEquals(nothing(), m.get(1));
    assertSame(o2, m.get(11).some().x);
    assertSame(o3, m.get(12).some().x);
  }

  public static void testDeleteReaddDeleteLeaf() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    Object o2 = new Object();
    m = m.put(1, o1);
    for (int i = 0; i < 10; i++) {
      m = m.put(11, o2);
      assertSame(o1, m.get(1).some().x);
      assertSame(o2, m.get(11).some().x);
      m = m.delete(11);
      assertSame(o1, m.get(1).some().x);
      assertEquals(nothing(), m.get(11));
    }
  }

  public static void testEquality() {
    Map<Integer, Object> m = Map.empty();
    assertEquals(m, m);
    Object o1 = new Object();
    Object o2 = new Object();
    Object o3 = new Object();
    Map<Integer, Object> m1 = m.put(1, o1);
    assertEquals(m1, m1);
    assertEquals(m1, m.put(1, o1));
    assertEquals(m.put(1, o1), m1);
    assertEquals(m.put(1, o1), m.put(1, o1));
    Map<Integer, Object> m2 = m1.put(11, o2);
    assertEquals(m2, m2);
    assertEquals(m2, m1.put(11, o2));
    assertEquals(m1.put(11, o2), m2);
    assertEquals(m1.put(11, o2), m1.put(11, o2));
    Map<Integer, Object> m3 = m2.put(12, o3);
    assertEquals(m3, m3);
    assertEquals(m3, m2.put(12, o3));
    assertEquals(m2.put(12, o3), m3);
    assertEquals(m2.put(12, o3), m2.put(12, o3));
  }

  public static void testEquality2() {
    Map<Integer, Object> m = Map.empty();
    assertEquals(m, m);
    Object o1 = new Object();
    Object o2 = new Object();
    Object o3 = new Object();
    Map<Integer, Object> m1 = m.put(1, o1);
    assertFalse(m.equals(m1));
    assertFalse(m1.equals(m));
    Map<Integer, Object> m2 = m.put(11, o2);
    assertFalse(m.equals(m2));
    assertFalse(m2.equals(m));
    assertFalse(m1.equals(m2));
    assertFalse(m2.equals(m1));
    Map<Integer, Object> m3 = m.put(12, o3);
    assertFalse(m.equals(m3));
    assertFalse(m3.equals(m));
    assertFalse(m1.equals(m3));
    assertFalse(m3.equals(m1));
    assertFalse(m2.equals(m3));
    assertFalse(m3.equals(m2));
  }

  public static void testEntrySet() {
    Map<Integer, Object> m = Map.empty();
    List<Pair<Integer, Object>> l = nil();
    assertEquals(l, m.entrySet());
    Object o1 = new Object();
    m = m.put(1, o1);
    l = append(pair(1, o1), l);
    assertEquals(l, m.entrySet());
    Object o2 = new Object();
    m = m.put(2, o2);
    l = append(pair(2, o2), l);
    assertEquals(l, m.entrySet());
    Object o3 = new Object();
    m = m.put(3, o3);
    l = append(pair(3, o3), l);
    assertEquals(l, m.entrySet());
  }

  public static void testPutPutDelete() {
    Map<Integer, Object> m = Map.empty();
    Object o1 = new Object();
    assertEquals(nothing(), m.put(1, o1).put(1, o1).delete(1).get(1));
  }
}