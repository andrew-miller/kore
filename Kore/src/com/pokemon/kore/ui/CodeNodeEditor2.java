package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.CodeUtils.codeAt2;
import static com.pokemon.kore.utils.CodeUtils.hash;
import static com.pokemon.kore.utils.CodeUtils.icode;
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
import com.pokemon.kore.codes.Code2;
import com.pokemon.kore.codes.Code2.Link;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.utils.Boom;
import com.pokemon.kore.utils.CodeUtils.Resolver;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.ICode;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Ref;
import com.pokemon.kore.utils.Unit;

public class CodeNodeEditor2 {
  public static interface Listener {
    void newField();

    void switchCodeOp();

    void deleteField(Label l);

    void selectCode(Label l);

    void replaceField(Either3<Code2, List<Label>, Link> cpl, Label l);

    void changeLabelAlias(Label label, String alias);

    void done();
  }

  public static View make(Context context, Code2 rootCode, Listener listener,
      Bijection<Link, String> codeAliases, List<Code2> codes, List<Label> path,
      CodeLabelAliasMap2 codeLabelAliases, Resolver r) {
    notNull(context, rootCode, listener, codeAliases, codes, path,
        codeLabelAliases, r);

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
    ICode ic = codeAt2(path, icode(rootCode, r)).some().x;
    F<Unit, Unit> render = new F<Unit, Unit>() {
      public Unit f(Unit $) {
        switch (ic.tag()) {
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
        Pair<Code2, List<Label>> link = ic.link();
        Bijection<Label, String> las =
            codeLabelAliases.getAliases(new Link(hash(link.x), link.y));
        for (Pair<Label, ?> e : iter(ic.labels().entrySet())) {
          Label l = e.x;
          CodeField3.Listener cfl = new CodeField3.Listener() {
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

            public void replaceField(Either3<Code2, List<Label>, Link> cpl) {
              listener.replaceField(cpl, l);
            }

            public void changeLabelAlias(String alias) {
              listener.changeLabelAlias(l, alias);
            }
          };

          fields.addView(CodeField3.make(context, cfl, l, rootCode, some(l)
              .equals(selectedLabel.get()), codeLabelAliases, codeAliases,
              codes, path, las.xy.get(l), r));
        }
        return unit();
      }
    };
    render.f(unit());
    return v;
  }
}