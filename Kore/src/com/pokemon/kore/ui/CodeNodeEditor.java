package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.Null.notNull;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.OptionalUtils.some;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pokemon.kore.R;
import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.utils.Boom;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Ref;
import com.pokemon.kore.utils.Unit;

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

  public static View make(Context context, Code code,
      Code rootCode, Listener listener,
      Bijection<CanonicalCode, String> codeAliases,
      List<Code> codes, List<Label> path,
      CodeLabelAliasMap codeLabelAliases) {
    notNull(context, code, rootCode, listener, codeAliases, codes, path,
        codeLabelAliases);

    View v =
        LayoutInflater.from(context).inflate(R.layout.code_node_editor, null,
            true);
    LinearLayout fields = (LinearLayout) v.findViewById(R.id.layout_fields);
    Button deleteButton = (Button) v.findViewById(R.id.button_delete_field);
    Button switchCodeOpButton =
        (Button) v.findViewById(R.id.button_switch_code_op);
    switchCodeOpButton.setOnClickListener($ -> listener.switchCodeOp());
    v.findViewById(R.id.button_new_field).setOnClickListener(
        $ -> listener.newField());

    v.findViewById(R.id.button_done).setOnClickListener($ -> listener.done());

    Ref<Optional<Label>> selectedLabel = new Ref<>(nothing());
    F<Unit, Unit> render = new F<Unit, Unit>() {
      public Unit f(Unit $) {
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
        for (Pair<Label, Either<Code, List<Label>>> e : iter(code.labels
            .entrySet())) {
          Label l = e.x;
          CodeField.Listener cfl = new CodeField.Listener() {
            public void selectLabel() {
              notNull(l);
              deleteButton.setVisibility(View.VISIBLE);
              selectedLabel.set(some(l));
              deleteButton.setOnClickListener($ -> listener.deleteField(l));
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