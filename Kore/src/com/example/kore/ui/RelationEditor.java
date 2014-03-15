package com.example.kore.ui;

import static com.example.kore.ui.PatternUtils.emptyPattern;
import static com.example.kore.ui.RelationUtils.adaptComposition;
import static com.example.kore.ui.RelationUtils.codomain;
import static com.example.kore.ui.RelationUtils.domain;
import static com.example.kore.ui.RelationUtils.dummy;
import static com.example.kore.ui.RelationUtils.enclosingAbstraction;
import static com.example.kore.ui.RelationUtils.projection;
import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.ui.RelationUtils.replaceRelationAt;
import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.fromArray;
import static com.example.kore.utils.ListUtils.insert;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Abstraction;
import com.example.kore.codes.Relation.Composition;
import com.example.kore.codes.Relation.Label_;
import com.example.kore.codes.Relation.Product;
import com.example.kore.codes.Relation.Projection;
import com.example.kore.codes.Relation.Tag;
import com.example.kore.codes.Relation.Union;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.Optional;
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
    pathContainer.addView(new RelationPath(context,
        relationViewColors.relationcolors, this, relation, relations,
        codeLabelAliases, null, path));
  }

  public void changeRelationType(Relation.Tag t) {
    Relation r = relationAt(path, relation).some().x;
    Code d = domain(r);
    Code c = codomain(r);
    Relation r2;
    switch (t) {
    case ABSTRACTION:
      r2 =
          Relation.abstraction(new Abstraction(emptyPattern, x(dummy(unit, c)),
              d, c));
      break;
    case COMPOSITION:
      r2 = Relation.composition(new Composition(nil, d, c));
      break;
    case PRODUCT:
      if (!equal(domain(r), unit))
        throw new RuntimeException("cannot make product with non-unit domain");
      Map<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> m =
          Map.empty();
      for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet()))
        m = m.put(e.k, x(dummy(unit, reroot(c, fromArray(e.k)))));
      r2 = Relation.product(new Product(m, c));
      break;
    case LABEL:
      if (!equal(domain(r), unit))
        throw new RuntimeException("cannot make label with non-unit domain");
      notEmpty: {
        for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet())) {
          r2 =
              Relation.label(new Label_(e.k, x(dummy(unit,
                  reroot(c, fromArray(e.k)))), c));
          break notEmpty;
        }
        r2 = dummy(d, c);
      }
      break;
    case PROJECTION:
      Optional<Abstraction> oa = enclosingAbstraction(path, relation);
      if (oa.isNothing())
        throw new RuntimeException(
            "attempt to make projection that isn't contained within an abstraction");
      Code arg = oa.some().x.i;
      Optional<Projection> or2 = projection(arg, c);
      r2 = or2.isNothing() ? dummy(d, c) : Relation.projection(or2.some().x);
      break;
    case UNION:
      r2 = dummy(d, c);
      break;
    default:
      throw boom();
    }
    relation = replaceRelationAt(relation, path, r2);
    setPath(path);
    initNodeEditor();
  }

  public void onDone() {
    doneListener.onDone(relation);
  }

  public void selectPath(List<Either3<Label, Integer, Unit>> p) {
    notNull(p);
    Optional<Relation> or = relationAt(p, relation);
    if (or.isNothing())
      throw new RuntimeException("invalid path");
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
    Relation r = relationAt(path, relation).some().x;
    Composition comp;
    Code d = domain(r);
    Code c = codomain(r);
    Relation t = dummy(c, c2);
    switch (r.tag) {
    case COMPOSITION:
      comp = new Composition(append(x(t), r.composition().l), d, c2);
      break;
    case ABSTRACTION:
    case LABEL:
    case PRODUCT:
    case PROJECTION:
    case UNION:
      comp = new Composition(cons(x(r), cons(x(t), nil)), d, c2);
      break;
    default:
      throw boom();
    }
    relation = replaceRelationAt(relation, path, Relation.composition(comp));
    initNodeEditor();
    setPath(path);
  }

  public void changeDomain(Code d2) {
    Relation r = relationAt(path, relation).some().x;
    Composition comp;
    Code d = domain(r);
    Code c = codomain(r);
    Relation t = dummy(d2, d);
    switch (r.tag) {
    case COMPOSITION:
      comp = new Composition(cons(x(t), r.composition().l), d2, c);
      break;
    case ABSTRACTION:
    case LABEL:
    case PRODUCT:
    case PROJECTION:
    case UNION:
      comp = new Composition(cons(x(t), cons(x(r), nil)), d2, c);
      break;
    default:
      throw boom();
    }
    relation = replaceRelationAt(relation, path, Relation.composition(comp));
    initNodeEditor();
    setPath(path);
  }

  public void move(Integer src, Integer dest) {
    if (dest < 0 | src < 0)
      throw new RuntimeException("indexes can't be negative");
    Relation r = relationAt(path, relation).some().x;
    switch (r.tag) {
    case COMPOSITION:
      Composition c = r.composition();
      relation =
          replaceRelationAt(
              relation,
              path,
              adaptComposition(
                  replaceRelationAt(relation, path, Relation
                      .composition(new Composition(ListUtils.move(c.l, src,
                          dest), c.i, c.o))), path));
      break;
    case UNION:
      Union u = r.union();
      relation =
          replaceRelationAt(relation, path, Relation.union(new Relation.Union(
              ListUtils.move(u.l, src, dest), u.i, u.o)));
      break;
    default:
      throw boom();
    }
    initNodeEditor();
    setPath(path);
  }

  public void extendComposition(List<Either3<Label, Integer, Unit>> p,
      Integer i, Relation r2) {
    if (i < 0)
      throw new RuntimeException("index can't be negative");
    List<Either3<Label, Integer, Unit>> path = append(this.path, p);
    Relation r = relationAt(path, relation).some().x;
    Code d = domain(r);
    Code c = codomain(r);

    Composition comp;
    if (r.tag == Tag.COMPOSITION) {
      comp = new Composition(insert(r.composition().l, i, x(r2)), d, c);
    } else {
      switch (i) {
      case 0:
        comp = new Composition(cons(x(r2), cons(x(r), nil)), d, c);
        break;
      case 1:
        comp = new Composition(cons(x(r), cons(x(r2), nil)), d, c);
        break;
      default:
        throw new RuntimeException("invalid index");
      }
    }

    relation =
        replaceRelationAt(
            relation,
            path,
            adaptComposition(
                replaceRelationAt(relation, path, Relation.composition(comp)),
                path));
    initNodeEditor();
    setPath(this.path);
  }

  public void extendUnion(List<Either3<Label, Integer, Unit>> p, Integer i,
      Relation r2) {
    if (i < 0)
      throw new RuntimeException("index can't be negative");
    List<Either3<Label, Integer, Unit>> path = append(this.path, p);
    Relation r = relationAt(path, relation).some().x;
    Code d = domain(r);
    Code c = codomain(r);
    Code d2 = domain(r2);
    Code c2 = codomain(r2);
    if (!equal(d, d2)) {
      Relation t = dummy(d, d2);
      if (r2.tag == Tag.COMPOSITION)
        r2 =
            Relation.composition(new Composition(
                cons(x(t), r2.composition().l), d, c2));
      else
        r2 =
            Relation.composition(new Composition(cons(x(t), cons(x(r2), nil)),
                d, c2));
    }
    if (!equal(c, c2)) {
      Relation t = dummy(c2, c);
      if (r2.tag == Tag.COMPOSITION)
        r2 =
            Relation.composition(new Composition(append(x(t),
                r2.composition().l), d, c));
      else
        r2 =
            Relation.composition(new Composition(cons(x(r2), cons(x(t), nil)),
                d, c));
    }
    Union union;
    if (r.tag == Tag.UNION)
      union =
          new Union(insert(r.union().l, i, x(r2)), r.union().i, r.union().o);
    else
      switch (i) {
      case 0:
        union = new Union(cons(x(r2), cons(x(r), nil)), d, c);
        break;
      case 1:
        union = new Union(cons(x(r), cons(x(r2), nil)), d, c);
        break;
      default:
        throw new RuntimeException("invalid index");
      }
    relation =
        RelationUtils.replaceRelationAt(relation, path, Relation.union(union));
    initNodeEditor();
    setPath(this.path);
  }

  private static List<Either<Relation, List<Either3<Label, Integer, Unit>>>> nil =
      ListUtils.<Either<Relation, List<Either3<Label, Integer, Unit>>>> nil();

  private static Either<Relation, List<Either3<Label, Integer, Unit>>> x(
      Relation r) {
    return Either.<Relation, List<Either3<Label, Integer, Unit>>> x(r);
  }

  public void
      replaceRelation(List<Either3<Label, Integer, Unit>> p, Relation r2) {
    p = append(path, p);
    Relation r = relationAt(p, relation).some().x;
    relation = replaceRelationAt(relation, p, r2);
    initNodeEditor();
  }

  public void selectRelation(List<Either3<Label, Integer, Unit>> p) {
    selectPath(append(path, p));
  }

  // XXX this is confusing as fuck because it's not obvious that
  // replaceRelation(Relation) is reacting to PathEditor, whereas
  // replaceRelation(List<...>, Relation) isn't. get rid of this multiple
  // interface shit
  public void replaceRelation(Relation r2) {
    Relation r = relationAt(path, relation).some().x;
    if (!equal(domain(r), domain(r2)) | !equal(codomain(r), codomain(r2)))
      r2 =
          adaptComposition(
              replaceRelationAt(relation, path,
                  Relation.composition(new Composition(fromArray(x(r2)),
                      domain(r), codomain(r)))), path);
    relation = replaceRelationAt(relation, path, r2);
    initNodeEditor();
    setPath(path);
  }

}
