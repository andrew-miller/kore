package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Tag;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Unit;

public class CompositionView {
  interface Listener {
    void extend(Integer i);

    void select();

    void changeRelationType(Tag t);
  }

  public static View make(final Context context,
      F<Either3<Label, Integer, Unit>, View> make, DragBro dragBro,
      Integer color, Integer highlightColor, final Relation relation,
      final List<Either3<Label, Integer, Unit>> path, final Listener listener) {
    Relation r = relationAt(path, relation).some().x;
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);
    boolean first = true;
    int i = 0;
    for (Object _ : iter(r.composition().l)) {
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
    if (r.composition().l.isEmpty()) {
      final Button b = new Button(context);
      b.setOnClickListener(new OnClickListener() {
        public void onClick(View _) {
          PopupMenu pm = new PopupMenu(context, b);
          Menu m = pm.getMenu();
          UIUtils.addRelationTypesToMenu(m, new F<Relation.Tag, Unit>() {
            public Unit f(Tag t) {
              listener.changeRelationType(t);
              return unit();
            }
          }, relation, path);
          pm.show();
        }
      });
      ll.addView(b);
    }
    return ll;
  }
}