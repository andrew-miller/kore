package com.example.kore.ui;

import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.fromArray;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Pair.pair;
import static com.example.kore.utils.Unit.unit;

import com.example.kore.codes.Code;
import com.example.kore.codes.Code.Tag;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.codes.RVertex;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Abstraction;
import com.example.kore.codes.Relation.Composition;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.List;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

/**
 * A directed cyclic graph represented by a spanning tree with links to nodes in
 * the spanning tree (represented by paths from the root)
 */
interface LinkTree<E, V> {
  List<Pair<E, Either<LinkTree<E, V>, List<E>>>> edges();

  V vertex();
}

class C {
  LinkTree<Label, Code.Tag> linkTree(final Code c) {
    return new LinkTree<Label, Code.Tag>() {
      public List<Pair<Label, Either<LinkTree<Label, Tag>, List<Label>>>>
          edges() {
        List<Pair<Label, Either<LinkTree<Label, Tag>, List<Label>>>> l = nil();
        for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet()))
          switch (e.v.tag) {
          case CODE:
            l =
                cons(
                    pair(
                        e.k,
                        Either
                            .<LinkTree<Label, Code.Tag>, List<Label>> x(linkTree(e.v.code))),
                    l);
            break;
          case PATH:
            l =
                cons(
                    pair(e.k, Either
                        .<LinkTree<Label, Code.Tag>, List<Label>> y(e.v.path)),
                    l);
            break;
          default:
            throw boom();
          }
        return l;
      }

      public Tag vertex() {
        return c.tag;
      }

    };
  }

  LinkTree<Either3<Label, Integer, Unit>, RVertex> linkTree(final Relation r) {
    return new LinkTree<Either3<Label, Integer, Unit>, RVertex>() {
      public
          List<Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>>>
          edges() {
        switch (r.tag) {
        case ABSTRACTION:
          Abstraction a = r.abstraction();
          return fromArray(pair(Either3.<Label, Integer, Unit> z(unit()),
              assballs(a.r)));
        case COMPOSITION: {
          List<Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>>> l =
              nil();
          int i = 0;
          Composition c = r.composition();
          for (Either<Relation, List<Either3<Label, Integer, Unit>>> r : iter(c.l))
            l =
                cons(pair(Either3.<Label, Integer, Unit> y(i++), assballs(r)),
                    l);
          return l;
        }
        case LABEL:
          return fromArray(pair(
              Either3.<Label, Integer, Unit> x(r.label().label),
              assballs(r.label().r)));
        case PRODUCT: {
          List<Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>>> l =
              nil();
          for (Entry<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(r
              .product().m.entrySet()))
            l =
                cons(
                    pair(Either3.<Label, Integer, Unit> x(e.k), assballs(e.v)),
                    l);
          return l;
        }
        case PROJECTION:
          return nil();
        case UNION:
          List<Pair<Either3<Label, Integer, Unit>, Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>>> l =
              nil();
          int i = 0;
          for (Either<Relation, List<Either3<Label, Integer, Unit>>> r_ : iter(r
              .union().l))
            l =
                cons(pair(Either3.<Label, Integer, Unit> y(i++), assballs(r_)),
                    l);
          return l;
        default:
          throw boom();
        }
      }

      private
          Either<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>>
          assballs(Either<Relation, List<Either3<Label, Integer, Unit>>> r) {
        return r.isY() ? Either
            .<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>> y(r
                .y())
            : Either
                .<LinkTree<Either3<Label, Integer, Unit>, RVertex>, List<Either3<Label, Integer, Unit>>> x(linkTree(r
                    .x()));
      }

      public RVertex vertex() {
        switch (r.tag) {
        case ABSTRACTION:
          return RVertex.abstraction(new RVertex.Abstraction(
              r.abstraction().pattern, r.abstraction().i, r.abstraction().o));
        case COMPOSITION:
          return RVertex.composition(new RVertex.Composition(r.composition().i,
              r.composition().o));
        case LABEL:
          return RVertex.label(new RVertex.Label(r.label().o));
        case PRODUCT:
          return RVertex.product(new RVertex.Product(r.product().o));
        case PROJECTION:
          return RVertex.projection(new RVertex.Projection(r
              .projection().path, r.projection().o));
        case UNION:
          return RVertex.union(new RVertex.Union(r.union().i, r.union().o));
        default:
          throw boom();
        }
      }
    };
  }

}
