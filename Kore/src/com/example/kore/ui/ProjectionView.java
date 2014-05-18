package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Tag;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Unit;

public class ProjectionView {
  interface Listener {
    void replace(List<Label> p);

    void changeRelationType(Tag t);
  }

  public static View make(final Context context, Integer color,
      Integer aliasTextColor, final Relation relation,
      final List<Either3<Label, Integer, Unit>> path,
      final CodeLabelAliasMap codeLabelAliases, final Code argCode,
      final Listener listener) {
    final Relation r = relationAt(path, relation).some().x;
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);

    F<View, Unit> setLCL = new F<View, Unit>() {
      public Unit f(final View v) {
        v.setOnClickListener(new OnClickListener() {
          public void onClick(View _) {
            PopupMenu pm = new PopupMenu(context, v);
            Menu m = pm.getMenu();
            UIUtils.addProjectionsToMenu(m, context, v, codeLabelAliases,
                argCode, reroot(argCode, r.projection().path),
                new F<List<Label>, Void>() {
                  public Void f(List<Label> p) {
                    listener.replace(p);
                    return null;
                  }
                });
            m.add("---");
            UIUtils.addRelationTypesToMenu(m, new F<Relation.Tag, Unit>() {
              public Unit f(Tag t) {
                listener.changeRelationType(t);
                return unit();
              }
            }, relation, path);
            pm.show();
          }
        });
        return unit();
      }
    };

    List<Label> p = nil();
    for (Label l : iter(r.projection().path)) {
      Optional<String> ola =
          codeLabelAliases.getAliases(new CanonicalCode(argCode, p)).xy.get(l);
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
      p = append(l, p);
    }
    if (r.projection().path.isEmpty()) {
      Button b = new Button(context);
      setLCL.f(b);
      ll.addView(b);
    }
    return ll;
  }
}