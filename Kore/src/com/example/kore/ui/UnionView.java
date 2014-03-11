package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.kore.codes.Label;
import com.example.kore.codes.Relation.Union;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.Unit;

public class UnionView {
  interface Listener {
    void insert(Integer i);

    void select();
  }

  public static View make(Context context,
      F<Either3<Label, Integer, Unit>, View> make, DragBro dragBro,
      Integer color, Integer highlightColor, Union union,
      final Listener listener) {
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);
    ll.setOrientation(LinearLayout.VERTICAL);
    boolean first = true;
    int i = 0;
    for (Object _ : iter(union.l)) {
      final int i_ = i;
      if (!first)
        ll.addView(DragDropEdges.makeSeparator(context, dragBro, color,
            highlightColor, false, new F<Object, Unit>() {
              public Unit f(Object o) {
                if (o instanceof ExtendRelation)
                  listener.insert(i_);
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
    if (union.l.isEmpty()) {
      Button b = new Button(context);
      ll.addView(b);
    }
    return ll;
  }
}
