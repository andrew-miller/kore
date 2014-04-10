package com.example.kore;

import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.nil;
import junit.framework.TestCase;

import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.Either;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;

public class CodeRerootTest extends TestCase {
  private static final List<Label> nilPath = nil();

  public static void testUnit() {
    assertEquals(unit, reroot(unit, nilPath));
  }

  // {'a {}}
  public static void testProd1() {
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(unit));
    Code c = Code.newProduct(m);
    assertEquals(c, reroot(c, nilPath));
    Code c2 = reroot(c, cons(new Label("a"), nilPath));
    assertFalse(c.equals(c2));
    assertEquals(c2, unit);
  }

  // {'a {}, 'b, {}}
  public static void testProd1_1() {
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(unit));
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(unit));
    Code c = Code.newProduct(m);
    assertEquals(c, reroot(c, nilPath));
    Code c2 = reroot(c, cons(new Label("a"), nilPath));
    assertFalse(c.equals(c2));
    assertEquals(c2, unit);
    Code c3 = reroot(c, cons(new Label("b"), nilPath));
    assertFalse(c.equals(c3));
    assertEquals(c3, unit);
  }

  // {'a <>}
  public static void testProdLoop() {
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> y(nilPath));
    Code c = Code.newProduct(m);
    assertEquals(c, reroot(c, nilPath));
  }

  // {'a {'b <>}}
  public static void testProdLoop2() {
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m = m.put(new Label("b"), Either.<Code, List<Label>> y(nilPath));
    Code c = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(c));
    Code c2 = Code.newProduct(m);
    // {'b {'a t}};
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> y(nilPath));
    Code c3 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(c3));
    assertEquals(Code.newProduct(m), reroot(c2, cons(new Label("a"), nilPath)));
  }

  // {'a {'b <>}, 'b <>}
  public static void testProdLoop2_() {
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m = m.put(new Label("b"), Either.<Code, List<Label>> y(nilPath));
    Code c = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(c));
    m = m.put(new Label("b"), Either.<Code, List<Label>> y(nilPath));
    c = Code.newProduct(m);
    assertEquals(c, reroot(c, nilPath));
    assertEquals(c, reroot(c, cons(new Label("b"), nilPath)));
    assertEquals(c,
        reroot(c, cons(new Label("a"), cons(new Label("b"), nilPath))));
    // {'b: {'a <>, 'b <b>}}
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> y(nilPath));
    m =
        m.put(new Label("b"),
            Either.<Code, List<Label>> y(cons(new Label("b"), nilPath)));
    Code c2 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(c2));
    c2 = Code.newProduct(m);
    assertEquals(c2, reroot(c, cons(new Label("a"), nilPath)));
    assertEquals(
        c2,
        reroot(
            c,
            cons(new Label("a"),
                cons(new Label("b"), cons(new Label("a"), nilPath)))));
  }

  // {'$x {'$y {'$z <>}}}
  private static Code makeProdLoop3(String x, String y, String z) {
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m = m.put(new Label(z), Either.<Code, List<Label>> y(nilPath));
    Code c = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label(y), Either.<Code, List<Label>> x(c));
    c = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label(x), Either.<Code, List<Label>> x(c));
    c = Code.newProduct(m);
    return c;
  }

  // {'a {'b {'c <>}}}
  public static void testProdLoop3() {
    Code c = makeProdLoop3("a", "b", "c");
    assertEquals(c, reroot(c, nilPath));
    // {'b {'c {'a <>}}};
    Code c2 = makeProdLoop3("b", "c", "a");
    assertEquals(c2, reroot(c, cons(new Label("a"), nilPath)));
    // {'c {'a {'b <>}}};
    Code c3 = makeProdLoop3("c", "a", "b");
    assertEquals(c3,
        reroot(c, cons(new Label("a"), cons(new Label("b"), nilPath))));
  }

  // {'a {'a <b>}, 'b {'a <a>}}
  public static void testCross() {
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m =
        m.put(new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("b"), nilPath)));
    Code ca1 = Code.newProduct(m);
    m = Map.empty();
    m =
        m.put(new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("a"), nilPath)));
    Code ca2 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(ca1));
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(ca2));
    Code ca = Code.newProduct(m);

    // {'a {'a {'a <a>}}, 'b <a,a>}
    m = Map.empty();
    m =
        m.put(new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("a"), nilPath)));
    Code cb1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(cb1));
    cb1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(cb1));
    m =
        m.put(
            new Label("b"),
            Either.<Code, List<Label>> y(cons(new Label("a"),
                cons(new Label("a"), nilPath))));
    Code cb = Code.newProduct(m);

    // {'a <b,a>, 'b {'a {'a <b>}}}
    m = Map.empty();
    m =
        m.put(new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("b"), nilPath)));
    Code cc1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(cc1));
    cc1 = Code.newProduct(m);
    m = Map.empty();
    m =
        m.put(
            new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("b"),
                cons(new Label("a"), nilPath))));
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(cc1));
    Code cc = Code.newProduct(m);

    for (Code c : new Code[] { ca, cb, cc }) {
      Code rc = reroot(c, nilPath);
      assertTrue(rc.equals(ca) || rc.equals(cb) || rc.equals(cc));
    }

    // {'a {'a <>}}
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> y(nilPath));
    Code rc = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(rc));
    rc = Code.newProduct(m);

    assertEquals(rc, reroot(ca, cons(new Label("a"), nilPath)));
    assertEquals(rc, reroot(ca, cons(new Label("b"), nilPath)));
  }

  // {'a {'a <b>, 'b <>}, 'b {'a <a>}}
  public static void testCross2() {
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m =
        m.put(new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("b"), nilPath)));
    m = m.put(new Label("b"), Either.<Code, List<Label>> y(nilPath));
    Code ca1 = Code.newProduct(m);
    m = Map.empty();
    m =
        m.put(new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("a"), nilPath)));
    Code ca2 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(ca1));
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(ca2));
    Code ca = Code.newProduct(m);

    // {'a {'a {'a <a>}, 'b <>}, 'b <a,a>}
    m = Map.empty();
    m =
        m.put(new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("a"), nilPath)));
    Code cb1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(cb1));
    m = m.put(new Label("b"), Either.<Code, List<Label>> y(nilPath));
    cb1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(cb1));
    m =
        m.put(
            new Label("b"),
            Either.<Code, List<Label>> y(cons(new Label("a"),
                cons(new Label("a"), nilPath))));
    Code cb = Code.newProduct(m);

    // {'a <b,a>, 'b {'a {'a <b>, 'b <>}}}
    m = Map.empty();
    m =
        m.put(new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("b"), nilPath)));
    m = m.put(new Label("b"), Either.<Code, List<Label>> y(nilPath));
    Code cc1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(cc1));
    cc1 = Code.newProduct(m);
    m = Map.empty();
    m =
        m.put(
            new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("b"),
                cons(new Label("a"), nilPath))));
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(cc1));
    Code cc = Code.newProduct(m);

    for (Code c : new Code[] { ca, cb, cc }) {
      Code rc = reroot(c, nilPath);
      assertTrue(rc.equals(ca) || rc.equals(cb) || rc.equals(cc));
    }

    // {'a {'a <>},'b {'a <>, 'b <a>}}
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> y(nilPath));
    Code cRa1_1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> y(nilPath));
    m =
        m.put(new Label("b"),
            Either.<Code, List<Label>> y(cons(new Label("a"), nilPath)));
    Code cRa1_2 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(cRa1_1));
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(cRa1_2));
    Code cRa1 = Code.newProduct(m);

    // {'a <b,b>, 'b {'a <>, 'b {'a <>}}}
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> y(nilPath));
    Code cRa2 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> y(nilPath));
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(cRa2));
    cRa2 = Code.newProduct(m);
    m = Map.empty();
    m =
        m.put(
            new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("b"),
                cons(new Label("b"), nilPath))));
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(cRa2));
    cRa2 = Code.newProduct(m);

    for (Code c : new Code[] { ca, cb, cc }) {
      Code rc = reroot(c, cons(new Label("a"), nilPath));
      assertTrue(rc.equals(cRa1) || rc.equals(cRa2));
    }

    // {'a {'a <>, 'b {'a <a>,'b <>}}}
    m = Map.empty();
    m =
        m.put(new Label("a"),
            Either.<Code, List<Label>> y(cons(new Label("a"), nilPath)));
    m = m.put(new Label("b"), Either.<Code, List<Label>> y(nilPath));
    Code cRb = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> y(nilPath));
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(cRb));
    cRb = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(cRb));
    cRb = Code.newProduct(m);

    for (Code c : new Code[] { ca, cb, cc }) {
      assertEquals(cRb, reroot(c, cons(new Label("b"), nilPath)));
    }
  }
}
