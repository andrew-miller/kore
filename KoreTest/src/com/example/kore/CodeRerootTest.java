package com.example.kore;

import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.nil;

import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;

import junit.framework.TestCase;

public class CodeRerootTest extends TestCase {
  private static final List<Label> nilPath = nil();

  public static void testUnit() {
    assertEquals(CodeUtils.unit, CodeUtils.reRoot(CodeUtils.unit, nilPath));
  }

  // {'a {}}
  public static void testProd1() {
    Map<Label, CodeOrPath> m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(CodeUtils.unit));
    Code c = Code.newProduct(m);
    assertEquals(c, CodeUtils.reRoot(c, nilPath));
    Code c2 = CodeUtils.reRoot(c, cons(new Label("a"), nilPath));
    assertFalse(c.equals(c2));
    assertEquals(c2, CodeUtils.unit);
  }

  // {'a {}, 'b, {}}
  public static void testProd1_1() {
    Map<Label, CodeOrPath> m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(CodeUtils.unit));
    m = m.put(new Label("b"), CodeOrPath.newCode(CodeUtils.unit));
    Code c = Code.newProduct(m);
    assertEquals(c, CodeUtils.reRoot(c, nilPath));
    Code c2 = CodeUtils.reRoot(c, cons(new Label("a"), nilPath));
    assertFalse(c.equals(c2));
    assertEquals(c2, CodeUtils.unit);
    Code c3 = CodeUtils.reRoot(c, cons(new Label("b"), nilPath));
    assertFalse(c.equals(c3));
    assertEquals(c3, CodeUtils.unit);
  }

  // {'a <>}
  public static void testProdLoop() {
    Map<Label, CodeOrPath> m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newPath(nilPath));
    Code c = Code.newProduct(m);
    assertEquals(c, CodeUtils.reRoot(c, nilPath));
  }

  // {'a {'b <>}}
  public static void testProdLoop2() {
    Map<Label, CodeOrPath> m = Map.empty();
    m = m.put(new Label("b"), CodeOrPath.newPath(nilPath));
    Code c = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(c));
    Code c2 = Code.newProduct(m);
    // {'b {'a t}};
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newPath(nilPath));
    Code c3 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("b"), CodeOrPath.newCode(c3));
    assertEquals(Code.newProduct(m),
        CodeUtils.reRoot(c2, cons(new Label("a"), nilPath)));
  }

  // {'a {'b <>}, 'b <>}
  public static void testProdLoop2_() {
    Map<Label, CodeOrPath> m = Map.empty();
    m = m.put(new Label("b"), CodeOrPath.newPath(nilPath));
    Code c = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(c));
    m = m.put(new Label("b"), CodeOrPath.newPath(nilPath));
    c = Code.newProduct(m);
    assertEquals(c, CodeUtils.reRoot(c, nilPath));
    assertEquals(c, CodeUtils.reRoot(c, cons(new Label("b"), nilPath)));
    assertEquals(c, CodeUtils.reRoot(c,
        cons(new Label("a"), cons(new Label("b"), nilPath))));
    // {'b: {'a <>, 'b <b>}}
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newPath(nilPath));
    m =
        m.put(new Label("b"), CodeOrPath.newPath(cons(new Label("b"), nilPath)));
    Code c2 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("b"), CodeOrPath.newCode(c2));
    c2 = Code.newProduct(m);
    assertEquals(c2, CodeUtils.reRoot(c, cons(new Label("a"), nilPath)));
    assertEquals(
        c2,
        CodeUtils.reRoot(
            c,
            cons(new Label("a"),
                cons(new Label("b"), cons(new Label("a"), nilPath)))));
  }

  // {'$x {'$y {'$z <>}}}
  private static Code makeProdLoop3(String x, String y, String z) {
    Map<Label, CodeOrPath> m = Map.empty();
    m = m.put(new Label(z), CodeOrPath.newPath(nilPath));
    Code c = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label(y), CodeOrPath.newCode(c));
    c = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label(x), CodeOrPath.newCode(c));
    c = Code.newProduct(m);
    return c;
  }

  // {'a {'b {'c <>}}}
  public static void testProdLoop3() {
    Code c = makeProdLoop3("a", "b", "c");
    assertEquals(c, CodeUtils.reRoot(c, nilPath));
    // {'b {'c {'a <>}}};
    Code c2 = makeProdLoop3("b", "c", "a");
    assertEquals(c2, CodeUtils.reRoot(c, cons(new Label("a"), nilPath)));
    // {'c {'a {'b <>}}};
    Code c3 = makeProdLoop3("c", "a", "b");
    assertEquals(c3, CodeUtils.reRoot(c,
        cons(new Label("a"), cons(new Label("b"), nilPath))));
  }

  // {'a {'a <b>}, 'b {'a <a>}}
  public static void testCross() {
    Map<Label, CodeOrPath> m = Map.empty();
    m =
        m.put(new Label("a"), CodeOrPath.newPath(cons(new Label("b"), nilPath)));
    Code ca1 = Code.newProduct(m);
    m = Map.empty();
    m =
        m.put(new Label("a"), CodeOrPath.newPath(cons(new Label("a"), nilPath)));
    Code ca2 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(ca1));
    m = m.put(new Label("b"), CodeOrPath.newCode(ca2));
    Code ca = Code.newProduct(m);

    // {'a {'a {'a <a>}}, 'b <a,a>}
    m = Map.empty();
    m =
        m.put(new Label("a"), CodeOrPath.newPath(cons(new Label("a"), nilPath)));
    Code cb1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(cb1));
    cb1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(cb1));
    m =
        m.put(
            new Label("b"),
            CodeOrPath.newPath(cons(new Label("a"),
                cons(new Label("a"), nilPath))));
    Code cb = Code.newProduct(m);

    // {'a <b,a>, 'b {'a {'a <b>}}}
    m = Map.empty();
    m =
        m.put(new Label("a"), CodeOrPath.newPath(cons(new Label("b"), nilPath)));
    Code cc1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(cc1));
    cc1 = Code.newProduct(m);
    m = Map.empty();
    m =
        m.put(
            new Label("a"),
            CodeOrPath.newPath(cons(new Label("b"),
                cons(new Label("a"), nilPath))));
    m = m.put(new Label("b"), CodeOrPath.newCode(cc1));
    Code cc = Code.newProduct(m);

    for (Code c : new Code[] { ca, cb, cc }) {
      Code rc = CodeUtils.reRoot(c, nilPath);
      assertTrue(rc.equals(ca) || rc.equals(cb) || rc.equals(cc));
    }

    // {'a {'a <>}}
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newPath(nilPath));
    Code rc = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(rc));
    rc = Code.newProduct(m);

    assertEquals(rc, CodeUtils.reRoot(ca, cons(new Label("a"), nilPath)));
    assertEquals(rc, CodeUtils.reRoot(ca, cons(new Label("b"), nilPath)));
  }

  // {'a {'a <b>, 'b <>}, 'b {'a <a>}}
  public static void testCross2() {
    Map<Label, CodeOrPath> m = Map.empty();
    m =
        m.put(new Label("a"), CodeOrPath.newPath(cons(new Label("b"), nilPath)));
    m = m.put(new Label("b"), CodeOrPath.newPath(nilPath));
    Code ca1 = Code.newProduct(m);
    m = Map.empty();
    m =
        m.put(new Label("a"), CodeOrPath.newPath(cons(new Label("a"), nilPath)));
    Code ca2 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(ca1));
    m = m.put(new Label("b"), CodeOrPath.newCode(ca2));
    Code ca = Code.newProduct(m);

    // {'a {'a {'a <a>}, 'b <>}, 'b <a,a>}
    m = Map.empty();
    m =
        m.put(new Label("a"), CodeOrPath.newPath(cons(new Label("a"), nilPath)));
    Code cb1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(cb1));
    m = m.put(new Label("b"), CodeOrPath.newPath(nilPath));
    cb1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(cb1));
    m =
        m.put(
            new Label("b"),
            CodeOrPath.newPath(cons(new Label("a"),
                cons(new Label("a"), nilPath))));
    Code cb = Code.newProduct(m);

    // {'a <b,a>, 'b {'a {'a <b>, 'b <>}}}
    m = Map.empty();
    m =
        m.put(new Label("a"), CodeOrPath.newPath(cons(new Label("b"), nilPath)));
    m = m.put(new Label("b"), CodeOrPath.newPath(nilPath));
    Code cc1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(cc1));
    cc1 = Code.newProduct(m);
    m = Map.empty();
    m =
        m.put(
            new Label("a"),
            CodeOrPath.newPath(cons(new Label("b"),
                cons(new Label("a"), nilPath))));
    m = m.put(new Label("b"), CodeOrPath.newCode(cc1));
    Code cc = Code.newProduct(m);

    for (Code c : new Code[] { ca, cb, cc }) {
      Code rc = CodeUtils.reRoot(c, nilPath);
      assertTrue(rc.equals(ca) || rc.equals(cb) || rc.equals(cc));
    }

    // {'a {'a <>},'b {'a <>, 'b <a>}}
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newPath(nilPath));
    Code cRa1_1 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newPath(nilPath));
    m =
        m.put(new Label("b"), CodeOrPath.newPath(cons(new Label("a"), nilPath)));
    Code cRa1_2 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(cRa1_1));
    m = m.put(new Label("b"), CodeOrPath.newCode(cRa1_2));
    Code cRa1 = Code.newProduct(m);

    // {'a <b,b>, 'b {'a <>, 'b {'a <>}}}
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newPath(nilPath));
    Code cRa2 = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newPath(nilPath));
    m = m.put(new Label("b"), CodeOrPath.newCode(cRa2));
    cRa2 = Code.newProduct(m);
    m = Map.empty();
    m =
        m.put(
            new Label("a"),
            CodeOrPath.newPath(cons(new Label("b"),
                cons(new Label("b"), nilPath))));
    m = m.put(new Label("b"), CodeOrPath.newCode(cRa2));
    cRa2 = Code.newProduct(m);

    for (Code c : new Code[] { ca, cb, cc }) {
      Code rc = CodeUtils.reRoot(c, cons(new Label("a"), nilPath));
      assertTrue(rc.equals(cRa1) || rc.equals(cRa2));
    }

    // {'a {'a <>, 'b {'a <a>,'b <>}}}
    m = Map.empty();
    m =
        m.put(new Label("a"), CodeOrPath.newPath(cons(new Label("a"), nilPath)));
    m = m.put(new Label("b"), CodeOrPath.newPath(nilPath));
    Code cRb = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newPath(nilPath));
    m = m.put(new Label("b"), CodeOrPath.newCode(cRb));
    cRb = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), CodeOrPath.newCode(cRb));
    cRb = Code.newProduct(m);

    for (Code c : new Code[] { ca, cb, cc }) {
      assertEquals(cRb, CodeUtils.reRoot(c, cons(new Label("b"), nilPath)));
    }
  }
}
