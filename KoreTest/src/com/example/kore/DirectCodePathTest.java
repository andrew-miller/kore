package com.example.kore;

import static com.example.kore.utils.CodeUtils.directPath;
import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.nil;
import junit.framework.TestCase;

import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;

public class DirectCodePathTest extends TestCase {
  private static final List<Label> nilPath = nil();

  // {'a <>, 'b, {}}
  public static void testNat() {
    Label a = new Label("a");
    Label b = new Label("b");
    Map<Label, CodeOrPath> m = Map.empty();
    m = m.put(a, CodeOrPath.newPath(nilPath));
    m = m.put(b, CodeOrPath.newCode(CodeUtils.unit));
    Code c = Code.newProduct(m);
    assertEquals(nilPath, directPath(nilPath, c));
    assertEquals(nilPath, directPath(cons(a, nilPath), c));
    assertEquals(nilPath, directPath(cons(a, cons(a, nilPath)), c));
    assertEquals(nilPath, directPath(cons(a, nilPath), c));
    assertEquals(cons(b, nilPath), directPath(cons(b, nilPath), c));
    assertEquals(cons(b, nilPath), directPath(cons(a, cons(b, nilPath)), c));
  }

}
