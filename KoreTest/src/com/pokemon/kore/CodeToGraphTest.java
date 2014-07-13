package com.pokemon.kore;

import static com.pokemon.kore.utils.CodeUtils.codeToGraph;
import static com.pokemon.kore.utils.CodeUtils.unit;
import static com.pokemon.kore.utils.PairUtils.pair;
import junit.framework.TestCase;

import org.jgrapht.graph.DirectedMultigraph;

import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Code.Tag;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Identity;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.ListUtils;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Pair;

public class CodeToGraphTest extends TestCase {
  public static void testCodeToGraphUnit() {
    Code c = unit;
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        codeToGraph(c);
    DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g = p.x;
    Identity<Tag> r = p.y;
    assertTrue(g.containsVertex(r));
    assertEquals(0, g.outgoingEdgesOf(r).size());
    assertEquals(0, g.incomingEdgesOf(r).size());
  }

  // {'a {}}
  public static void testCodeToGraphProd1() {
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(unit));
    Code c = Code.newProduct(m);
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        codeToGraph(c);
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
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(unit));
    m = m.put(new Label("b"), Either.<Code, List<Label>> x(unit));
    Code c = Code.newProduct(m);
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        codeToGraph(c);
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
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m =
        m.put(new Label("a"),
            Either.<Code, List<Label>> y(ListUtils.<Label> nil()));
    Code c = Code.newProduct(m);
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        codeToGraph(c);
    DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>> g = p.x;
    Identity<Tag> r = p.y;
    assertTrue(g.containsVertex(r));
    assertEquals(1, g.edgesOf(r).size());
    assertEquals(r, g.getEdgeSource(pair(r, new Label("a"))));
    assertEquals(r, g.getEdgeTarget(pair(r, new Label("a"))));
  }

  // {'a {'b <>}}
  public static void testCodeToGraphProdLoop2() {
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m =
        m.put(new Label("b"),
            Either.<Code, List<Label>> y(ListUtils.<Label> nil()));
    Code c = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(c));
    c = Code.newProduct(m);
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        codeToGraph(c);
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
    Map<Label, Either<Code, List<Label>>> m = Map.empty();
    m =
        m.put(new Label("b"),
            Either.<Code, List<Label>> y(ListUtils.<Label> nil()));
    Code c = Code.newProduct(m);
    m = Map.empty();
    m = m.put(new Label("a"), Either.<Code, List<Label>> x(c));
    m =
        m.put(new Label("b"),
            Either.<Code, List<Label>> y(ListUtils.<Label> nil()));
    c = Code.newProduct(m);
    Pair<DirectedMultigraph<Identity<Tag>, Pair<Identity<Tag>, Label>>, Identity<Tag>> p =
        codeToGraph(c);
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
