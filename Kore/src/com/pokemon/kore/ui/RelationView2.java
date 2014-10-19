package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.codomain;
import static com.pokemon.kore.ui.RelationUtils.defaultValue;
import static com.pokemon.kore.ui.RelationUtils.enclosingAbstraction;
import static com.pokemon.kore.ui.RelationUtils.getRelation;
import static com.pokemon.kore.ui.RelationUtils.linkTree;
import static com.pokemon.kore.ui.RelationUtils.linkTreeToRelation;
import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.CodeUtils.equal;
import static com.pokemon.kore.utils.CodeUtils.reroot;
import static com.pokemon.kore.utils.CodeUtils.unit;
import static com.pokemon.kore.utils.LinkTreeUtils.rebase;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.fromArray;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.length;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.OptionalUtils.some;
import static com.pokemon.kore.utils.PairUtils.pair;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.CanonicalRelation;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.codes.Relation.Abstraction;
import com.pokemon.kore.codes.Relation.Label_;
import com.pokemon.kore.codes.Relation.Tag;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.SARef;
import com.pokemon.kore.utils.Unit;

public final class RelationView2 {
  interface Listener {
    void extendComposition(List<Either3<Label, Integer, Unit>> path, Integer i,
        Either<Relation, List<Either3<Label, Integer, Unit>>> er);

    void extendUnion(List<Either3<Label, Integer, Unit>> path, Integer i,
        Either<Relation, List<Either3<Label, Integer, Unit>>> er);

    void select(List<Either3<Label, Integer, Unit>> path);

    void selectPath(List<Either3<Label, Integer, Unit>> path);

    void replaceRelation(List<Either3<Label, Integer, Unit>> path,
        Either<Relation, List<Either3<Label, Integer, Unit>>> er);

    boolean dontAbbreviate(List<Either3<Label, Integer, Unit>> path);
  }

