package com.pokemon.kore.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.pokemon.kore.codes.Label;

public class LabelView {

  public static View make(Context context, Label k, Integer aliasTextColor) {
    Button b = new Button(context);
    b.setText(k.label.substring(0, 8) + "\n" + k.label.substring(8, 16) + "\n"
        + k.label.substring(16, 24) + "\n" + k.label.substring(24, 32));
    b.setTextColor(aliasTextColor);
    return b;
  }

}
