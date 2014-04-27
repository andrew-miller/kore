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
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
import com.example.kore.utils.Map;
import com.example.kore.utils.Unit;

public class RelationEditor extends FrameLayout {
  private static final String STATE_PATH = "path";
  private static final String STATE_RELATION = "relation";

  private RelationNodeEditor nodeEditor;
  private Relation relation;
  private List<Either3<Label, Integer, Unit>> path;
  private final F<Relation, Unit> done;
  private final Context context;
  private final ViewGroup pathContainer;
  private final List<Code> codes;
  private final CodeLabelAliasMap codeLabelAliases;
  private final Map<CanonicalCode, String> codeAliases;
  private final Map<CanonicalRelation, String> relationAliases;
  private final RelationViewColors relationViewColors;
  private final List<Relation> relations;

  private RelationEditor(Context context, Relation relation,
      final List<Code> codes, final CodeLabelAliasMap codeLabelAliases,
      final Map<CanonicalCode, String> codeAliases,
      final Map<CanonicalRelation, String> relationAliases,
      List<Relation> relations, RelationViewColors relationViewColors,
      List<Either3<Label, Integer, Unit>> path, F<Relation, Unit> done) {
    super(context);
    notNull(relation, done, relationViewColors);
    this.path = path;
    this.context = context;
    this.relation = relation;
    this.done = done;
    this.codes = codes;
    this.codeLabelAliases = codeLabelAliases;
    this.codeAliases = codeAliases;
    this.relationAliases = relationAliases;
    this.relationViewColors = relationViewColors;
    this.relations = relations;
    LayoutInflater.from(context).inflate(R.layout.relation_editor, this, true);
    pathContainer = (ViewGroup) findViewById(R.id.container_path);
    initNodeEditor();
    setPath(path);
  }

  public RelationEditor(Context context, Relation relation,
      final List<Code> codes, final CodeLabelAliasMap codeLabelAliases,
      final Map<CanonicalCode, String> codeAliases,
      final Map<CanonicalRelation, String> relationAliases,
      List<Relation> relations, RelationViewColors relationViewColors,
      F<Relation, Unit> done) {
    this(context, relation, codes, codeLabelAliases, codeAliases,
        relationAliases, relations, relationViewColors, ListUtils
            .<Either3<Label, Integer, Unit>> nil(), done);
  }

  public RelationEditor(Context context, final List<Code> codes,
      final CodeLabelAliasMap codeLabelAliases,
      final Map<CanonicalCode, String> codeAliases,
      final Map<CanonicalRelation, String> relationAliases,
      List<Relation> relations, RelationViewColors relationViewColors,
      Bundle b, F<Relation, Unit> done) {
    this(context, (Relation) b.getSerializable(STATE_RELATION), codes,
        codeLabelAliases, codeAliases, relationAliases, relations,
        relationViewColors, (List<Either3<Label, Integer, Unit>>) b
            .getSerializable(STATE_PATH), done);
  }

  private void setPath(List<Either3<Label, Integer, Unit>> p) {
    pathContainer.removeAllViews();
    pathContainer.addView(RelationPath.make(
        context,
        relationViewColors.relationcolors,
        new RelationPath.Listener() {
          public void selectPath(List<Either3<Label, Integer, Unit>> p) {
            RelationEditor.this.selectPath(p);
          }

          public void replaceRelation(
              Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
            Either<Relation, List<Either3<Label, Integer, Unit>>> rp =
                relationOrPathAt(path, relation);
            Relation r = resolve(relation, rp);
            Relation r2 = resolve(relation, er);
            relation =
                !equal(domain(r), domain(r2))
                    | !equal(codomain(r), codomain(r2)) ? adaptComposition(
                    replaceRelationOrPathAt(
                        relation,
                        path,
                        x(Relation
                            .composition(new Composition(
                                fromArray(er.tag == er.tag.X ? x(linkTreeToRelation(rebase(
                                    append(Either3.<Label, Integer, Unit> y(0),
                                        path), linkTree(er.x())))) : er),
                                domain(r), codomain(r))))), path)
                    : replaceRelationOrPathAt(
                        relation,
                        path,
                        er.tag == er.tag.X ? x(linkTreeToRelation(rebase(path,
                            linkTree(er.x())))) : er);
            initNodeEditor();
            setPath(path);
          }

          public void changeRelationType(Tag t) {
            relation = RelationUtils.changeRelationType(relation, path, t);
            setPath(path);
            initNodeEditor();
          }
        }, relation, relations, codeLabelAliases, relationAliases, p,
        relationViewColors.referenceColors.x));
  }

  public void changeRelationType(Relation.Tag t) {
    relation = RelationUtils.changeRelationType(relation, path, t);
    setPath(path);
    initNodeEditor();
  }

  private void selectPath(List<Either3<Label, Integer, Unit>> p) {
    notNull(p);
    relationOrPathAt(p, relation);
    path = p;
    setPath(p);
    initNodeEditor();
  }

  private void initNodeEditor() {
    nodeEditor =
        new RelationNodeEditor(
            context,
            relation,
            new RelationNodeEditor.Listener() {
              public void selectRelation(List<Either3<Label, Integer, Unit>> p) {
                RelationEditor.this.selectPath(append(path, p));
              }

              public void replaceRelation(
                  List<Either3<Label, Integer, Unit>> p, Relation r2) {
                p = append(path, p);
                Relation r = relationAt(p, relation).some().x;
                relation = replaceRelationOrPathAt(relation, p, x(r2));
                initNodeEditor();
                setPath(path);
              }

              public void done() {
                done.f(relation);
              }

              public void extendUnion(List<Either3<Label, Integer, Unit>> p,
                  final Integer i, Relation r2) {
                relation =
                    RelationUtils.extendUnion(relation, append(path, p), i, r2);
                initNodeEditor();
                setPath(path);
              }

              public void
                  extendComposition(List<Either3<Label, Integer, Unit>> p,
                      Integer i, Relation r2) {
                relation =
                    RelationUtils.extendComposition(relation, append(path, p),
                        i, r2);
                initNodeEditor();
                setPath(path);
              }

              public void changeDomain(Code d2) {
                relation = RelationUtils.changeDomain(relation, path, d2);
                initNodeEditor();
                setPath(path);
              }

              public void changeCodomain(Code c2) {
                relation = RelationUtils.changeCodomain(relation, path, c2);
                initNodeEditor();
                setPath(path);
              }

              public void selectPath(List<Either3<Label, Integer, Unit>> p) {
                RelationEditor.this.selectPath(relationOrPathAt(
                    append(path, p), relation).y());
              }
            }, path, codes, codeLabelAliases, codeAliases, relationAliases,
            relationViewColors);
    ViewGroup cont = (ViewGroup) findViewById(R.id.container_relation_editor);
    cont.removeAllViews();
    cont.addView(nodeEditor);
  }

  private static Either<Relation, List<Either3<Label, Integer, Unit>>> x(
      Relation r) {
    return Either.x(r);
  }

  public Bundle getState() {
    Bundle b = new Bundle();
    b.putSerializable(STATE_PATH, path);
    b.putSerializable(STATE_RELATION, relation);
    return b;
  }

}
