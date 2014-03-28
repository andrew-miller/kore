package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.adaptComposition;
import static com.example.kore.ui.RelationUtils.codomain;
import static com.example.kore.ui.RelationUtils.domain;
import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.ui.RelationUtils.relationOrPathAt;
import static com.example.kore.ui.RelationUtils.replaceRelationOrPathAt;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.fromArray;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Composition;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;
import com.example.kore.utils.Unit;

public class RelationEditor extends FrameLayout implements
    RelationNodeEditor.Listener, RelationPath.Listener {

  private RelationNodeEditor nodeEditor;
  private Relation relation;
  private List<Either3<Label, Integer, Unit>> path = nil();
  private final DoneListener doneListener;
  private final Context context;
  private final ViewGroup pathContainer;
  private final List<Code> codes;
  private final CodeLabelAliasMap codeLabelAliases;
  private final Map<CanonicalCode, String> codeAliases;
  private final RelationViewColors relationViewColors;
  private final List<Relation> relations;

  public interface DoneListener {
    public void onDone(Relation r);
  }

  public RelationEditor(Context context, Relation relation,
      DoneListener doneListener, final List<Code> codes,
      final CodeLabelAliasMap codeLabelAliases,
      final Map<CanonicalCode, String> codeAliases, List<Relation> relations,
      RelationViewColors relationViewColors) {
    super(context);
    notNull(relation, doneListener, relationViewColors);
    this.context = context;
    this.relation = relation;
    this.doneListener = doneListener;
    this.codes = codes;
    this.codeLabelAliases = codeLabelAliases;
    this.codeAliases = codeAliases;
    this.relationViewColors = relationViewColors;
    this.relations = relations;
    LayoutInflater.from(context).inflate(R.layout.relation_editor, this, true);
    pathContainer = (ViewGroup) findViewById(R.id.container_path);
    initNodeEditor();
    setPath(path);
  }

  private void setPath(List<Either3<Label, Integer, Unit>> path) {
    pathContainer.removeAllViews();
    pathContainer.addView(RelationPath.make(context,
        relationViewColors.relationcolors, this, relation, relations,
        codeLabelAliases, null, path));
  }

  public void changeRelationType(Relation.Tag t) {
    relation = RelationUtils.changeRelationType(relation, path, t);
    setPath(path);
    initNodeEditor();
  }

  public void onDone() {
    doneListener.onDone(relation);
  }

  public void selectPath(List<Either3<Label, Integer, Unit>> p) {
    notNull(p);
    relationOrPathAt(p, relation);
    path = p;
    setPath(p);
    initNodeEditor();
  }

  private void initNodeEditor() {
    nodeEditor =
        new RelationNodeEditor(context, relation, this, path, codes,
            codeLabelAliases, codeAliases, relationViewColors);
    ViewGroup cont = (ViewGroup) findViewById(R.id.container_relation_editor);
    cont.removeAllViews();
    cont.addView(nodeEditor);
  }

  public void changeCodomain(Code c2) {
    relation = RelationUtils.changeCodomain(relation, path, c2);
    initNodeEditor();
    setPath(path);
  }

  public void changeDomain(Code d2) {
    relation = RelationUtils.changeDomain(relation, path, d2);
    initNodeEditor();
    setPath(path);
  }

  public void extendComposition(List<Either3<Label, Integer, Unit>> p,
      final Integer i, Relation r2) {
    relation =
        RelationUtils.extendComposition(relation, append(this.path, p), i, r2);
    initNodeEditor();
    setPath(this.path);
  }

  public void extendUnion(List<Either3<Label, Integer, Unit>> p,
      final Integer i, Relation r2) {
    relation = RelationUtils.extendUnion(relation, append(this.path, p), i, r2);
    initNodeEditor();
    setPath(this.path);
  }

  private static Either<Relation, List<Either3<Label, Integer, Unit>>> x(
      Relation r) {
    return Either.<Relation, List<Either3<Label, Integer, Unit>>> x(r);
  }

  public void
      replaceRelation(List<Either3<Label, Integer, Unit>> p, Relation r2) {
    p = append(path, p);
    Relation r = relationAt(p, relation).some().x;
    relation = replaceRelationOrPathAt(relation, p, x(r2));
    initNodeEditor();
    setPath(path);
  }

  public void selectRelation(List<Either3<Label, Integer, Unit>> p) {
    selectPath(append(path, p));
  }

  // XXX this is confusing as fuck because it's not obvious that
  // this replaceRelation is reacting to PathEditor, whereas
  // replaceRelation(List<...>, Relation) isn't. get rid of this multiple
  // interface shit
  public void replaceRelation(
      Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
    Relation r = relationAt(path, relation).some().x;
    Relation r2 = er.isY() ? relationAt(er.y(), relation).some().x : er.x();
    relation =
        !equal(domain(r), domain(r2)) | !equal(codomain(r), codomain(r2)) ? adaptComposition(
            replaceRelationOrPathAt(relation, path,
                x(Relation.composition(new Composition(fromArray(er),
                    domain(r), codomain(r))))), path)
            : replaceRelationOrPathAt(relation, path, er);
    initNodeEditor();
    setPath(path);
  }
}
