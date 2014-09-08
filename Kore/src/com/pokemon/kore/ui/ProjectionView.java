package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.relationAt;
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
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Unit;

public class ProjectionView {
  public static View make(Context context, Integer color,
      Integer aliasTextColor, Integer labelTextColor, Relation relation,
      List<Either3<Label, Integer, Unit>> path,
      CodeLabelAliasMap codeLabelAliases, Code argCode, F<View, Unit> select) {
    Relation r = relationAt(path, relation).some().x;
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);

    F<View, Unit> setLCL = v -> {
      v.setOnClickListener($ -> select.f(v));
      return unit();
    };

    List<Label> p = nil();
    for (Label l : iter(r.projection().path)) {
      Optional<String> ola =
          codeLabelAliases.getAliases(new CanonicalCode(argCode, p)).xy.get(l);
      View v =
          LabelView.make(context,
              ola.isNothing() ? Either.x(l) : Either.y(ola.some().x),
              aliasTextColor, labelTextColor);
      setLCL.f(v);
      ll.addView(v);
      p = append(l, p);
    }
    if (r.projection().path.isEmpty()) {
      Button b = new Button(context);
      setLCL.f(b);
      ll.addView(b);
    }
    return ll;
  }
}