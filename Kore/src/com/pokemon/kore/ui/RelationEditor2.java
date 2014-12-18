package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.irelation;
import static com.pokemon.kore.ui.RelationUtils.relationOrPathAt;
import static com.pokemon.kore.ui.RelationUtils.resolve;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.fromArray;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Null.notNull;
import static com.pokemon.kore.utils.PairUtils.pair;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pokemon.kore.R;
import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.CanonicalRelation;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Code2;
import com.pokemon.kore.codes.IRelation;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.codes.Relation2;
import com.pokemon.kore.codes.Relation2.Link;
import com.pokemon.kore.ui.RelationUtils.Resolver;
import com.pokemon.kore.utils.CodeUtils;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public class RelationEditor2 {
  private static final String STATE_PATH = "path";
  private static final String STATE_RELATION = "relation";

  static class S {
    private View nodeEditor;
    private Relation2 relation;
    private List<Either3<Label, Integer, Unit>> path;
    public F<Unit, Unit> initNodeEditor;
    public F<List<Either3<Label, Integer, Unit>>, Unit> selectPath;
  }

  private static Pair<View, Pair<F<Unit, Bundle>, F<Unit, Relation2>>> make(
      Context context, Relation2 relation, List<Code2> codes,
      CodeLabelAliasMap2 codeLabelAliases,
      Bijection<Code2.Link, String> codeAliases,
      Bijection<Link, String> relationAliases, List<Relation2> relations,
      RelationViewColors2 relationViewColors,
      List<Either3<Label, Integer, Unit>> path, Resolver rr,
      CodeUtils.Resolver cr, F<Relation2, Unit> done) {
    notNull(context, relation, done, relationViewColors);
    View v =
        LayoutInflater.from(context).inflate(R.layout.relation_editor, null);
    ViewGroup pathContainer = (ViewGroup) v.findViewById(R.id.container_path);

    S s = new S();
    s.relation = relation;
    s.path = path;

    F<List<Either3<Label, Integer, Unit>>, Unit> setPath =
        new F<List<Either3<Label, Integer, Unit>>, Unit>() {
          public Unit f(List<Either3<Label, Integer, Unit>> p) {
            pathContainer.removeAllViews();
            pathContainer.addView(RelationPath2.make(
                context,
                relationViewColors.relationcolors,
                new RelationPath2.Listener() {
                  public void selectPath(List<Either3<Label, Integer, Unit>> p) {
                    s.selectPath.f(p);
                  }

                  public
                      void
                      replaceRelation(
                          Either<Relation2, List<Either3<Label, Integer, Unit>>> er) {
                    s.relation =
                        RelationUtils.replaceRelation(s.relation, s.path, er);
                    s.initNodeEditor.f(unit());
                    f(s.path);
                  }
                }, irelation(s.relation, rr, cr), relations, codeLabelAliases,
                relationAliases, p, relationViewColors.referenceColors.x,
                relationViewColors, rr));
            return unit();
          }
        };

    s.initNodeEditor = new F<Unit, Unit>() {
      public Unit f(Unit $) {
        Pair<IRelation, Optional<Pair<IRelation, Either3<Label, Integer, Unit>>>> res =
            resolve(irelation(s.relation, rr, cr), s.path);
        s.nodeEditor =
            RelationNodeEditor2.make(
                context,
                res.y.isNothing() ? res.x : res.y.some().x.x,
                new RelationNodeEditor2.Listener() {
                  public void selectRelation(
                      List<Either3<Label, Integer, Unit>> p) {
                    s.selectPath.f(append(s.path, p));
                  }

                  public void done() {
                    done.f(s.relation);
                  }

                  public
                      void
                      extendUnion(
                          List<Either3<Label, Integer, Unit>> p,
                          Integer i,
                          Either<Relation2, List<Either3<Label, Integer, Unit>>> r2) {
                    s.relation =
                        extendUnion(s.relation, append(s.path, p), i, r2);
                    f(unit());
                    setPath.f(s.path);
                  }

                  public
                      void
                      extendComposition(
                          List<Either3<Label, Integer, Unit>> p,
                          Integer i,
                          Either<Relation2, List<Either3<Label, Integer, Unit>>> r2) {
                    s.relation =
                        extendComposition(s.relation, append(s.path, p), i, r2);
                    f(unit());
                    setPath.f(s.path);
                  }

                  public void changeDomain(Code2.Link d2) {
                    s.relation = changeDomain(s.relation, s.path, d2);
                    f(unit());
                    setPath.f(s.path);
                  }

                  public void changeCodomain(Code2.Link c2) {
                    s.relation = changeCodomain(s.relation, s.path, c2);
                    f(unit());
                    setPath.f(s.path);
                  }

                  public void selectPath(List<Either3<Label, Integer, Unit>> p) {
                    s.selectPath.f(relationOrPathAt(append(s.path, p),
                        s.relation).y());
                  }

                  public
                      void
                      replaceRelation(
                          List<Either3<Label, Integer, Unit>> p,
                          Either<Relation2, List<Either3<Label, Integer, Unit>>> er) {
                    s.relation =
                        RelationUtils.replaceRelation(s.relation,
                            append(s.path, p), er);
                    f(unit());
                    setPath.f(s.path);
                  }

                  public boolean canReplaceWithRef(
                      List<Either3<Label, Integer, Unit>> path) {
                    return !s.path.isEmpty();
                  }
                }, res.y.isNothing() ? nil() : fromArray(res.y.some().x.y),
                codes, codeLabelAliases, codeAliases, relationAliases,
                relationViewColors, relations, cr);
        ViewGroup cont =
            (ViewGroup) v.findViewById(R.id.container_relation_editor);
        cont.removeAllViews();
        cont.addView(s.nodeEditor);
        return unit();
      }
    };

    s.selectPath = p -> {
      notNull(p);
      relationOrPathAt(p, s.relation);
      s.path = p;
      setPath.f(p);
      s.initNodeEditor.f(unit());
      return unit();
    };

    F<Unit, Bundle> getState = $ -> {
      Bundle b = new Bundle();
      b.putSerializable(STATE_PATH, s.path);
      b.putSerializable(STATE_RELATION, s.relation);
      return b;
    };

    F<Unit, Relation2> getRelation = $ -> {
      return s.relation;
    };

    s.initNodeEditor.f(unit());
    setPath.f(path);
    return pair(v, pair(getState, getRelation));
  }

  public static Pair<View, Pair<F<Unit, Bundle>, F<Unit, Relation>>> make(
      Context context, Relation relation, List<Code> codes,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      List<Relation> relations, RelationViewColors relationViewColors,
      F<Relation, Unit> done) {
    return make(context, relation, codes, codeLabelAliases, codeAliases,
        relationAliases, relations, relationViewColors, nil(), done);
  }

  public static Pair<View, Pair<F<Unit, Bundle>, F<Unit, Relation>>> make(
      Context context, List<Code> codes, CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      List<Relation> relations, RelationViewColors relationViewColors,
      Bundle b, F<Relation, Unit> done) {
    return make(context, (Relation) b.getSerializable(STATE_RELATION), codes,
        codeLabelAliases, codeAliases, relationAliases, relations,
        relationViewColors,
        (List<Either3<Label, Integer, Unit>>) b.getSerializable(STATE_PATH),
        done);
  }
}