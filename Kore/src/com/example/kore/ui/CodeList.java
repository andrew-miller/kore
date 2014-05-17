package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Optional;

public class CodeList {

  public static interface Listener {
    public void select(Code c);

    public boolean changeAlias(Code code, List<Label> path, String alias);
  }

  public static View make(final Context context, final Listener listener,
      List<Code> codes, CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases) {
    notNull(context, codes, listener, codeLabelAliases, codeAliases);
    View v = LayoutInflater.from(context).inflate(R.layout.code_list, null);
    LinearLayout codeListLayout =
        (LinearLayout) v.findViewById(R.id.layout_code_list);
    for (final Code code : iter(codes)) {
      final FrameLayout fl = new FrameLayout(context);
      Button b = new Button(context);
      Optional<String> codeName =
          codeAliases.xy.get(new CanonicalCode(code, ListUtils.<Label> nil()));
      final String strCode =
          codeName.isNothing() ? CodeUtils.renderCode(code,
              ListUtils.<Label> nil(), codeLabelAliases, codeAliases, 1)
              : codeName.some().x;
      b.setText(strCode);
      b.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          listener.select(code);
        }
      });
      fl.addView(b);

      b.setOnLongClickListener(new OnLongClickListener() {
        public boolean onLongClick(final View v) {
          UIUtils.replaceWithTextEntry(fl, v, context, strCode,
              new F<String, Void>() {
                public Void f(String s) {
                  listener.changeAlias(code, ListUtils.<Label> nil(), s);
                  return null;
                }
              });
          return true;
        }
      });

      codeListLayout.addView(fl);
    }
    return v;
  }
}