package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public class RelationRefView {
  public static View make(Context context, Integer color,
      Optional<Pair<Integer, String>> label, F<Unit, Unit> selected) {
    Button b = new Button(context);
    b.setBackgroundColor(color);
    if (!label.isNothing()) {
      b.setText(label.some().x.y);
      b.setTextColor(label.some().x.x);
    }
    b.setOnClickListener($ -> selected.f(unit()));
    return b;
  }
}
