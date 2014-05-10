package com.example.kore.ui;

import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation.Projection;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Unit;

public class ProjectionView {

  public static View make(final Context context, Integer color,
      Integer aliasTextColor, final Projection projection,
      final CodeLabelAliasMap codeLabelAliases, final Code argCode,
      final F<List<Label>, Unit> replace) {
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);

    F<View, Unit> setLCL = new F<View, Unit>() {
      public Unit f(final View v) {
        v.setOnClickListener(new OnClickListener() {
          public void onClick(View _) {
            ProjectionMenu.make(context, v, codeLabelAliases, argCode,
                reroot(argCode, projection.path), new F<List<Label>, Void>() {
                  public Void f(List<Label> p) {
                    replace.f(p);
                    return null;
                  }
                });
          }
        });
        return unit();
      }
    };

    List<Label> path = nil();
    for (Label l : iter(projection.path)) {
      Optional<String> ola =
          codeLabelAliases.getAliases(new CanonicalCode(argCode, path)).xy
              .get(l);
      View v;
      if (ola.isNothing())
        v = LabelView.make(context, l, aliasTextColor);
      else {
        Button b = new Button(context);
        b.setText(ola.some().x);
        v = b;
      }
      setLCL.f(v);
      ll.addView(v);
      path = append(l, path);
    }
    if (projection.path.isEmpty()) {
      Button b = new Button(context);
      setLCL.f(b);
      ll.addView(b);
    }
    return ll;
  }

}
