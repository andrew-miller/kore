package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.PatternUtils.patternAt;
import static com.pokemon.kore.ui.PatternUtils.replacePatternAt;
import static com.pokemon.kore.utils.CodeUtils.directPath;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Pattern;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public class PatternView {
  public static View make(Context context, Integer aliasTextColor,
      Pattern rootPattern, Code rootCode, List<Label> codePath,
      CodeLabelAliasMap codeLabelAliases, F<Pattern, Unit> replace) {
    return make(context, aliasTextColor, rootPattern, nil(), rootCode,
        codePath, codeLabelAliases, replace);
  }

  private static View make(Context context, Integer aliasTextColor,
      Pattern rootPattern, List<Label> patternPath, Code rootCode,
      List<Label> codePath, CodeLabelAliasMap codeLabelAliases,
      F<Pattern, Unit> replace) {
    Pattern pattern = patternAt(rootPattern, patternPath).some().x;
    F<View, Unit> f =
        v -> {
          v.setOnClickListener($ -> PatternMenu.make(v, context, rootCode,
              codePath, codeLabelAliases, pattern, p -> replace
                  .f(replacePatternAt(rootPattern, patternPath, p).some().x)));
          return unit();
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
