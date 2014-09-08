package com.pokemon.kore.ui;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.pokemon.kore.codes.Label;
import com.pokemon.kore.utils.Either;

public class LabelView {

  public static View make(Context context, Either<Label, String> k,
      Integer aliasTextColor, Integer labelTextColor) {
    Button b = new Button(context);
    switch (k.tag) {
    case Y:
      b.setTextColor(aliasTextColor);
      b.setText(k.y());
      break;
    case X:
      b.setText(k.x().label.substring(0, 8) + "\n"
          + k.x().label.substring(8, 16) + "\n" + k.x().label.substring(16, 24)
          + "\n" + k.x().label.substring(24, 32));
      b.setTextColor(labelTextColor);
      break;
    }
    return b;
  }
}