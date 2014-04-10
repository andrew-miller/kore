package com.example.kore;

import static com.example.kore.utils.CodeUtils.directPath;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.fromArray;
import static com.example.kore.utils.ListUtils.nil;
import junit.framework.TestCase;

import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.Either;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;

public class DirectCodePathTest extends TestCase {
  private static final List<Label> nil = nil();

  // {'a <>, 'b, {}}
  public static void testNat() {
    Label a = new Label("a");
    Label b = new Label("b");
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m = m.put(a, Either.<Code, List<Label>> y(nil));
    m = m.put(b, Either.<Code, List<Label>> x(unit));
    Code c = Code.newProduct(m);
    assertEquals(nil, directPath(nil, c));
    assertEquals(nil, directPath(fromArray(a), c));
    assertEquals(nil, directPath(fromArray(a, a), c));
    assertEquals(fromArray(b), directPath(fromArray(b), c));
    assertEquals(fromArray(b), directPath(fromArray(a, b), c));
    assertEquals(fromArray(b), directPath(fromArray(a, a, b), c));
  }

  // {'a <b>, 'b, {}}
  public static void testSilly() {
    Label a = new Label("a");
    Label b = new Label("b");
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m = m.put(a, Either.<Code, List<Label>> y(fromArray(b)));
    m = m.put(b, Either.<Code, List<Label>> x(unit));
    Code c = Code.newProduct(m);
    assertEquals(nil, directPath(nil, c));
    assertEquals(fromArray(b), directPath(fromArray(a), c));
    assertEquals(fromArray(b), directPath(fromArray(b), c));
  }

}