  public static View make(Context context, RelationViewColors rvc,
      DragBro dragBro, Relation root, List<Either3<Label, Integer, Unit>> path,
      Listener listener, CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      List<Relation> relations) {
    Either<Relation, List<Either3<Label, Integer, Unit>>> er =
        RelationUtils.relationOrPathAt(path, root);
    View rv;
    Pair<Integer, Integer> cp;
    Optional<String> alias =
        relationAliases.xy.get(new CanonicalRelation(root, path));
    F<Pair<Pair<Boolean, View>, F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Unit>>, Pair<PopupWindow, ViewGroup>> makeMenu =
        x -> RelationMenu.make(context, root, path, x.x.y, rvc,
            codeLabelAliases, relationAliases, relations, x.x.x, x.y);
    F<View, Pair<PopupWindow, ViewGroup>> makeReplacementMenu =
        v -> makeMenu.f(pair(pair(!path.isEmpty(), v), $er -> {
          listener.replaceRelation(
              path,
              $er.tag == $er.tag.X ? Either.x(linkTreeToRelation(rebase(path,
                  linkTree($er.x())))) : $er);
          return unit();
        }));
    if (alias.isNothing() | listener.dontAbbreviate(path)) {
      switch (er.tag) {
      case Y:
        cp = rvc.referenceColors;
        rv =
            RelationRefView.make(context, rvc.referenceColors.x, nothing(),
                $ -> {
                  listener.selectPath(path);
                  return unit();
                });
        break;
      case X:
        Optional<Abstraction> oea = enclosingAbstraction(path, root);
        Optional<Code> argCode =
            oea.isNothing() ? nothing() : some(oea.some().x.i);
        Relation r = er.x();
        cp = rvc.relationcolors.m.get(r.tag).some().x;
        F<Either3<Label, Integer, Unit>, View> make =
            e -> make(context, rvc, dragBro, root, append(e, path), listener,
                codeLabelAliases, relationAliases, relations);
        switch (r.tag) {
        case COMPOSITION: {
          SARef<View> rvr = new SARef<>();
          rvr.set(rv =
              CompositionView.make(context, make, dragBro, cp.x, cp.y, root,
                  path, new CompositionView.Listener() {
                    public void select2() {
                      listener.select(path);
                    }

                    public void extend(Integer i) {
                      SARef<Pair<PopupWindow, ViewGroup>> p = new SARef<>();
                      F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Unit> f =
                          $er -> {
                            p.get().x.dismiss();
                            listener.extendComposition(path, i, $er);
                            return unit();
                          };
                      p.set(makeMenu.f(pair(pair(true, rvr.get()), f)));
                    }

                    public void select(View v) {
                      makeReplacementMenu.f(v);
                    }
                  }));
          break;
        }
        case UNION:
          SARef<View> rvr = new SARef<>();
          rvr.set(rv =
              UnionView.make(context, make, dragBro, cp.x, cp.y, root, path,
                  new UnionView.Listener() {
                    public void select2() {
                      listener.select(path);
                    }

                    public void insert(Integer i) {
                      SARef<Pair<PopupWindow, ViewGroup>> p = new SARef<>();
                      F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Unit> f =
                          $er -> {
                            p.get().x.dismiss();
                            listener.extendUnion(path, i, $er);
                            return unit();
                          };
                      p.set(makeMenu.f(pair(pair(true, rvr.get()), f)));
                    }

                    public void select(View v) {
                      makeReplacementMenu.f(v);
                    }
                  }));
          break;
        case LABEL:
          rv =
              Label_View.make(
                  context,
                  make,
                  cp.x,
                  root,
                  path,
                  rvc.aliasTextColor,
                  rvc.labelTextColor,
                  codeLabelAliases,
                  v -> {
                    Pair<PopupWindow, ViewGroup> p = makeReplacementMenu.f(v);
                    List<View> labels =
                        UIUtils.relationLabels(
                            p.y,
                            context,
                            v,
                            codeLabelAliases,
                            new CanonicalCode(r.label().o, nil()),
                            l -> {
                              p.x.dismiss();
                              Relation sr =
                                  getRelation(root, r, Either3.z(unit()))
                                      .some().x;
                              Code o = reroot(r.label().o, fromArray(l));
                              listener.replaceRelation(path, Either.x(Relation
                                  .label(new Label_(l, Either.x(equal(
                                      codomain(sr), o) ? sr : defaultValue(
                                      unit, o)), r.label().o))));
                              return unit();
                            });
                    int i = 0;
                    for (View lv : iter(labels))
                      p.y.addView(lv, i++);
                    return unit();
                  });
          break;
        case ABSTRACTION:
          rv =
              AbstractionView.make(context, make, cp.x, rvc.aliasTextColor,
                  rvc.labelTextColor, root, path, codeLabelAliases,
                  new AbstractionView.Listener() {
                    public void replace(Relation r) {
                      listener.replaceRelation(path, Either.x(r));
                    }

                    public void select(View v) {
                      makeReplacementMenu.f(v);
                    }
                  });
          break;
        case PRODUCT:
          rv =
              ProductView.make(context, make, cp.x, rvc.aliasTextColor,
                  rvc.labelTextColor, root, path, codeLabelAliases, v -> {
                    makeReplacementMenu.f(v);
                    return unit();
                  });
          break;
        case PROJECTION:
          rv =
              ProjectionView.make(context, cp.x, rvc.aliasTextColor,
                  rvc.labelTextColor, root, path, codeLabelAliases, argCode
                      .some().x, v -> {
                    Pair<PopupWindow, ViewGroup> p = makeReplacementMenu.f(v);
                    UIUtils.addProjectionsToMenu(p, context, v,
                        codeLabelAliases, argCode.some().x,
                        reroot(argCode.some().x, r.projection().path),
                        proj -> {
                          p.x.dismiss();
                          listener.replaceRelation(path, Either.x(Relation
                              .projection(new Relation.Projection(proj, r
                                  .projection().o))));
                          return unit();
                        });
                    return unit();
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
          RelationRefView.make(context, rvc.referenceColors.x,
              some(pair(rvc.aliasTextColor, alias.some().x)), $ -> {
                listener.select(path);
                return unit();
              });
    }
    return DragDropEdges
        .make(
            context,
            dragBro,
            rv,
            cp.x,
            cp.y,
            p -> {
              if (p.y instanceof SelectRelation)
                listener.select(path);
              else
                switch (p.x) {
                case BOTTOM:
                case TOP: {
                  SARef<Pair<PopupWindow, ViewGroup>> p2 = new SARef<>();
                  F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Unit> f =
                      x -> {
                        p2.get().x.dismiss();
                        switch (p.x) {
                        case BOTTOM:
                          listener.extendUnion(
                              path,
                              er.tag == er.tag.X && er.x().tag == Tag.UNION ? length(er
                                  .x().union().l) : 1, x);
                          break;
                        case TOP:
                          listener.extendUnion(path, 0, x);
                          break;
                        }
                        return unit();
                      };
                  p2.set(makeMenu.f(pair(pair(true, rv), f)));
                  break;
                }
                case LEFT:
                case RIGHT:
                  SARef<Pair<PopupWindow, ViewGroup>> p2 = new SARef<>();
                  F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Unit> f =
                      x -> {
                        p2.get().x.dismiss();
                        switch (p.x) {
                        case LEFT:
                          listener.extendComposition(path, 0, x);
                          break;
                        case RIGHT:
                          listener.extendComposition(path, er.tag == er.tag.X
                              && er.x().tag == Tag.COMPOSITION ? length(er.x()
                              .composition().l) : 1, x);
                          break;
                        }
                        return unit();
                      };
                  p2.set(makeMenu.f(pair(pair(true, rv), f)));
                  break;
                }
              return unit();
            }, o -> o instanceof SelectRelation | o instanceof ExtendRelation);
  }
}