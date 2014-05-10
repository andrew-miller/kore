package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Null.notNull;
import static com.example.kore.utils.OptionalUtils.some;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.Boom;
import com.example.kore.utils.Either;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;
import com.example.kore.utils.Optional;
import com.example.kore.utils.OptionalUtils;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Ref;
import com.example.kore.utils.Unit;

public class CodeNodeEditor {
  public static interface Listener {
    void newField();

    void switchCodeOp();

    void deleteField(Label l);

    void selectCode(Label l);

    void replaceField(Either<Code, List<Label>> cp, Label l);

    void changeLabelAlias(Label label, String alias);

    void done();
  }

  public static View make(final Context context, final Code code,
      final Code rootCode, final Listener listener,
      final Map<CanonicalCode, String> codeAliases, final List<Code> codes,
      final List<Label> path, final CodeLabelAliasMap codeLabelAliases) {
    notNull(context, code, rootCode, listener, codeAliases, codes, path,
        codeLabelAliases);

    View v =
        LayoutInflater.from(context).inflate(R.layout.code_node_editor, null,
            true);
    final LinearLayout fields =
        (LinearLayout) v.findViewById(R.id.layout_fields);
    final Button deleteButton =
        (Button) v.findViewById(R.id.button_delete_field);
    final Button switchCodeOpButton =
        (Button) v.findViewById(R.id.button_switch_code_op);
    switchCodeOpButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        listener.switchCodeOp();
      }
    });
    v.findViewById(R.id.button_new_field).setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            listener.newField();
          }
        });

    v.findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        listener.done();
      }
    });

    final Ref<Optional<Label>> selectedLabel =
        new Ref<Optional<Label>>(OptionalUtils.<Label> nothing());
    final F<Unit, Unit> render = new F<Unit, Unit>() {
      public Unit f(Unit _) {
        switch (code.tag) {
        case PRODUCT:
          switchCodeOpButton.setText("{}");
          break;
        case UNION:
          switchCodeOpButton.setText("[]");
          break;
        default:
          throw Boom.boom();
        }
        fields.removeAllViews();
        Bijection<Label, String> las =
            codeLabelAliases.getAliases(new CanonicalCode(rootCode, path));
        for (final Pair<Label, Either<Code, List<Label>>> e : iter(code.labels
            .entrySet())) {
          final Label l = e.x;
          CodeField.Listener cfl = new CodeField.Listener() {
            public void selectLabel() {
              notNull(l);
              deleteButton.setVisibility(View.VISIBLE);
              selectedLabel.set(some(l));
              deleteButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                  listener.deleteField(l);
                }
              });
              f(unit());
            }

            public void selectCode() {
              listener.selectCode(l);
            }

            public void replaceField(Either<Code, List<Label>> cp) {
              listener.replaceField(cp, l);
            }

            public void changeLabelAlias(String alias) {
              listener.changeLabelAlias(l, alias);
            }
          };

          fields.addView(CodeField.make(context, cfl, l, e.y, rootCode, some(l)
              .equals(selectedLabel.get()), codeLabelAliases, codeAliases,
              codes, path, las.xy.get(l)));
        }
        return unit();
      }
    };
    render.f(unit());
    return v;
  }
}