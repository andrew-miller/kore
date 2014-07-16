package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.adaptComposition;
import static com.pokemon.kore.ui.RelationUtils.codomain;
import static com.pokemon.kore.ui.RelationUtils.domain;
import static com.pokemon.kore.ui.RelationUtils.linkTree;
import static com.pokemon.kore.ui.RelationUtils.linkTreeToRelation;
import static com.pokemon.kore.ui.RelationUtils.relationOrPathAt;
import static com.pokemon.kore.ui.RelationUtils.replaceRelationOrPathAt;
import static com.pokemon.kore.ui.RelationUtils.resolve;
import static com.pokemon.kore.utils.CodeUtils.equal;
import static com.pokemon.kore.utils.LinkTreeUtils.rebase;
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
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.codes.Relation.Composition;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public class RelationEditor {
  private static final String STATE_PATH = "path";
  private static final String STATE_RELATION = "relation";

  static class S {
    private View nodeEditor;
    private Relation relation;
    private List<Either3<Label, Integer, Unit>> path;
    public F<Unit, Unit> initNodeEditor;
    public F<List<Either3<Label, Integer, Unit>>, Unit> selectPath;
  }

  private static Relation replaceRelation(Relation relation,
      List<Either3<Label, Integer, Unit>> path,
      Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
    Either<Relation, List<Either3<Label, Integer, Unit>>> rp =
        relationOrPathAt(path, relation);
    Relation r = resolve(relation, rp);
    Relation r2 = resolve(relation, er);
    return !equal(domain(r), domain(r2)) | !equal(codomain(r), codomain(r2)) ? adaptComposition(
        replaceRelationOrPathAt(relation, path, Either.x(Relation
            .composition(new Composition(fromArray(er.tag == er.tag.X ? Either
                .x(linkTreeToRelation(rebase(append(Either3.y(0), path),
                    linkTree(er.x())))) : er), domain(r), codomain(r))))), path)
        : replaceRelationOrPathAt(
            relation,
            path,
            er.tag == er.tag.X ? Either.x(linkTreeToRelation(rebase(path,
                linkTree(er.x())))) : er);
  }

  private static Pair<View, F<Unit, Bundle>> make(Context context,
      Relation relation, List<Code> codes, CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      List<Relation> relations, RelationViewColors relationViewColors,
      List<Either3<Label, Integer, Unit>> path, F<Relation, Unit> done) {
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
            pathContainer.addView(RelationPath.make(
                context,
                relationViewColors.relationcolors,
                new RelationPath.Listener() {
                  public void selectPath(List<Either3<Label, Integer, Unit>> p) {
                    s.selectPath.f(p);
                  }

                  public void replaceRelation(
                      Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
                    s.relation =
                        RelationEditor.replaceRelation(s.relation, s.path, er);
                    s.initNodeEditor.f(unit());
                    f(s.path);
                  }
                }, s.relation, relations, codeLabelAliases, relationAliases, p,
                relationViewColors.referenceColors.x, relationViewColors));
            return unit();
          }
        };

    s.initNodeEditor = new F<Unit, Unit>() {
      public Unit f(Unit $) {
        s.nodeEditor =
            RelationNodeEditor.make(
                context,
                s.relation,
                new RelationNodeEditor.Listener() {
                  public void selectRelation(
                      List<Either3<Label, Integer, Unit>> p) {
                    s.selectPath.f(append(s.path, p));
                  }

                  public void done() {
                    done.f(s.relation);
                  }

                  public void extendUnion(
                      List<Either3<Label, Integer, Unit>> p, Integer i,
                      Either<Relation, List<Either3<Label, Integer, Unit>>> r2) {
                    s.relation =
                        RelationUtils.extendUnion(s.relation,
                            append(s.path, p), i, r2);
                    f(unit());
                    setPath.f(s.path);
                  }

                  public void extendComposition(
                      List<Either3<Label, Integer, Unit>> p, Integer i,
                      Either<Relation, List<Either3<Label, Integer, Unit>>> r2) {
                    s.relation =
                        RelationUtils.extendComposition(s.relation,
                            append(s.path, p), i, r2);
                    f(unit());
                    setPath.f(s.path);
                  }

                  public void changeDomain(Code d2) {
                    s.relation =
                        RelationUtils.changeDomain(s.relation, s.path, d2);
                    f(unit());
                    setPath.f(s.path);
                  }

                  public void changeCodomain(Code c2) {
                    s.relation =
                        RelationUtils.changeCodomain(s.relation, s.path, c2);
                    f(unit());
                    setPath.f(s.path);
                  }

                  public void selectPath(List<Either3<Label, Integer, Unit>> p) {
                    s.selectPath.f(relationOrPathAt(append(s.path, p),
                        s.relation).y());
                  }

                  public void replaceRelation(
                      List<Either3<Label, Integer, Unit>> p,
                      Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
                    s.relation =
                        RelationEditor.replaceRelation(s.relation,
                            append(s.path, p), er);
                    f(unit());
                    setPath.f(s.path);
                  }
                }, s.path, codes, codeLabelAliases, codeAliases,
                relationAliases, relationViewColors, relations);
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

    s.initNodeEditor.f(unit());
    setPath.f(path);
    return pair(v, getState);
  }

  public static Pair<View, F<Unit, Bundle>> make(Context context,
      Relation relation, List<Code> codes, CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      List<Relation> relations, RelationViewColors relationViewColors,
      F<Relation, Unit> done) {
    return make(context, relation, codes, codeLabelAliases, codeAliases,
        relationAliases, relations, relationViewColors, nil(), done);
  }

  public static Pair<View, F<Unit, Bundle>> make(Context context,
      List<Code> codes, CodeLabelAliasMap codeLabelAliases,
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