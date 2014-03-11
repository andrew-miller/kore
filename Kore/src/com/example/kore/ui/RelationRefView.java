package com.example.kore.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.example.kore.codes.Label;
import com.example.kore.utils.Either3;
import com.example.kore.utils.List;
import com.example.kore.utils.Unit;

public class RelationRefView {
  public static View
      make(Context context, List<Either3<Label, Integer, Unit>> p) {
    Button b = new Button(context);
    b.setBackgroundColor(0xFF000000);
    return b;
  }
}
