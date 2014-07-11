package com.example.kore.codes;

import static com.example.kore.ui.RelationUtils.dummy;
import static com.example.kore.ui.RelationUtils.resolve;
import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.CodeUtils.empty;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.OptionalUtils.nothing;
import static com.example.kore.utils.OptionalUtils.some;

import com.example.kore.codes.Relation.Label_;
import com.example.kore.codes.Relation.Product;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class ValueUtils {
  public static Value unitVal = new Value(Map.<Label, Value> empty(), unit);
  public static Value emptyVal = new Value(Map.<Label, Value> empty(), empty);

  public static Optional<Value> eval(Relation r) {
    return eval(r, r, emptyVal, unitVal);
  }

  public static Relation toRelation(Value v) {
    switch (v.code.tag) {
    case PRODUCT:
      Map<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> m =
          Map.empty();
      for (Pair<Label, Value> e : iter(v.val.entrySet()))
        m = m.put(e.x, x(toRelation(e.y)));
      return Relation.product(new Product(m, v.code));
    case UNION:
      List<Pair<Label, Value>> es = v.val.entrySet();
      if (es.isEmpty())
        return dummy(unit, v.code);
      Pair<Label, Value> x = es.cons().x;
      return Relation.label(new Label_(x.x, x(toRelation(x.y)), v.code));
    default:
      throw boom();
    }
  }

  private static Either<Relation, List<Either3<Label, Integer, Unit>>> x(
      Relation r) {
    return Either.x(r);
  }

  private static Optional<Value> eval(Relation root, Relation r, Value a,
      Value i) {
    switch (r.tag) {
    case ABSTRACTION:
      if (!match(r.abstraction().pattern, i))
        return nothing();
      return eval(root, resolve(root, r.abstraction().r), i, i);
    case COMPOSITION:
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> er : iter(r
          .composition().l)) {
        Optional<Value> ov = eval(root, resolve(root, er), a, i);
        if (ov.isNothing())
          return ov;
        i = ov.some().x;
      }
      return some(i);
    case LABEL: {
      Optional<Value> ov = eval(root, resolve(root, r.label().r), a, unitVal);
      if (ov.isNothing())
        return ov;
      return some(new Value(Map.<Label, Value> empty().put(r.label().label,
          ov.some().x), r.label().o));
    }
    case PRODUCT:
      Map<Label, Value> m = Map.empty();
      for (Pair<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(r
          .product().m.entrySet())) {
        Optional<Value> ov = eval(root, resolve(root, e.y), a, unitVal);
        if (ov.isNothing())
          return ov;
        m = m.put(e.x, ov.some().x);
      }
      return some(new Value(m, r.product().o));
    case PROJECTION:
      for (Label l : iter(r.projection().path)) {
        Optional<Value> ov = a.val.get(l);
        if (ov.isNothing())
          return ov;
        a = ov.some().x;
      }
      return some(a);
    case UNION:
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> er : iter(r
          .union().l)) {
        Optional<Value> ov = eval(root, resolve(root, er), a, i);
        if (!ov.isNothing())
          return ov;
      }
      return nothing();
    default:
      throw boom();
    }
  }

  public static boolean match(Pattern pattern, Value i) {
    for (Pair<Label, Pattern> e : iter(pattern.fields.entrySet())) {
      Optional<Value> ov = i.val.get(e.x);
      if (ov.isNothing())
        return false;
      if (!match(e.y, ov.some().x))
        return false;
    }
    return true;
  }
}
