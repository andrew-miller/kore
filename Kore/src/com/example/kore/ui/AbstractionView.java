package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Tag;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Unit;

public class AbstractionView {
  interface Listener {
    void replace(Pattern p);

    void changeRelationType(Tag t);
  }

  public static View make(final Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      Integer aliasTextColor, final Relation relation,
      final List<Either3<Label, Integer, Unit>> path,
      CodeLabelAliasMap codeLabelAliases, final Listener listener) {
    Relation r = relationAt(path, relation).some().x;
    final LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);
    ll.addView(PatternView.make(context, aliasTextColor,
        r.abstraction().pattern, r.abstraction().i, ListUtils.<Label> nil(),
        codeLabelAliases, new F<Pattern, Unit>() {
          public Unit f(Pattern p) {
            listener.replace(p);
            return unit();
          }
        }));
    ll.setClickable(true);
    ll.setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        PopupMenu pm = new PopupMenu(context, ll);
        UIUtils.addRelationTypesToMenu(pm.getMenu(),
            new F<Relation.Tag, Unit>() {
              public Unit f(Tag t) {
                listener.changeRelationType(t);
                return unit();
              }
            }, relation, path);
        pm.show();
      }
    });
    ll.addView(make.f(Either3.<Label, Integer, Unit> z(unit())));
    return ll;
  }
}