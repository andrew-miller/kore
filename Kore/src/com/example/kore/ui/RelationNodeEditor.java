package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.codomain;
import static com.example.kore.ui.RelationUtils.domain;
import static com.example.kore.ui.RelationUtils.enclosingAbstraction;
import static com.example.kore.ui.RelationUtils.inAbstraction;
import static com.example.kore.ui.RelationUtils.unit_unit;
import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Null.notNull;
import static com.example.kore.utils.OptionalUtils.some;
import android.content.Context;
import android.util.Pair;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Tag;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.OptionalUtils;
import com.example.kore.utils.Unit;

public class RelationNodeEditor extends FrameLayout {
  interface Listener {
    void changeRelationType(Relation.Tag t);

    void relationSelected(Either3<Label, Integer, Unit> e);

    void onDone();

    void relationSelected(Either3<Label, Integer, Unit> e1,
        Either3<Label, Integer, Unit> e2);

    void changeCodomain(Code c);

    void changeDomain(Code c);

    void extendComposition(Integer i, Relation r);

    void extendUnion(Integer i, Relation r);

    void changePattern(Pattern p);

    void changeLabel(Label l);

    void move(Integer src, Integer dest);
  }

  private final Relation relation;
  private final Relation rootRelation;
  private final Listener listener;
  private final List<Either3<Label, Integer, Unit>> path;
  private final Button changeRelationTypeButton;
  private final LinearLayout fields;
  private CodeLabelAliasMap codeLabelAliases;

  class Move {
    Integer i;
  }

