package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.kore.codes.Label;
import com.example.kore.codes.Relation.Product;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.Unit;

public class ProductView {

  public static View make(Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      Integer aliasTextColor, Product product) {
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setBackgroundColor(color);
    for (Entry<Label, ? extends Object> e : iter(product.m.entrySet())) {
      LinearLayout ll2 = new LinearLayout(context);
      ll2.addView(LabelView.make(context, e.k, aliasTextColor));
      ll2.addView(make.f(Either3.<Label, Integer, Unit> x(e.k)));
      ll.addView(ll2);
    }
    if (product.m.entrySet().isEmpty()) {
      Button b = new Button(context);
      ll.addView(b);
    }
    return ll;
  }

}
