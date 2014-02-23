package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.renderRelation;
import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Unit;

public class RelationField extends FrameLayout {
  private final Button labelButton;
  private final Optional<Label> label;
  private final Relation relation;
  private final Button relationButton;
  private final Listener listener;
  private final LinearLayout ll;
  private final Optional<String> labelAlias;
  private final CodeLabelAliasMap codeLabelAliases;
  private final Context context;
  private final Code rootCode;
  private final List<Label> path;
  private final Optional<Code> argCode;

  public interface Listener {
    void select();

    void select(Integer i);

    void replaceLabel(Label l);

    void insert(Integer i);

    void move(Integer src, Integer dest);
  }

  /**
   * (rootCode,path) designates the codomain of the relation containing this
   * field<br />
   * argCode is the domain of the enclosing abstraction if this field is a
   * projection or this field contains a projection without an abstraction on
   * the path from it to that projection
   */
  public RelationField(Context context, Optional<Label> l, Relation r,
      Optional<String> labelAlias, CodeLabelAliasMap codeLabelAliases,
      Code rootCode, List<Label> path, Optional<Code> argCode, Listener listener) {
    super(context);
    notNull(context, listener, l, r, labelAlias, codeLabelAliases, path,
        rootCode);
    this.context = context;
    this.listener = listener;
    this.label = l;
    this.relation = r;
    this.labelAlias = labelAlias;
    this.codeLabelAliases = codeLabelAliases;
    this.path = path;
    this.rootCode = rootCode;
    this.argCode = argCode;
    View v =
        LayoutInflater.from(context).inflate(R.layout.relation_field, this,
            true);
    ll = (LinearLayout) v.findViewById(R.id.ll);
    labelButton = (Button) v.findViewById(R.id.button_label);
    initLabelButton();
    relationButton = (Button) v.findViewById(R.id.button_relation);
    initRelationButton();
  }

  private void initLabelButton() {
    if (label.isNothing()) {
      labelButton.setVisibility(GONE);
      return;
    }
    labelButton.setBackgroundColor((int) Long.parseLong(
        label.some().x.label.substring(0, 8), 16));
    labelButton.setText(label.some().x.label);
    labelButton.setText(labelAlias.isNothing() ? label.some().x.label
        : labelAlias.some().x);
    labelButton.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(View v) {
        LabelSelectMenu.make(context, v, rootCode, codeLabelAliases,
            new CanonicalCode(rootCode, path), new F<Label, Void>() {
              public Void f(Label l) {
                listener.replaceLabel(l);
                return null;
              }
            });
        return true;
      }
    });
  }

  private void initRelationButton() {
    switch (relation.tag) {
    case PROJECTION:
      ll.removeViewAt(1);
      ll.addView(new ProjectionEditor(getContext(), relation.projection(),
          argCode.some().x, codeLabelAliases, new F<Void, Void>() {
            public Void f(Void x) {
              listener.select();
              return null;
            }
          }));
      break;
    case COMPOSITION:
      ll.removeViewAt(1);
      ll.addView(new CompositionEditor(getContext(), relation.composition(),
          codeLabelAliases, argCode, new CompositionEditor.Listener() {
            public void select(Integer i) {
              listener.select(i);
            }

            public void insert(Integer i) {
              listener.insert(i);
            }

            public void move(Integer src, Integer dest) {
              listener.move(src, dest);
            }
          }));
      break;
    case LABEL:
    case ABSTRACTION:
    case PRODUCT:
    case UNION:
      relationButton.setText(renderRelation(argCode,
          Either.<Relation, List<Either3<Label, Integer, Unit>>> x(relation),
          codeLabelAliases));
      break;
    default:
      throw boom();
    }
    relationButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        listener.select();
      }
    });
  }
}
