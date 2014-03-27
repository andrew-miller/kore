package com.example.kore.ui;

import static com.example.kore.ui.PatternUtils.emptyPattern;
import static com.example.kore.ui.RelationUtils.adaptComposition;
import static com.example.kore.ui.RelationUtils.codomain;
import static com.example.kore.ui.RelationUtils.domain;
import static com.example.kore.ui.RelationUtils.dummy;
import static com.example.kore.ui.RelationUtils.enclosingAbstraction;
import static com.example.kore.ui.RelationUtils.mapPaths;
import static com.example.kore.ui.RelationUtils.projection;
import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.ui.RelationUtils.relationOrPathAt;
import static com.example.kore.ui.RelationUtils.replaceRelationOrPathAt;
import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.drop;
import static com.example.kore.utils.ListUtils.fromArray;
import static com.example.kore.utils.ListUtils.insert;
import static com.example.kore.utils.ListUtils.isSubList;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.length;
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
import com.example.kore.utils.F;
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
    pathContainer.addView(RelationPath.make(context,
        relationViewColors.relationcolors, this, relation, relations,
        codeLabelAliases, null, path));
  }

  public void changeRelationType(Relation.Tag t) {
    Either<Relation, List<Either3<Label, Integer, Unit>>> rp =
        relationOrPathAt(path, relation);
    Relation r = rp.isY() ? relationAt(rp.y(), relation).some().x : rp.x();
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
      if (!equal(d, unit))
        throw new RuntimeException("cannot make product with non-unit domain");
      Map<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> m =
          Map.empty();
      for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet()))
        m = m.put(e.k, x(dummy(unit, reroot(c, fromArray(e.k)))));
      r2 = Relation.product(new Product(m, c));
      break;
    case LABEL:
      if (!equal(d, unit))
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
    relation = replaceRelationOrPathAt(relation, path, x(r2));
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
    relation =
        replaceRelationOrPathAt(relation, path, x(Relation.composition(comp)));
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
    relation =
        replaceRelationOrPathAt(relation, path, x(Relation.composition(comp)));
    initNodeEditor();
    setPath(path);
  }

  public void extendComposition(List<Either3<Label, Integer, Unit>> p,
      final Integer i, Relation r2) {
    if (i < 0)
      throw new RuntimeException("index can't be negative");
    final List<Either3<Label, Integer, Unit>> path = append(this.path, p);
    Either<Relation, List<Either3<Label, Integer, Unit>>> er =
        relationOrPathAt(path, relation);
    Relation r1 = er.isY() ? relationAt(er.y(), relation).some().x : er.x();
    Code d = domain(r1);
    Code c = codomain(r1);

    Composition comp;
    Relation remappedRelation;
    if (!er.isY() && er.x().tag == Tag.COMPOSITION) {
      comp = new Composition(insert(er.x().composition().l, i, x(r2)), d, c);
      remappedRelation = bumpIndexes(relation, i, path);
    } else {
      switch (i) {
      case 0:
        comp = new Composition(fromArray(x(r2), er), d, c);
        break;
      case 1:
        comp = new Composition(fromArray(er, x(r2)), d, c);
        break;
      default:
        throw new RuntimeException("invalid index");
      }
      remappedRelation = insertIndexes(relation, i, path);
    }

    relation =
        adaptComposition(
            replaceRelationOrPathAt(remappedRelation, path,
                x(Relation.composition(comp))), path);
    initNodeEditor();
    setPath(this.path);
  }

  public void extendUnion(List<Either3<Label, Integer, Unit>> p,
      final Integer i, Relation r2) {
    if (i < 0)
      throw new RuntimeException("index can't be negative");
    final List<Either3<Label, Integer, Unit>> path = append(this.path, p);
    Either<Relation, List<Either3<Label, Integer, Unit>>> er =
        relationOrPathAt(path, relation);
    Relation r1 = er.isY() ? relationAt(er.y(), relation).some().x : er.x();
    Code d = domain(r1);
    Code c = codomain(r1);
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
            Relation
                .composition(new Composition(fromArray(x(t), x(r2)), d, c2));
    }
    if (!equal(c, c2)) {
      Relation t = dummy(c2, c);
      if (r2.tag == Tag.COMPOSITION)
        r2 =
            Relation.composition(new Composition(append(x(t),
                r2.composition().l), d, c));
      else
        r2 =
            Relation.composition(new Composition(fromArray(x(r2), x(t)), d, c));
    }
    Union union;
    Relation remappedRelation;
    if (!er.isY() && er.x().tag == Tag.UNION) {
      union =
          new Union(insert(er.x().union().l, i, x(r2)), er.x().union().i, er
              .x().union().o);
      remappedRelation = bumpIndexes(relation, i, path);
    } else {
      switch (i) {
      case 0:
        union = new Union(fromArray(x(r2), er), d, c);
        break;
      case 1:
        union = new Union(fromArray(er, x(r2)), d, c);
        break;
      default:
        throw new RuntimeException("invalid index");
      }
      remappedRelation = insertIndexes(relation, i, path);
    }
    relation =
        replaceRelationOrPathAt(remappedRelation, path,
            x(Relation.union(union)));
    initNodeEditor();
    setPath(this.path);
  }

  private Relation bumpIndexes(Relation r, final Integer i,
      final List<Either3<Label, Integer, Unit>> path) {
    return mapPaths(
        r,
        new F<List<Either3<Label, Integer, Unit>>, List<Either3<Label, Integer, Unit>>>() {
          public List<Either3<Label, Integer, Unit>> f(
              List<Either3<Label, Integer, Unit>> p) {
            if (isSubList(path, p)) {
              List<Either3<Label, Integer, Unit>> l = drop(p, length(path));
              if (!l.isEmpty() && l.cons().x.y() >= i)
                return append(
                    path,
                    cons(Either3.<Label, Integer, Unit> y(l.cons().x.y() + 1),
                        l.cons().tail));
            }
            return p;
          }
        });
  }

  private Relation insertIndexes(Relation r, final Integer i,
      final List<Either3<Label, Integer, Unit>> path) {
    return mapPaths(
        r,
        new F<List<Either3<Label, Integer, Unit>>, List<Either3<Label, Integer, Unit>>>() {
          public List<Either3<Label, Integer, Unit>> f(
              List<Either3<Label, Integer, Unit>> p) {
            if (isSubList(path, p)) {
              List<Either3<Label, Integer, Unit>> l = drop(p, length(path));
              if (!l.isEmpty())
                return append(path,
                    cons(Either3.<Label, Integer, Unit> y((i + 1) % 2), l));
            }
            return p;
          }
        });
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
