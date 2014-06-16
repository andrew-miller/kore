package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Abstraction;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Unit;

public class AbstractionView {
  interface Listener {
    void replace(Relation r);

    void select(View v);
  }

  public static View make(final Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      Integer aliasTextColor, final Relation relation,
      final List<Either3<Label, Integer, Unit>> path,
      final CodeLabelAliasMap codeLabelAliases, final Listener listener) {
    final Relation r = relationAt(path, relation).some().x;
    final LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);
    ll.addView(PatternView.make(context, aliasTextColor,
        r.abstraction().pattern, r.abstraction().i, ListUtils.<Label> nil(),
        codeLabelAliases, new F<Pattern, Unit>() {
          public Unit f(Pattern p) {
            listener.replace(Relation.abstraction(new Abstraction(p, r
                .abstraction().r, r.abstraction().i, r.abstraction().o)));
            return unit();
          }
        }));
    ll.setClickable(true);
    ll.setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        listener.select(ll);
      }
    });
    ll.addView(make.f(Either3.<Label, Integer, Unit> z(unit())));
    return ll;
  }
}