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
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Tag;
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
      CodeLabelAliasMap codeLabelAliases,
      final RelationViewColors relationViewColors,
      final F<Relation.Tag, Unit> changeRelationType) {
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
          Pair<PopupWindow, ViewGroup> p = UIUtils.makePopupWindow(context);
          p.x.showAsDropDown(b);
          UIUtils.addRelationTypesToMenu(context, relationViewColors, p.y,
              new F<Relation.Tag, Unit>() {
                public Unit f(Tag t) {
                  changeRelationType.f(t);
                  return unit();
                }
              }, relation, path);
        }
      });
      ll.addView(b);
    }
    return ll;
  }
}