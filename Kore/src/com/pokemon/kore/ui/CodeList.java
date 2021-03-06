package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.pokemon.kore.R;
import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.utils.CodeUtils;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;

public class CodeList {

  public static interface Listener {
    public void select(Code c);

    public boolean changeAlias(Code code, List<Label> path, String alias);
  }

  public static View make(Context context, Listener listener, List<Code> codes,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases) {
    notNull(context, codes, listener, codeLabelAliases, codeAliases);
    View v = LayoutInflater.from(context).inflate(R.layout.code_list, null);
    LinearLayout codeListLayout =
        (LinearLayout) v.findViewById(R.id.layout_code_list);
    for (Code code : iter(codes)) {
      FrameLayout fl = new FrameLayout(context);
      Button b = new Button(context);
      Optional<String> codeName =
          codeAliases.xy.get(new CanonicalCode(code, nil()));
      String strCode =
          codeName.isNothing() ? CodeUtils.renderCode(code, nil(),
              codeLabelAliases, codeAliases, 1) : codeName.some().x;
      b.setText(strCode);
      b.setOnClickListener($ -> listener.select(code));
      fl.addView(b);

      b.setOnLongClickListener($v -> {
        UIUtils.replaceWithTextEntry(fl, $v, context, strCode, s -> {
          listener.changeAlias(code, nil(), s);
          return null;
        });
        return true;
      });

      codeListLayout.addView(fl);
    }
    return v;
  }
}