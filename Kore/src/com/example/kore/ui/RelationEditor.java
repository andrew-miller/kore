package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.adaptComposition;
import static com.example.kore.ui.RelationUtils.codomain;
import static com.example.kore.ui.RelationUtils.domain;
import static com.example.kore.ui.RelationUtils.linkTree;
import static com.example.kore.ui.RelationUtils.linkTreeToRelation;
import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.ui.RelationUtils.relationOrPathAt;
import static com.example.kore.ui.RelationUtils.replaceRelationOrPathAt;
import static com.example.kore.ui.RelationUtils.resolve;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.LinkTreeUtils.rebase;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.fromArray;
import static com.example.kore.utils.Null.notNull;
import static com.example.kore.utils.PairUtils.pair;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Composition;
import com.example.kore.codes.Relation.Tag;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

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

  private static Pair<View, F<Unit, Bundle>> make(final Context context,
      Relation relation, final List<Code> codes,
      final CodeLabelAliasMap codeLabelAliases,
      final Bijection<CanonicalCode, String> codeAliases,
      final Bijection<CanonicalRelation, String> relationAliases,
      final List<Relation> relations,
      final RelationViewColors relationViewColors,
      List<Either3<Label, Integer, Unit>> path, final F<Relation, Unit> done) {
    notNull(context, relation, done, relationViewColors);
    final View v =
        LayoutInflater.from(context).inflate(R.layout.relation_editor, null);
    final ViewGroup pathContainer =
        (ViewGroup) v.findViewById(R.id.container_path);

    final S s = new S();
    s.relation = relation;
    s.path = path;

    final F<List<Either3<Label, Integer, Unit>>, Unit> setPath =
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
                    Either<Relation, List<Either3<Label, Integer, Unit>>> rp =
                        relationOrPathAt(s.path, s.relation);
                    Relation r = resolve(s.relation, rp);
                    Relation r2 = resolve(s.relation, er);
                    s.relation =
                        !equal(domain(r), domain(r2))
                            | !equal(codomain(r), codomain(r2)) ? adaptComposition(
                            replaceRelationOrPathAt(
                                s.relation,
                                s.path,
                                x(Relation
                                    .composition(new Composition(
                                        fromArray(er.tag == er.tag.X ? x(linkTreeToRelation(rebase(
                                            append(Either3
                                                .<Label, Integer, Unit> y(0),
                                                s.path), linkTree(er.x()))))
                                            : er), domain(r), codomain(r))))),
                            s.path)
                            : replaceRelationOrPathAt(
                                s.relation,
                                s.path,
                                er.tag == er.tag.X ? x(linkTreeToRelation(rebase(
                                    s.path, linkTree(er.x())))) : er);
                    s.initNodeEditor.f(unit());
                    f(s.path);
                  }

                  public void changeRelationType(Tag t) {
                    s.relation =
                        RelationUtils.changeRelationType(s.relation, s.path, t);
                    f(s.path);
                    s.initNodeEditor.f(unit());
                  }
                }, s.relation, relations, codeLabelAliases, relationAliases, p,
                relationViewColors.referenceColors.x));
            return unit();
          }
        };

    s.initNodeEditor = new F<Unit, Unit>() {
      public Unit f(Unit _) {
        s.nodeEditor =
            RelationNodeEditor.make(
                context,
                s.relation,
                new RelationNodeEditor.Listener() {
                  public void selectRelation(
                      List<Either3<Label, Integer, Unit>> p) {
                    s.selectPath.f(append(s.path, p));
                  }

                  public void replaceRelation(
                      List<Either3<Label, Integer, Unit>> p, Relation r2) {
                    p = append(s.path, p);
                    Relation r = relationAt(p, s.relation).some().x;
                    s.relation = replaceRelationOrPathAt(s.relation, p, x(r2));
                    f(unit());
                    setPath.f(s.path);
                  }

                  public void done() {
                    done.f(s.relation);
                  }

                  public void extendUnion(
                      List<Either3<Label, Integer, Unit>> p, final Integer i,
                      Relation r2) {
                    s.relation =
                        RelationUtils.extendUnion(s.relation,
                            append(s.path, p), i, r2);
                    f(unit());
                    setPath.f(s.path);
                  }

                  public void extendComposition(
                      List<Either3<Label, Integer, Unit>> p, Integer i,
                      Relation r2) {
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

                  public void changeRelationType(
                      List<Either3<Label, Integer, Unit>> path, Tag t) {
                    s.relation =
                        RelationUtils.changeRelationType(s.relation, path, t);
                    setPath.f(s.path);
                    f(unit());
                  }
                }, s.path, codes, codeLabelAliases, codeAliases,
                relationAliases, relationViewColors);
        ViewGroup cont =
            (ViewGroup) v.findViewById(R.id.container_relation_editor);
        cont.removeAllViews();
        cont.addView(s.nodeEditor);
        return unit();
      }
    };

    s.selectPath = new F<List<Either3<Label, Integer, Unit>>, Unit>() {
      public Unit f(List<Either3<Label, Integer, Unit>> p) {
        notNull(p);
        relationOrPathAt(p, s.relation);
        s.path = p;
        setPath.f(p);
        s.initNodeEditor.f(unit());
        return unit();
      }
    };

    F<Unit, Bundle> getState = new F<Unit, Bundle>() {
      public Bundle f(Unit _) {
        Bundle b = new Bundle();
        b.putSerializable(STATE_PATH, s.path);
        b.putSerializable(STATE_RELATION, s.relation);
        return b;
      }
    };

    s.initNodeEditor.f(unit());
    setPath.f(path);
    return pair(v, getState);
  }

  public static Pair<View, F<Unit, Bundle>> make(Context context,
      Relation relation, final List<Code> codes,
      final CodeLabelAliasMap codeLabelAliases,
      final Bijection<CanonicalCode, String> codeAliases,
      final Bijection<CanonicalRelation, String> relationAliases,
      List<Relation> relations, RelationViewColors relationViewColors,
      F<Relation, Unit> done) {
    return make(context, relation, codes, codeLabelAliases, codeAliases,
        relationAliases, relations, relationViewColors,
        ListUtils.<Either3<Label, Integer, Unit>> nil(), done);
  }

  public static Pair<View, F<Unit, Bundle>> make(Context context,
      final List<Code> codes, final CodeLabelAliasMap codeLabelAliases,
      final Bijection<CanonicalCode, String> codeAliases,
      final Bijection<CanonicalRelation, String> relationAliases,
      List<Relation> relations, RelationViewColors relationViewColors,
      Bundle b, F<Relation, Unit> done) {
    return make(context, (Relation) b.getSerializable(STATE_RELATION), codes,
        codeLabelAliases, codeAliases, relationAliases, relations,
        relationViewColors,
        (List<Either3<Label, Integer, Unit>>) b.getSerializable(STATE_PATH),
        done);
  }

  private static Either<Relation, List<Either3<Label, Integer, Unit>>> x(
      Relation r) {
    return Either.x(r);
  }
}