package com.pokemon.kore;

import static com.pokemon.kore.utils.CodeUtils.directPath;
import static com.pokemon.kore.utils.CodeUtils.unit;
import static com.pokemon.kore.utils.ListUtils.fromArray;
import static com.pokemon.kore.utils.ListUtils.nil;
import junit.framework.TestCase;

import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;

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
