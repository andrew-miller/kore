package com.example.kore.ui;

import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.codes.Relation.Abstraction;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Unit;

public class AbstractionView {
  public static View make(Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      Integer aliasTextColor, Abstraction abstraction,
      CodeLabelAliasMap codeLabelAliases, final F<Pattern, Unit> replace) {
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);
    ll.addView(PatternView.make(context, aliasTextColor, abstraction.pattern,
        abstraction.i, ListUtils.<Label> nil(), codeLabelAliases,
        new F<Pattern, Unit>() {
          public Unit f(Pattern p) {
            replace.f(p);
            return unit();
          }
        }));
    ll.addView(make.f(Either3.<Label, Integer, Unit> z(unit())));
    return ll;
  }
}
