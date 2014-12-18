package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.iRelationOrPathToRelationOrPath;
import static com.pokemon.kore.utils.CodeUtils.hashLink;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.pokemon.kore.codes.IRelation;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation2;
import com.pokemon.kore.codes.Relation2.Abstraction;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.Unit;

public class AbstractionView2 {
  interface Listener {
    void replace(Relation2 r);

    void select(View v);
  }

  public static View make(Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      Integer aliasTextColor, Integer labelTextColor, IRelation r,
      CodeLabelAliasMap2 codeLabelAliases, Listener listener) {
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);
    ll.addView(PatternView2.make(context, aliasTextColor, labelTextColor, r.ir
        .abstraction().pattern, r.ir.abstraction().i, codeLabelAliases, p -> {
      listener.replace(Relation2.abstraction(new Abstraction(p,
          iRelationOrPathToRelationOrPath(r.ir.abstraction().r.f(unit())),
          hashLink(r.ir.abstraction().i.link()), hashLink(r.ir.abstraction().o
              .link()))));
      return unit();
    }));
    ll.setClickable(true);
    ll.setOnClickListener($ -> listener.select(ll));
    ll.addView(make.f(Either3.z(unit())));
    return ll;
  }
}