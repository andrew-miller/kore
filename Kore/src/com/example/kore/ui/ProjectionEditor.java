package com.example.kore.ui;

import static com.example.kore.utils.CodeUtils.directPath;
import static com.example.kore.utils.CodeUtils.followPath;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation.Projection;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Optional;

public class ProjectionEditor extends FrameLayout {

  interface Listener {
    void select();

    void replace(List<Label> proj);
  }

  public ProjectionEditor(final Context context, final Projection p,
      final Code code, final CodeLabelAliasMap codeLabelAliases,
      final Listener listener) {
    super(context);
    notNull(p);
    LinearLayout ll = new LinearLayout(context);
    ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT));
    Button b = new Button(context);
    b.setText("~");
    b.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        ProjectionMenu.make(context, v, codeLabelAliases, code,
            followPath(p.path, code).some().x, new F<List<Label>, Void>() {
              public Void f(List<Label> proj) {
                listener.replace(proj);
                return null;
              }
            });
      }
    });
    ll.addView(b);
    List<Label> path = nil();
    for (Label l : iter(p.path)) {
      b = new Button(context);
      b.setOnClickListener(new OnClickListener() {
        public void onClick(View _) {
          listener.select();
        }
      });
      b.setWidth(0);
      b.setHeight(LayoutParams.MATCH_PARENT);
      b.setBackgroundColor((int) Long.parseLong(l.label.substring(0, 8), 16));
      Optional<String> a =
          codeLabelAliases.getAliases(
              new CanonicalCode(code, directPath(path, code))).get(l);
      if (!a.isNothing())
        b.setText(a.some().x);
      ll.addView(b);
      path = append(l, path);
    }
    HorizontalScrollView sv = new HorizontalScrollView(context);
    sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT));
    sv.addView(ll);
    addView(sv);
  }

}
