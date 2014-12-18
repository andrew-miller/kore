package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.CodeUtils.hashLink;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.pokemon.kore.codes.IRelation;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Unit;

public class Label_View2 {

  public static View make(Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color, IRelation r,
      Integer aliasTextColor, Integer labelTextColor,
      CodeLabelAliasMap2 codeLabelAliases, F<View, Unit> select) {
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);

    Optional<String> ola =
        codeLabelAliases.getAliases(hashLink(r.ir.label().o.link())).xy
            .get(r.ir.label().label);
    View v =
        LabelView.make(context, ola.isNothing() ? Either.x(r.ir.label().label)
            : Either.y(ola.some().x), aliasTextColor, labelTextColor);
    v.setOnClickListener($ -> select.f(v));

    ll.addView(v);
    ll.addView(make.f(Either3.z(unit())));
    return ll;
  }
}