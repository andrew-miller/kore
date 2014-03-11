package com.example.kore.ui;

import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation.Label_;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Unit;

public class Label_View {
  public static View make(final Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      Integer aliasTextColor, final Label_ label, Optional<String> labelAlias,
      final CodeLabelAliasMap codeLabelAliases, final F<Label, Unit> replace) {
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);

    final View v;
    if (labelAlias.isNothing())
      v = LabelView.make(context, label.label, aliasTextColor);
    else {
      Button b = new Button(context);
      b.setText(labelAlias.some().x);
      v = b;
    }
    v.setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        LabelMenu.make(context, v, codeLabelAliases, new CanonicalCode(label.o,
            ListUtils.<Label> nil()), new F<Label, Void>() {
          public Void f(Label l) {
            replace.f(l);
            return null;
          }
        });
      }
    });

    ll.addView(v);
    ll.addView(make.f(Either3.<Label, Integer, Unit> z(unit())));
    return ll;
  }
}
