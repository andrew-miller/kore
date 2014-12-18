package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.PatternUtils.patternAt;
import static com.pokemon.kore.ui.PatternUtils.replacePatternAt;
import static com.pokemon.kore.utils.CodeUtils.child;
import static com.pokemon.kore.utils.CodeUtils.hashLink;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Pattern;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.ICode;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public class PatternView2 {
  public static View make(Context context, Integer aliasTextColor,
      Integer labelTextColor, Pattern rootPattern, ICode c,
      CodeLabelAliasMap2 codeLabelAliases, F<Pattern, Unit> replace) {
    return make(context, aliasTextColor, labelTextColor, rootPattern, nil(), c,
        codeLabelAliases, replace);
  }

  private static View make(Context context, Integer aliasTextColor,
      Integer labelTextColor, Pattern rootPattern, List<Label> patternPath,
      ICode c, CodeLabelAliasMap2 codeLabelAliases, F<Pattern, Unit> replace) {
    Pattern pattern = patternAt(rootPattern, patternPath).some().x;
    F<View, Unit> f =
        v -> {
          v.setOnClickListener($ -> PatternMenu2.make(v, context, c,
              codeLabelAliases, pattern, p -> replace.f(replacePatternAt(
                  rootPattern, patternPath, p).some().x)));
          return unit();
        };
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    Bijection<Label, String> las =
        codeLabelAliases.getAliases(hashLink(c.link()));
    for (Pair<Label, ?> e : iter(pattern.fields.entrySet())) {
      LinearLayout ll2 = new LinearLayout(context);
      Optional<String> ola = las.xy.get(e.x);
      View v =
          LabelView.make(context,
              ola.isNothing() ? Either.x(e.x) : Either.y(ola.some().x),
              aliasTextColor, labelTextColor);
      ll2.addView(v);
      f.f(v);
      ll2.addView(make(context, aliasTextColor, labelTextColor, rootPattern,
          append(e.x, patternPath), child(c, e.x), codeLabelAliases, replace));
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
