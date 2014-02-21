package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.example.kore.codes.Label;
import com.example.kore.codes.Relation.Projection;

public class ProjectionEditor extends FrameLayout {

  public ProjectionEditor(Context context, Projection p) {
    super(context);
    notNull(p);
    LinearLayout ll = new LinearLayout(getContext());
    ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT));
    for (Label l : iter(p.path)) {
      Button b = new Button(getContext());
      b.setWidth(0);
      b.setHeight(LayoutParams.MATCH_PARENT);
      b.setBackgroundColor((int) Long.parseLong(l.label.substring(0, 8), 16));
      ll.addView(b);
    }
    HorizontalScrollView sv = new HorizontalScrollView(getContext());
    sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT));
    sv.addView(ll);
    addView(sv);
  }

}
