package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.relationAt;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Unit;

public class CompositionView {
  interface Listener {
    void extend(Integer i);

    /** traverse into the composition */
    void select2();

    void select(View v);
  }

  public static View make(Context context,
      F<Either3<Label, Integer, Unit>, View> make, DragBro dragBro,
      Integer color, Integer highlightColor, Relation relation,
      List<Either3<Label, Integer, Unit>> path, Listener listener) {
    Relation r = relationAt(path, relation).some().x;
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);
    boolean first = true;
    int i = 0;
    for (Object $ : iter(r.composition().l)) {
      int i_ = i;
      if (!first)
        ll.addView(DragDropEdges.makeSeparator(context, dragBro, color,
            highlightColor, true, o -> {
              if (o instanceof ExtendRelation)
                listener.extend(i_);
              else
                listener.select2();
              return unit();
            }, o -> o instanceof SelectRelation
                | o instanceof ExtendRelation
        ));
      first = false;
      ll.addView(make.f(Either3.y(i)));
      i++;
    }
    if (r.composition().l.isEmpty()) {
      Button b = new Button(context);
      b.setOnClickListener($ -> listener.select(b));
      ll.addView(b);
    }
    return ll;
  }
}