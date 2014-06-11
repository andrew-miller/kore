package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Abstraction;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class AbstractionView {
  interface Listener {
    void replace(Relation r);
  }

  public static View make(final Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      Integer aliasTextColor, final Relation relation,
      final List<Either3<Label, Integer, Unit>> path,
      final CodeLabelAliasMap codeLabelAliases,
      final RelationViewColors relationViewColors,
      final Bijection<CanonicalRelation, String> relationAliases,
      final Listener listener) {
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
        final Pair<PopupWindow, ViewGroup> p = UIUtils.makePopupWindow(context);
        p.x.showAsDropDown(ll);
        UIUtils.addEmptyRelationsToMenu(context, relationViewColors, p.y,
            new F<Relation, Unit>() {
              public Unit f(Relation r) {
                p.x.dismiss();
                listener.replace(r);
                return unit();
              }
            }, relation, path, codeLabelAliases, relationAliases);
      }
    });
    ll.addView(make.f(Either3.<Label, Integer, Unit> z(unit())));
    return ll;
  }
}