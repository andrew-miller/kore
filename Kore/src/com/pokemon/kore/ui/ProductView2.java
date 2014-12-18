package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.CodeUtils.hashLink;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pokemon.kore.codes.IRelation;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public class ProductView2 {
  public static View make(Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      Integer aliasTextColor, Integer labelTextColor, IRelation r,
      CodeLabelAliasMap2 codeLabelAliases, F<View, Unit> select) {
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setBackgroundColor(color);
    Bijection<Label, String> las =
        codeLabelAliases.getAliases(hashLink(r.ir.product().o.link()));
    for (Pair<Label, ?> e : iter(r.ir.product().m.f(unit()).entrySet())) {
      LinearLayout ll2 = new LinearLayout(context);
      Optional<String> ola = las.xy.get(e.x);
      ll2.addView(LabelView.make(context, ola.isNothing() ? Either.x(e.x)
          : Either.y(ola.some().x), aliasTextColor, labelTextColor));
      ll2.addView(make.f(Either3.x(e.x)));
      ll.addView(ll2);
    }
    if (r.ir.product().m.f(unit()).entrySet().isEmpty()) {
      Button b = new Button(context);
      b.setOnClickListener($ -> select.f(b));
      ll.addView(b);
    }
    return ll;
  }
}