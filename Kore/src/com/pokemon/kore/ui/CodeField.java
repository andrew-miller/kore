package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.CodeUtils.directPath;
import static com.pokemon.kore.utils.CodeUtils.linkTree;
import static com.pokemon.kore.utils.CodeUtils.linkTreeToCode;
import static com.pokemon.kore.utils.CodeUtils.renderCode;
import static com.pokemon.kore.utils.CodeUtils.replaceCodeAt;
import static com.pokemon.kore.utils.CodeUtils.reroot;
import static com.pokemon.kore.utils.CodeUtils.validCode;
import static com.pokemon.kore.utils.LinkTreeUtils.rebase;
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
import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;

public class CodeField {
  public static interface Listener {
    public void selectCode();

    public void selectLabel();

    public void replaceField(Either<Code, List<Label>> cp);

    public void changeLabelAlias(String alias);
  }

  public static View make(Context context, Listener listener, Label label,
      Either<Code, List<Label>> codeOrPath, Code rootCode, boolean selected,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases, List<Code> codes,
      List<Label> path, Optional<String> labelAlias) {
    notNull(context, listener, label, codeOrPath, rootCode, codeLabelAliases,
        codeAliases, codes, path, labelAlias);
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
    Button codeButton = (Button) v.findViewById(R.id.button_code);
    codeButton.setOnClickListener($ -> listener.selectCode());
    codeButton.setOnLongClickListener($v -> {
      PopupMenu pm = new PopupMenu(context, $v);
      Menu m = pm.getMenu();
      UIUtils.addCodeToMenu(
          m,
          rootCode,
          nil(),
          codeLabelAliases,
          codeAliases,
          p -> {
            p = directPath(p, rootCode);
            if (validCode(replaceCodeAt(rootCode, append(label, path),
                Either.y(p))))
              listener.replaceField(Either.y(p));
            return unit();
          });
      m.add("---");
      for (Code c : iter(codes))
        UIUtils.addCodeToMenu(
            m,
            c,
            nil(),
            codeLabelAliases,
            codeAliases,
            p -> {
              Either<Code, List<Label>> n =
                  Either.x(linkTreeToCode(rebase(append(label, path),
                      linkTree(reroot(c, p)))));
              if (validCode(replaceCodeAt(rootCode, append(label, path), n)))
                listener.replaceField(n);
              return unit();
            });
      pm.show();
      return true;
    });
    codeButton.setText(renderCode(rootCode, append(label, path),
        codeLabelAliases, codeAliases, 1));
    return v;
  }

}
