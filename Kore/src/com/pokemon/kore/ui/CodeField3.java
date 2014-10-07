package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.CodeUtils.canReplace;
import static com.pokemon.kore.utils.CodeUtils.codeAt2;
import static com.pokemon.kore.utils.CodeUtils.hash;
import static com.pokemon.kore.utils.CodeUtils.icode;
import static com.pokemon.kore.utils.CodeUtils.renderCode3;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Null.notNull;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import com.pokemon.kore.R;
import com.pokemon.kore.codes.Code2;
import com.pokemon.kore.codes.Code2.Link;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.utils.CodeAtErr;
import com.pokemon.kore.utils.CodeUtils.Resolver;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.ICode;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;

public class CodeField3 {
  public static interface Listener {
    public void selectCode();

    public void selectLabel();

    public void replaceField(Either3<Code2, List<Label>, Link> n);

    public void changeLabelAlias(String alias);
  }

  public static View make(Context context, Listener listener, Label label,
      Code2 rootCode, boolean selected, CodeLabelAliasMap2 codeLabelAliases,
      Bijection<Link, String> codeAliases, List<Code2> codes, List<Label> path,
      Optional<String> labelAlias, Resolver r) {
    notNull(context, listener, label, rootCode, codeLabelAliases, codeAliases,
        codes, path, labelAlias, r);
    View v = LayoutInflater.from(context).inflate(R.layout.code_field, null);
    Button labelButton = (Button) v.findViewById(R.id.button_label);
    labelButton.setBackgroundColor((int) Long.parseLong(
        label.label.substring(0, 8), 16));
    labelButton.setText(labelAlias.isNothing() ? label.label : labelAlias
        .some().x);
    if (selected)
      labelButton.setText("---");
    labelButton.setOnClickListener($ -> listener.selectLabel());
    labelButton.setOnLongClickListener($v -> {
      UIUtils.replaceWithTextEntry((ViewGroup) $v.getParent(), $v, context,
          label.label, s -> {
            listener.changeLabelAlias(s);
            return null;
          });
      return true;
    });
    ICode ir = icode(rootCode, r);
    Button codeButton = (Button) v.findViewById(R.id.button_code);
    codeButton.setOnClickListener($ -> listener.selectCode());
    codeButton.setOnLongClickListener($v -> {
      PopupMenu pm = new PopupMenu(context, $v);
      Menu m = pm.getMenu();
      UIUtils.addCodeToMenu3(m, ir, nil(), codeLabelAliases, codeAliases,
          p -> {
            Either3<Code2, List<Label>, Link> n;
            if (codeAt2(p, rootCode).equals(Either.y(CodeAtErr.HitLink))) {
              Pair<Code2, List<Label>> cl = codeAt2(p, ir).some().x.link();
              n = Either3.z(new Link(hash(cl.x), cl.y));
            } else
              n = Either3.y(p);
            if (canReplace(rootCode, append(label, path), n, r))
              listener.replaceField(n);
            return unit();
          });
      m.add("---");
      for (Code2 c : iter(codes)) {
        ICode ic = icode(c, r);
        UIUtils.addCodeToMenu3(
            m,
            ic,
            nil(),
            codeLabelAliases,
            codeAliases,
            p -> {
              Pair<Code2, List<Label>> cl = codeAt2(p, ic).some().x.link();
              Either3<Code2, List<Label>, Link> n =
                  Either3.z(new Link(hash(cl.x), cl.y));
              if (canReplace(rootCode, append(label, path), n, r))
                listener.replaceField(n);
              return unit();
            });
      }
      pm.show();
      return true;
    });
    codeButton.setText(renderCode3(ir, append(label, path), codeLabelAliases,
        codeAliases, 1));
    return v;
  }

}
