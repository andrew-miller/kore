package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.relationAt;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.codes.Relation.Abstraction;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Unit;

public class AbstractionView {
  interface Listener {
    void replace(Relation r);

    void select(View v);
  }

  public static View make(Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      Integer aliasTextColor, Integer labelTextColor, Relation relation,
      List<Either3<Label, Integer, Unit>> path,
      CodeLabelAliasMap codeLabelAliases, Listener listener) {
    Relation r = relationAt(path, relation).some().x;
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);
    ll.addView(PatternView.make(context, aliasTextColor, labelTextColor, r
        .abstraction().pattern, r.abstraction().i, nil(), codeLabelAliases,
        p -> {
          listener.replace(Relation.abstraction(new Abstraction(p, r
              .abstraction().r, r.abstraction().i, r.abstraction().o)));
          return unit();
        }));
    ll.setClickable(true);
    ll.setOnClickListener($ -> listener.select(ll));
    ll.addView(make.f(Either3.z(unit())));
    return ll;
  }
}