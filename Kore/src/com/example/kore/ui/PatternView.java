package com.example.kore.ui;

import static com.example.kore.ui.PatternUtils.patternAt;
import static com.example.kore.ui.PatternUtils.replacePatternAt;
import static com.example.kore.utils.CodeUtils.directPath;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class PatternView {
  public static View make(final Context context, Integer aliasTextColor,
      final Pattern rootPattern, final Code rootCode,
      final List<Label> codePath, final CodeLabelAliasMap codeLabelAliases,
      final F<Pattern, Unit> replace) {
    return make(context, aliasTextColor, rootPattern, ListUtils.<Label> nil(),
        rootCode, codePath, codeLabelAliases, replace);
  }

  private static View make(final Context context, Integer aliasTextColor,
      final Pattern rootPattern, final List<Label> patternPath,
      final Code rootCode, final List<Label> codePath,
      final CodeLabelAliasMap codeLabelAliases, final F<Pattern, Unit> replace) {
    final Pattern pattern = patternAt(rootPattern, patternPath).some().x;
    F<View, Unit> f = new F<View, Unit>() {
      public Unit f(final View v) {
        v.setOnClickListener(new OnClickListener() {
          public void onClick(View _) {
            PatternMenu.make(v, context, rootCode, codePath, codeLabelAliases,
                pattern, new PatternMenu.Listener() {
                  public void select(Pattern p) {
                    replace.f(replacePatternAt(rootPattern, patternPath, p)
                        .some().x);
                  }
                });
          }
        });
        return unit();
      }
    };
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    Bijection<Label, String> las =
        codeLabelAliases.getAliases(new CanonicalCode(rootCode, codePath));
    for (Pair<Label, ?> e : iter(pattern.fields.entrySet())) {
      LinearLayout ll2 = new LinearLayout(context);
      Optional<String> ola = las.xy.get(e.x);
      View v;
      if (ola.isNothing())
        ll2.addView(v = LabelView.make(context, e.x, aliasTextColor));
      else {
        Button b = new Button(context);
        b.setText(ola.some().x);
        ll2.addView(b);
        v = b;
      }
      f.f(v);
      ll2.addView(make(context, aliasTextColor, rootPattern,
          append(e.x, patternPath), rootCode,
          directPath(append(e.x, codePath), rootCode), codeLabelAliases,
          replace));
      ll.addView(ll2);
    }
    if (pattern.fields.entrySet().isEmpty()) {
      Button b = new Button(context);
      f.f(b);
      ll.addView(b);
    }
    return ll;
  }
}
