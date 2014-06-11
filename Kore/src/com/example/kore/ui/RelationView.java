package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.enclosingAbstraction;
import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.length;
import static com.example.kore.utils.OptionalUtils.some;
import static com.example.kore.utils.PairUtils.pair;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;

import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Abstraction;
import com.example.kore.codes.Relation.Tag;
import com.example.kore.ui.DragDropEdges.Side;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Optional;
import com.example.kore.utils.OptionalUtils;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public final class RelationView {
  interface Listener {
    void extendComposition(List<Either3<Label, Integer, Unit>> path, Integer i);

    void extendUnion(List<Either3<Label, Integer, Unit>> path, Integer i);

    void select(List<Either3<Label, Integer, Unit>> path);

    void selectPath(List<Either3<Label, Integer, Unit>> path);

    void replaceRelation(List<Either3<Label, Integer, Unit>> path, Relation r);

    boolean dontAbbreviate(List<Either3<Label, Integer, Unit>> path);
  }

  public static View make(final Context context, final RelationViewColors rvc,
      final DragBro dragBro, final Relation root,
      final List<Either3<Label, Integer, Unit>> path, final Listener listener,
      final CodeLabelAliasMap codeLabelAliases,
      final Bijection<CanonicalRelation, String> relationAliases) {
    final Either<Relation, List<Either3<Label, Integer, Unit>>> er =
        RelationUtils.relationOrPathAt(path, root);
    View rv;
    Pair<Integer, Integer> cp;
    Optional<String> alias =
        relationAliases.xy.get(new CanonicalRelation(root, path));
    if (alias.isNothing() | listener.dontAbbreviate(path)) {
      switch (er.tag) {
      case Y:
        cp = rvc.referenceColors;
        rv =
            RelationRefView.make(context,
                OptionalUtils.<Pair<Integer, String>> nothing(),
                new F<Unit, Unit>() {
                  public Unit f(Unit _) {
                    listener.selectPath(path);
                    return unit();
                  }
                });
        break;
      case X:
        Optional<Abstraction> oea = enclosingAbstraction(path, root);
        Optional<Code> argCode =
            oea.isNothing() ? OptionalUtils.<Code> nothing()
                : some(oea.some().x.i);
        final Relation r = er.x();
        cp = rvc.relationcolors.m.get(r.tag).some().x;
        F<Either3<Label, Integer, Unit>, View> make =
            new F<Either3<Label, Integer, Unit>, View>() {
              public View f(Either3<Label, Integer, Unit> e) {
                return make(context, rvc, dragBro, root, append(e, path),
                    listener, codeLabelAliases, relationAliases);
              }
            };
        switch (r.tag) {
        case COMPOSITION:
          rv =
              CompositionView.make(context, make, dragBro, cp.x, cp.y, root,
                  path, rvc, codeLabelAliases, relationAliases,
                  new CompositionView.Listener() {
                    public void select() {
                      listener.select(path);
                    }

                    public void extend(Integer i) {
                      listener.extendComposition(path, i);
                    }

                    public void replace(Relation r) {
                      listener.replaceRelation(path, r);
                    }
                  });
          break;
        case UNION:
          rv =
              UnionView.make(context, make, dragBro, cp.x, cp.y, root, path,
                  rvc, relationAliases, codeLabelAliases,
                  new UnionView.Listener() {
                    public void select() {
                      listener.select(path);
                    }

                    public void insert(Integer i) {
                      listener.extendUnion(path, i);
                    }

                    public void replace(Relation r) {
                      listener.replaceRelation(path, r);
                    }
                  });
          break;
        case LABEL:
          rv =
              Label_View.make(context, make, cp.x, root, path,
                  rvc.aliasTextColor, codeLabelAliases, rvc, relationAliases,
                  new Label_View.Listener() {
                    public void replace(Relation r) {
                      listener.replaceRelation(path, r);
                    }
                  });
          break;
        case ABSTRACTION:
          rv =
              AbstractionView.make(context, make, cp.x, rvc.aliasTextColor,
                  root, path, codeLabelAliases, rvc, relationAliases,
                  new AbstractionView.Listener() {
                    public void replace(Relation r) {
                      listener.replaceRelation(path, r);
                    }
                  });
          break;
        case PRODUCT:
          rv =
              ProductView.make(context, make, cp.x, rvc.aliasTextColor, root,
                  path, codeLabelAliases, relationAliases, rvc,
                  new F<Relation, Unit>() {
                    public Unit f(Relation r) {
                      listener.replaceRelation(path, r);
                      return unit();
                    }
                  });
          break;
        case PROJECTION:
          rv =
              ProjectionView.make(context, cp.x, rvc.aliasTextColor, root,
                  path, codeLabelAliases, relationAliases, argCode.some().x,
                  rvc, new ProjectionView.Listener() {
                    public void replace(Relation r) {
                      listener.replaceRelation(path, r);
                    }
                  });
          break;
        default:
          throw boom();
        }
        break;
      default:
        throw boom();
      }
    } else {
      cp = rvc.referenceColors;
      rv =
          RelationRefView.make(context,
              some(pair(rvc.aliasTextColor, alias.some().x)),
              new F<Unit, Unit>() {
                public Unit f(Unit _) {
                  listener.select(path);
                  return unit();
                }
              });
    }
    return DragDropEdges.make(context, dragBro, rv, cp.x, cp.y,
        new F<Pair<Side, Object>, Unit>() {
          public Unit f(Pair<Side, Object> p) {
            if (p.y instanceof SelectRelation)
              listener.select(path);
            else
              switch (p.x) {
              case BOTTOM:
                listener.extendUnion(path, er.tag == er.tag.X
                    && er.x().tag == Tag.UNION ? length(er.x().union().l) : 1);
                break;
              case TOP:
                listener.extendUnion(path, 0);
                break;
              case LEFT:
                listener.extendComposition(path, 0);
                break;
              case RIGHT:
                listener.extendComposition(path, er.tag == er.tag.X
                    && er.x().tag == Tag.COMPOSITION ? length(er.x()
                    .composition().l) : 1);
                break;
              }
            return unit();
          }
        }, new F<Object, Boolean>() {
          public Boolean f(Object o) {
            return o instanceof SelectRelation | o instanceof ExtendRelation;
          }
        });
  }
}