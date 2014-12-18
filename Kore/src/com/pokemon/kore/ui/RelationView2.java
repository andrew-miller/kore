package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.child;
import static com.pokemon.kore.ui.RelationUtils.codomain;
import static com.pokemon.kore.ui.RelationUtils.defaultValue2;
import static com.pokemon.kore.ui.RelationUtils.enclosingAbstraction;
import static com.pokemon.kore.ui.RelationUtils.hashLink;
import static com.pokemon.kore.ui.RelationUtils.relationAt;
import static com.pokemon.kore.ui.RelationUtils.subRelationOrPath;
import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.CodeUtils.child;
import static com.pokemon.kore.utils.CodeUtils.equal;
import static com.pokemon.kore.utils.CodeUtils.hashLink;
import static com.pokemon.kore.utils.CodeUtils.iunit;
import static com.pokemon.kore.utils.ListUtils.append;
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

import com.pokemon.kore.codes.IRelation;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation2;
import com.pokemon.kore.codes.Relation2.Label_;
import com.pokemon.kore.codes.Relation2.Link;
import com.pokemon.kore.codes.Relation2.Tag;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.ICode;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.SARef;
import com.pokemon.kore.utils.Unit;

public final class RelationView2 {
  interface Listener {
    void extendComposition(List<Either3<Label, Integer, Unit>> path, Integer i,
        Either<Relation2, List<Either3<Label, Integer, Unit>>> er);

    void extendUnion(List<Either3<Label, Integer, Unit>> path, Integer i,
        Either<Relation2, List<Either3<Label, Integer, Unit>>> er);

    void select(List<Either3<Label, Integer, Unit>> path);

    void selectPath(List<Either3<Label, Integer, Unit>> path);

    void replaceRelation(List<Either3<Label, Integer, Unit>> path,
        Either<Relation2, List<Either3<Label, Integer, Unit>>> rp);

    boolean dontAbbreviate(List<Either3<Label, Integer, Unit>> path);

    boolean canReplaceWithRef(List<Either3<Label, Integer, Unit>> path);
  }

  public static
      View
      make(
          Context context,
          RelationViewColors2 rvc,
          DragBro dragBro,
          Either<IRelation, Pair<Optional<String>, List<Either3<Label, Integer, Unit>>>> rap,
          Listener listener, CodeLabelAliasMap2 codeLabelAliases,
          Bijection<Link, String> relationAliases, List<Relation2> relations) {
    return make(context, rvc, dragBro, rap, listener, codeLabelAliases,
        relationAliases, relations, nil());
  }

