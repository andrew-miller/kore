package com.example.kore.ui;

import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.kore.utils.F;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class RelationRefView {
  public static View make(Context context,
      Optional<Pair<Integer, String>> label, final F<Unit, Unit> selected) {
    Button b = new Button(context);
    b.setBackgroundColor(0xFF000000);
    if (!label.isNothing()) {
      b.setText(label.some().x.y);
      b.setTextColor(label.some().x.x);
    }
    b.setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        selected.f(unit());
      }
    });
    return b;
  }
}
