package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
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
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Map;
import com.example.kore.utils.Optional;

public class CodeList extends FrameLayout {

  public static interface CodeSelectListener {
    public void onCodeSelected(Code c);
  }

  public static interface CodeAliasChangedListener {
    public void codeAliasChanged(Code code, List<Label> path, String alias);
  }

  public CodeList(Context context, final CodeSelectListener codeSelectListener,
      List<Code> codes, CodeLabelAliasMap codeLabelAliases,
      final CodeAliasChangedListener codeAliasChangedListener,
      Map<CanonicalCode, String> codeAliases) {
    super(context);
    notNull(codes, codeSelectListener, codeLabelAliases, codeAliases);
    View v =
        LayoutInflater.from(context).inflate(R.layout.code_list, this, true);
    LinearLayout codeListLayout =
        (LinearLayout) v.findViewById(R.id.layout_code_list);
    for (final Code code : iter(codes)) {
      final FrameLayout fl = new FrameLayout(context);
      Button b = new Button(context);
      Optional<String> codeName =
          codeAliases.get(new CanonicalCode(code, ListUtils.<Label> nil()));
      final String strCode =
          codeName.isNothing() ? CodeUtils.renderCode(code,
              ListUtils.<Label> nil(), codeLabelAliases, codeAliases, 1)
              : codeName.some().x;
      b.setText(strCode);
      b.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          codeSelectListener.onCodeSelected(code);
        }
      });
      fl.addView(b);

      b.setOnLongClickListener(new OnLongClickListener() {
        public boolean onLongClick(final View v) {
          UIUtils.replaceWithTextEntry(fl, v, getContext(), strCode,
              new F<String, Void>() {
                public Void f(String s) {
                  codeAliasChangedListener.codeAliasChanged(code,
                      ListUtils.<Label> nil(), s);
                  return null;
                }
              });
          return true;
        }
      });

      codeListLayout.addView(fl);
    }
  }

}
