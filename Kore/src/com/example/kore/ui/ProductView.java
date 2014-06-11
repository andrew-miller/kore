package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class ProductView {
  public static View make(final Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      Integer aliasTextColor, final Relation relation,
      final List<Either3<Label, Integer, Unit>> path,
      final CodeLabelAliasMap codeLabelAliases,
      final Bijection<CanonicalRelation, String> relationAliases,
      final RelationViewColors relationViewColors,
      final F<Relation, Unit> replace) {
    Relation r = relationAt(path, relation).some().x;
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setBackgroundColor(color);
    Bijection<Label, String> las =
        codeLabelAliases.getAliases(new CanonicalCode(r.product().o, ListUtils
            .<Label> nil()));
    for (Pair<Label, ?> e : iter(r.product().m.entrySet())) {
      LinearLayout ll2 = new LinearLayout(context);
      Optional<String> ola = las.xy.get(e.x);
      if (ola.isNothing())
        ll2.addView(LabelView.make(context, e.x, aliasTextColor));
      else {
        Button b = new Button(context);
        b.setText(ola.some().x);
        ll2.addView(b);
      }
      ll2.addView(make.f(Either3.<Label, Integer, Unit> x(e.x)));
      ll.addView(ll2);
    }
    if (r.product().m.entrySet().isEmpty()) {
      final Button b = new Button(context);
      b.setOnClickListener(new OnClickListener() {
        public void onClick(View _) {
          final Pair<PopupWindow, ViewGroup> p =
              UIUtils.makePopupWindow(context);
          p.x.showAsDropDown(b);
          UIUtils.addEmptyRelationsToMenu(context, relationViewColors, p.y,
              new F<Relation, Unit>() {
                public Unit f(Relation r) {
                  p.x.dismiss();
                  replace.f(r);
                  return unit();
                }
              }, relation, path, codeLabelAliases, relationAliases);
        }
      });
      ll.addView(b);
    }
    return ll;
  }
}