  private static
      View
      make(
          Context context,
          RelationViewColors2 rvc,
          DragBro dragBro,
          Either<IRelation, Pair<Optional<String>, List<Either3<Label, Integer, Unit>>>> rap,
          Listener listener, CodeLabelAliasMap2 codeLabelAliases,
          Bijection<Link, String> relationAliases, List<Relation2> relations,
          List<Either3<Label, Integer, Unit>> path) {
    View rv;
    Pair<Integer, Integer> cp;
    F<Pair<Pair<Boolean, View>, F<Either<Relation2, List<Either3<Label, Integer, Unit>>>, Unit>>, Pair<PopupWindow, ViewGroup>> makeMenu =
        x -> RelationMenu2.make(context, rap.x(), x.x.y, rvc, codeLabelAliases,
            relationAliases, relations, x.x.x, x.y);
    F<View, Pair<PopupWindow, ViewGroup>> makeReplacementMenu =
        v -> makeMenu.f(pair(pair(listener.canReplaceWithRef(path), v),
            rpl -> {
              listener.replaceRelation(path, rpl);
              return unit();
            }));
    Optional<String> alias = rap.tag == Either.Tag.Y ? rap.y().x : nothing();
    if (alias.isNothing() | listener.dontAbbreviate(path)) {
      switch (rap.tag) {
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
        Optional<IRelation.IR.Abstraction> oea = enclosingAbstraction(rap.x());
        Optional<ICode> argCode =
            oea.isNothing() ? nothing() : some(oea.some().x.i);
        IRelation r = rap.x();
        cp = rvc.relationcolors.m.get(r.ir.tag).some().x;
        F<Either3<Label, Integer, Unit>, View> make =
            e -> {
              Either<IRelation, Pair<Optional<String>, List<Either3<Label, Integer, Unit>>>> rap2 =
                  null;
              Either<IRelation, List<Either3<Label, Integer, Unit>>> srp =
                  subRelationOrPath(r, e).some().x;
              switch (srp.tag) {
              case X:
                rap2 = Either.x(srp.x());
                break;
              case Y:
                Optional<String> oa =
                    relationAliases.xy
                        .get(hashLink(r.relationAt.f(srp.y()).link));
                rap2 = Either.y(pair(oa, srp.y()));
                break;
              default:
                throw boom();
              }
              return make(context, rvc, dragBro, rap2, listener,
                  codeLabelAliases, relationAliases, relations, append(e, path));
            };
        switch (r.ir.tag) {
        case COMPOSITION: {
          SARef<View> rvr = new SARef<>();
          rvr.set(rv =
              CompositionView2.make(context, make, dragBro, cp.x, cp.y, r,
                  new CompositionView2.Listener() {
                    public void select2() {
                      listener.select(path);
                    }

                    public void extend(Integer i) {
                      SARef<Pair<PopupWindow, ViewGroup>> p = new SARef<>();
                      F<Either<Relation2, List<Either3<Label, Integer, Unit>>>, Unit> f =
                          er -> {
                            p.get().x.dismiss();
                            listener.extendComposition(path, i, er);
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
              UnionView2.make(context, make, dragBro, cp.x, cp.y, r,
                  new UnionView2.Listener() {
                    public void select2() {
                      listener.select(path);
                    }

                    public void insert(Integer i) {
                      SARef<Pair<PopupWindow, ViewGroup>> p = new SARef<>();
                      F<Either<Relation2, List<Either3<Label, Integer, Unit>>>, Unit> f =
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
              Label_View2
                  .make(
                      context,
                      make,
                      cp.x,
                      r,
                      rvc.aliasTextColor,
                      rvc.labelTextColor,
                      codeLabelAliases,
                      v -> {
                        Pair<PopupWindow, ViewGroup> p =
                            makeReplacementMenu.f(v);
                        List<View> labels =
                            UIUtils.relationLabels(
                                p.y,
                                context,
                                v,
                                codeLabelAliases,
                                r.ir.label().o,
                                l -> {
                                  p.x.dismiss();
                                  Either<IRelation, List<Either3<Label, Integer, Unit>>> srp =
                                      subRelationOrPath(r, Either3.z(unit()))
                                          .some().x;
                                  IRelation sr = child(r, Either3.z(unit()));
                                  Either3<Relation2, List<Either3<Label, Integer, Unit>>, Link> srpl;
                                  switch (srp.tag) {
                                  case X:
                                    Pair<Relation2, List<Either3<Label, Integer, Unit>>> sl =
                                        srp.x().link;
                                    srpl =
                                        sl.x.equals(r.link.x) ? Either3
                                            .x(relationAt(sl.y, sl.x).some().x)
                                            : Either3.z(hashLink(sl));
                                    break;
                                  case Y:
                                    srpl = Either3.y(srp.y());
                                    break;
                                  default:
                                    throw boom();
                                  }
                                  ICode o = child(r.ir.label().o, l);
                                  listener.replaceRelation(path, Either
                                      .x(Relation2.label(new Label_(l, equal(
                                          codomain(sr), o) ? srpl : Either3
                                          .x(defaultValue2(iunit, o)),
                                          hashLink(r.ir.label().o.link())))));
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
              AbstractionView2.make(context, make, cp.x, rvc.aliasTextColor,
                  rvc.labelTextColor, r, codeLabelAliases,
                  new AbstractionView2.Listener() {
                    public void replace(Relation2 r) {
                      listener.replaceRelation(path, Either.x(r));
                    }

                    public void select(View v) {
                      makeReplacementMenu.f(v);
                    }
                  });
          break;
        case PRODUCT:
          rv =
              ProductView2.make(context, make, cp.x, rvc.aliasTextColor,
                  rvc.labelTextColor, r, codeLabelAliases, v -> {
                    makeReplacementMenu.f(v);
                    return unit();
                  });
          break;
        case PROJECTION:
          rv =
              ProjectionView2.make(context, cp.x, rvc.aliasTextColor,
                  rvc.labelTextColor, r, codeLabelAliases, argCode.some().x,
                  v -> {
                    Pair<PopupWindow, ViewGroup> p = makeReplacementMenu.f(v);
                    UIUtils.addProjectionsToMenu(p, context, v,
                        codeLabelAliases, argCode.some().x,
                        r.ir.projection().o, proj -> {
                          p.x.dismiss();
                          listener.replaceRelation(path, Either.x(Relation2
                              .projection(new Relation2.Projection(proj,
                                  hashLink(r.ir.projection().o.link())))));
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
                  F<Either<Relation2, List<Either3<Label, Integer, Unit>>>, Unit> f =
                      x -> {
                        p2.get().x.dismiss();
                        switch (p.x) {
                        case BOTTOM:
                          listener.extendUnion(
                              path,
                              rap.tag == rap.tag.X
                                  && rap.x().ir.tag == Tag.UNION ? length(rap
                                  .x().ir.union().l.f(unit())) : 1, x);
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
                  F<Either<Relation2, List<Either3<Label, Integer, Unit>>>, Unit> f =
                      x -> {
                        p2.get().x.dismiss();
                        switch (p.x) {
                        case LEFT:
                          listener.extendComposition(path, 0, x);
                          break;
                        case RIGHT:
                          listener.extendComposition(
                              path,
                              rap.tag == rap.tag.X
                                  && rap.x().ir.tag == Tag.COMPOSITION ? length(rap
                                  .x().ir.composition().l.f(unit())) : 1, x);
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