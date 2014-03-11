package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.kore.codes.Label;
import com.example.kore.codes.Relation.Composition;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.Unit;

public class CompositionView {
  interface Listener {
    void extend(Integer i);

    void select();
  }

  public static View make(Context context,
      F<Either3<Label, Integer, Unit>, View> make, DragBro dragBro,
      Integer color, Integer highlightColor, Composition composition,
      final Listener listener) {
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);
    boolean first = true;
    int i = 0;
    for (Object _ : iter(composition.l)) {
      final int i_ = i;
      if (!first)
        ll.addView(DragDropEdges.makeSeparator(context, dragBro, color,
            highlightColor, true, new F<Object, Unit>() {
              public Unit f(Object o) {
                if (o instanceof ExtendRelation)
                  listener.extend(i_);
                else
                  listener.select();
                return unit();
              }
            }, new F<Object, Boolean>() {
              public Boolean f(Object o) {
                return o instanceof SelectRelation
                    | o instanceof ExtendRelation;
              }
            }));
      first = false;
      ll.addView(make.f(Either3.<Label, Integer, Unit> y(i)));
      i++;
    }
    if (composition.l.isEmpty()) {
      Button b = new Button(context);
      ll.addView(b);
    }
    return ll;
  }
}