  public RelationNodeEditor(final Context context, final Relation relation,
      final Relation rootRelation, final Listener listener,
      final List<Either3<Label, Integer, Unit>> path, final List<Code> codes,
      final CodeLabelAliasMap codeLabelAliases,
      final Map<CanonicalCode, String> codeAliases) {
    super(context);
    notNull(relation, rootRelation, listener, path, codes, codeLabelAliases,
        codeAliases);
    this.relation = relation;
    this.rootRelation = rootRelation;
    this.listener = listener;
    this.path = path;
    this.codeLabelAliases = codeLabelAliases;
    View v =
        LayoutInflater.from(context).inflate(R.layout.relation_node_editor,
            this, true);
    fields = (LinearLayout) v.findViewById(R.id.layout_fields);
    changeRelationTypeButton =
        (Button) v.findViewById(R.id.button_change_relation_type);
    v.findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        listener.onDone();
      }
    });
    changeRelationTypeButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        PopupMenu pm = new PopupMenu(context, v);
        final Menu m = pm.getMenu();
        F<Pair<String, Tag>, Void> add =
            new F<Pair<String, Relation.Tag>, Void>() {
              public Void f(final Pair<String, Tag> p) {
                m.add(p.first).setOnMenuItemClickListener(
                    new OnMenuItemClickListener() {
                      public boolean onMenuItemClick(MenuItem _) {
                        listener.changeRelationType(p.second);
                        return true;
                      }
                    });
                return null;
              }
            };
        add.f(Pair.create("[]", Tag.UNION));
        if (equal(domain(relation), unit)) {
          if (codomain(relation).tag == Code.Tag.PRODUCT)
            add.f(Pair.create("{}", Tag.PRODUCT));
          if (codomain(relation).tag == Code.Tag.UNION)
            add.f(Pair.create("'", Tag.LABEL));
        }
        add.f(Pair.create("->", Tag.ABSTRACTION));
        add.f(Pair.create("|", Tag.COMPOSITION));
        if (inAbstraction(path, rootRelation))
          add.f(Pair.create(".", Tag.PROJECTION));
        pm.show();
      }
    });
    Button newFieldButton = (Button) v.findViewById(R.id.button_new_field);
    newFieldButton.setOnTouchListener(new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
          v.startDrag(null, new DragShadowBuilder(), Integer.valueOf(1234), 0);
          ((Button) v).setText("@");
        }
        return false;
      }
    });
    Button changeDomainButton =
        (Button) v.findViewById(R.id.button_change_domain);
    changeDomainButton.setText(CodeUtils.renderCode(
        RelationUtils.domain(relation), ListUtils.<Label> nil(),
        codeLabelAliases, codeAliases, 1));
    changeDomainButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        PopupMenu pm = new PopupMenu(context, v);
        Menu m = pm.getMenu();
        for (final Code c : iter(codes))
          UIUtils.addCodeToMenu(c, m, CodeOrPath.newCode(c), "", "",
              ListUtils.<Label> nil(), codeLabelAliases, codeAliases,
              new F<Void, Void>() {
                public Void f(Void x) {
                  listener.changeDomain(c);
                  return null;
                }
              });
        pm.show();
      }
    });
    Button changeCodomainButton =
        (Button) v.findViewById(R.id.button_change_codomain);
    changeCodomainButton.setText(CodeUtils.renderCode(
        RelationUtils.codomain(relation), ListUtils.<Label> nil(),
        codeLabelAliases, codeAliases, 1));
    changeCodomainButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        PopupMenu pm = new PopupMenu(context, v);
        Menu m = pm.getMenu();
        for (final Code c : iter(codes))
          UIUtils.addCodeToMenu(c, m, CodeOrPath.newCode(c), "", "",
              ListUtils.<Label> nil(), codeLabelAliases, codeAliases,
              new F<Void, Void>() {
                public Void f(Void _) {
                  listener.changeCodomain(c);
                  return null;
                }
              });
        pm.show();
      }
    });
    render();
  }

  private void setDragListener(View v) {
    v.setOnDragListener(new OnDragListener() {
      public boolean onDrag(View v, DragEvent e) {
        if (e.getAction() == DragEvent.ACTION_DROP) {
          int vMid = v.getWidth() / 2;
          int hMid = v.getHeight() / 2;
          double x = e.getX();
          double y = e.getY();
          boolean right = x > vMid;
          boolean down = y > hMid;
          double vDist = Math.max(x - vMid, vMid - x);
          double hDist = Math.max(y - hMid, hMid - y);
          if (vDist > hDist)
            listener.extendComposition(right ? 1 : 0, unit_unit);
          else
            listener.extendUnion(down ? 1 : 0, unit_unit);
        }
        return true;
      }
    });
  }

  private void render() {
    switch (relation.tag) {
    case PRODUCT:
      changeRelationTypeButton.setText("{}");
      Map<Label, String> m =
          codeLabelAliases.getAliases(new CanonicalCode(relation.product().o,
              ListUtils.<Label> nil()));
      for (final Entry<Label, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(relation
          .product().m.entrySet())) {
        if (e.v.isY())
          throw new RuntimeException("TODO handle path");
        fields.addView(new RelationField(getContext(), some(e.k), e.v.x(), m
            .get(e.k), codeLabelAliases, relation.product().o, ListUtils
            .<Label> nil(), some(enclosingAbstraction(path, rootRelation)
            .some().x.i), new RelationField.Listener() {
          public void selected() {
            listener.relationSelected(Either3.<Label, Integer, Unit> x(e.k));
          }

          public void selected(Integer i) {
            listener.relationSelected(Either3.<Label, Integer, Unit> x(e.k),
                Either3.<Label, Integer, Unit> y(i));
          }

          public void replaceLabel(Label l) {
            throw new RuntimeException("cannot replace label in product");

          }

          public void insert(Integer i) {
            throw new RuntimeException(
                "TODO handle composition insert in relationField");
          }

          public void move(Integer src, Integer dest) {
            throw new RuntimeException(
                "TODO handle composition move in relationField");
          }
        }));
      }
      break;
    case UNION:
      changeRelationTypeButton.setText("[]");
      int i = 0;
      for (Either<Relation, List<Either3<Label, Integer, Unit>>> r : iter(relation
          .union().l)) {
        final int i_ = i;
        if (r.isY())
          throw new RuntimeException("TODO handle path");
        RelationField rf =
            new RelationField(getContext(), OptionalUtils.<Label> nothing(),
                r.x(), OptionalUtils.<String> nothing(), codeLabelAliases,
                relation.union().o, ListUtils.<Label> nil(),
                some(enclosingAbstraction(path, rootRelation).some().x.i),
                new RelationField.Listener() {
                  public void selected() {
                    listener.relationSelected(Either3
                        .<Label, Integer, Unit> y(i_));
                  }

                  public void selected(Integer i) {
                    listener.relationSelected(
                        Either3.<Label, Integer, Unit> y(i_),
                        Either3.<Label, Integer, Unit> y(i));
                  }

                  public void replaceLabel(Label l) {
                    throw new RuntimeException("cannot replace label in union");
                  }

                  public void insert(Integer i) {
                    throw new RuntimeException(
                        "TODO handle composition insert in relationField");
                  }

                  public void move(Integer src, Integer dest) {
                    throw new RuntimeException(
                        "TODO handle composition move in relationField");
                  }
                });
        rf.setOnLongClickListener(new OnLongClickListener() {
          public boolean onLongClick(View v) {
            Move m = new Move();
            m.i = i_;
            v.startDrag(null, new DragShadowBuilder(), m, 0);
            return false;
          }
        });
        rf.setOnDragListener(new OnDragListener() {
          public boolean onDrag(View v, DragEvent e) {
            if (e.getAction() == DragEvent.ACTION_DROP) {
              Integer dest = e.getY() < v.getHeight() / 2 ? i_ : i_ + 1;
              if (e.getLocalState() instanceof Move)
                listener.move(((Move) e.getLocalState()).i, dest);
              else
                listener.extendUnion(dest, unit_unit);
            }
            return true;
          }
        });
        fields.addView(rf);
        i++;
      }
      break;
    case PROJECTION:
      changeRelationTypeButton.setText(".");
      ProjectionEditor pe =
          new ProjectionEditor(getContext(), relation.projection(),
              enclosingAbstraction(path, rootRelation).some().x.i,
              codeLabelAliases);
      setDragListener(pe);
      fields.addView(pe);
      break;
    case COMPOSITION:
      changeRelationTypeButton.setText("|");
      fields.addView(new CompositionEditor(getContext(),
          relation.composition(), codeLabelAliases,
          new CompositionEditor.Listener() {
            public void select(Integer i) {
              listener.relationSelected(Either3.<Label, Integer, Unit> y(i));
            }

            public void insert(Integer i) {
              listener.extendComposition(i, unit_unit);
            }

            public void move(Integer src, Integer dest) {
              listener.move(src, dest);
            }
          }));
      break;
    case ABSTRACTION:
      changeRelationTypeButton.setText("->");
      AbstractionEditor ae =
          new AbstractionEditor(getContext(), relation.abstraction(),
              codeLabelAliases, new AbstractionEditor.Listener() {
                public void relationSelected() {
                  listener.relationSelected(Either3
                      .<Label, Integer, Unit> z(Unit.unit()));
                }

                public void patternChanged(Pattern p) {
                  listener.changePattern(p);
                }
              });
      setDragListener(ae);
      fields.addView(ae);
      break;
    case LABEL:
      changeRelationTypeButton.setText("'");
      Either<Relation, List<Either3<Label, Integer, Unit>>> r =
          relation.label().r;
      final Label l = relation.label().label;
      if (r.isY())
        throw new RuntimeException("TODO handle path");
      RelationField rf =
          new RelationField(getContext(), some(l), r.x(),
              codeLabelAliases
                  .getAliases(
                      new CanonicalCode(relation.label().o, ListUtils
                          .<Label> nil())).get(l), codeLabelAliases,
              relation.label().o, ListUtils.<Label> nil(),
              OptionalUtils.<Code> nothing(), new RelationField.Listener() {
                public void selected() {
                  listener.relationSelected(Either3
                      .<Label, Integer, Unit> z(Unit.unit()));
                }

                public void selected(Integer i) {
                  throw new RuntimeException("wat");
                }

                public void replaceLabel(Label l) {
                  listener.changeLabel(l);
                }

                public void insert(Integer i) {
                  throw new RuntimeException(
                      "TODO handle composition insert in relationField");
                }

                public void move(Integer src, Integer dest) {
                  throw new RuntimeException(
                      "TODO handle composition move in relationField");
                }
              });
      setDragListener(rf);
      fields.addView(rf);
      break;
    default:
      throw boom();
    }
  }
}
