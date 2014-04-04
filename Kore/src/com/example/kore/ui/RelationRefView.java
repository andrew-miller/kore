package com.example.kore.ui;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.kore.codes.Label;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Unit;
import static com.example.kore.utils.Unit.unit;

public class RelationRefView {
  public static View
      make(Context context, List<Either3<Label, Integer, Unit>> p,
           final F<Unit, Unit> selected) {
    Button b = new Button(context);
    b.setBackgroundColor(0xFF000000);
    b.setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        selected.f(unit());
      }
    });
    return b;
  }
}
