package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation.Product;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Map;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class ProductView {

  public static View make(Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      Integer aliasTextColor, Product product,
      CodeLabelAliasMap codeLabelAliases) {
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setBackgroundColor(color);
    Map<Label, String> las =
        codeLabelAliases.getAliases(new CanonicalCode(product.o, ListUtils
            .<Label> nil()));
    for (Pair<Label, ?> e : iter(product.m.entrySet())) {
      LinearLayout ll2 = new LinearLayout(context);
      Optional<String> ola = las.get(e.x);
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
    if (product.m.entrySet().isEmpty()) {
      Button b = new Button(context);
      ll.addView(b);
    }
    return ll;
  }

}
