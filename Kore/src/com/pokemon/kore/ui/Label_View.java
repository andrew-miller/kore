package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.relationAt;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Unit;

public class Label_View {

  public static View make(final Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      final Relation relation, final List<Either3<Label, Integer, Unit>> path,
      Integer aliasTextColor, final CodeLabelAliasMap codeLabelAliases,
      final F<View, Unit> select) {
    final Relation r = relationAt(path, relation).some().x;
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);

    final View v;
    Optional<String> ola =
        codeLabelAliases.getAliases(new CanonicalCode(r.label().o,
            nil())).xy.get(r.label().label);
    if (ola.isNothing())
      v = LabelView.make(context, r.label().label, aliasTextColor);
    else {
      Button b = new Button(context);
      b.setText(ola.some().x);
      v = b;
    }
    v.setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        select.f(v);
      }
    });

    ll.addView(v);
    ll.addView(make.f(Either3.z(unit())));
    return ll;
  }
}