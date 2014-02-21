package com.example.kore.ui;

import com.example.kore.codes.Relation;
import com.example.kore.utils.List;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public class ValueEditor {
  interface Tree {
    List<Tree> children();
  }

  public static View make(Context c, Relation r) {
    LinearLayout ll = new LinearLayout(c);
    return ll;
  }

}
