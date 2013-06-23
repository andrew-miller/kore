package com.example.kore;

import static com.example.kore.utils.Pair.pair;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DirectedMultigraph;

import com.example.kore.codes.Code;
import com.example.kore.codes.Code.Tag;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.Identity;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Pair;

import junit.framework.TestCase;

public class CodeToGraphTest extends TestCase {
  public static void testCodeToGraphUnit() {
    Code c = CodeUtils.unit;
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        CodeUtils.codeToGraph(c);
    DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g = p.x;
    Identity<Tag> r = p.y;
    assertTrue(g.containsVertex(r));
    assertEquals(0, g.outgoingEdgesOf(r).size());
    assertEquals(0, g.incomingEdgesOf(r).size());
  }

  // {'a {}}
  public static void testCodeToGraphProd1() {
    Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>();
    m.put(new Label("a"), CodeOrPath.newCode(CodeUtils.unit));
    Code c = Code.newProduct(m);
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        CodeUtils.codeToGraph(c);
    DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g = p.x;
    Identity<Tag> r = p.y;
    assertTrue(g.containsVertex(r));
    assertEquals(1, g.edgesOf(r).size());
    assertEquals(r, g.getEdgeSource(pair(r, new Label("a"))));
    Identity<Tag> v = g.getEdgeTarget(pair(r, new Label("a")));
    assertEquals(0, g.outgoingEdgesOf(v).size());
    assertEquals(1, g.incomingEdgesOf(v).size());
  }

  // {'a {}, 'b, {}}
  public static void testCodeToGraphProd1_1() {
    Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>();
    m.put(new Label("a"), CodeOrPath.newCode(CodeUtils.unit));
    m.put(new Label("b"), CodeOrPath.newCode(CodeUtils.unit));
    Code c = Code.newProduct(m);
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        CodeUtils.codeToGraph(c);
    DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g = p.x;
    Identity<Tag> r = p.y;
    assertTrue(g.containsVertex(r));
    assertEquals(2, g.edgesOf(r).size());
    assertEquals(r, g.getEdgeSource(pair(r, new Label("a"))));
    Identity<Tag> v = g.getEdgeTarget(pair(r, new Label("a")));
    assertEquals(0, g.outgoingEdgesOf(v).size());
    assertEquals(1, g.incomingEdgesOf(v).size());
    assertEquals(r, g.getEdgeSource(pair(r, new Label("b"))));
    v = g.getEdgeTarget(pair(r, new Label("b")));
    assertEquals(0, g.outgoingEdgesOf(v).size());
    assertEquals(1, g.incomingEdgesOf(v).size());
  }

  // {'a <>}
  public static void testCodeToGraphProdLoop() {
    Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>();
    m.put(new Label("a"), CodeOrPath.newPath(ListUtils.<Label> nil()));
    Code c = Code.newProduct(m);
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        CodeUtils.codeToGraph(c);
    DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g = p.x;
    Identity<Tag> r = p.y;
    assertTrue(g.containsVertex(r));
    assertEquals(1, g.edgesOf(r).size());
    assertEquals(r, g.getEdgeSource(pair(r, new Label("a"))));
    assertEquals(r, g.getEdgeTarget(pair(r, new Label("a"))));
  }

  // {'a {'b <>}}
  public static void testCodeToGraphProdLoop2() {
    Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>();
    m.put(new Label("b"), CodeOrPath.newPath(ListUtils.<Label> nil()));
    Code c = Code.newProduct(m);
    m = new HashMap<Label, CodeOrPath>();
    m.put(new Label("a"), CodeOrPath.newCode(c));
    c = Code.newProduct(m);
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        CodeUtils.codeToGraph(c);
    DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g = p.x;
    Identity<Tag> r = p.y;
    assertTrue(g.containsVertex(r));
    assertEquals(2, g.edgesOf(r).size());
    assertEquals(r, g.getEdgeSource(pair(r, new Label("a"))));
    Identity<Tag> v = g.getEdgeTarget(pair(r, new Label("a")));
    assertEquals(2, g.edgesOf(v).size());
    assertEquals(v, g.getEdgeSource(pair(v, new Label("b"))));
    assertEquals(r, g.getEdgeTarget(pair(v, new Label("b"))));
  }

  // {'a {'b <>}, 'b <>}
  public static void testCodeToGraphProdLoop2_() {
    Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>();
    m.put(new Label("b"), CodeOrPath.newPath(ListUtils.<Label> nil()));
    Code c = Code.newProduct(m);
    m = new HashMap<Label, CodeOrPath>();
    m.put(new Label("a"), CodeOrPath.newCode(c));
    m.put(new Label("b"), CodeOrPath.newPath(ListUtils.<Label> nil()));
    c = Code.newProduct(m);
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        CodeUtils.codeToGraph(c);
    DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g = p.x;
    Identity<Tag> r = p.y;
    assertTrue(g.containsVertex(r));
    assertEquals(3, g.edgesOf(r).size());
    assertEquals(r, g.getEdgeSource(pair(r, new Label("a"))));
    Identity<Tag> v = g.getEdgeTarget(pair(r, new Label("a")));
    assertEquals(2, g.edgesOf(v).size());
    assertEquals(v, g.getEdgeSource(pair(v, new Label("b"))));
    assertEquals(r, g.getEdgeTarget(pair(v, new Label("b"))));
    assertEquals(r, g.getEdgeSource(pair(r, new Label("b"))));
  }

}
