package com.pokemon.kore.ui;

import android.content.Context;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.widget.Button;
import android.widget.RelativeLayout;

public class Overlay {
  interface Listener {
    void onClick();

    boolean onLongClick();
  }

  public static View make(final Context context, final View v,
      final Listener listener) {
    final OnLayoutChangeListener[] l = { null };
    final RelativeLayout rl = new RelativeLayout(context);
    rl.addView(v);
    l[0] = (arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8) -> {
      // Adding views to rl doesn't work at this point. Dunno why.
      // Doing it in post instead works.
      rl.post(() -> {
        Button b = new Button(context);
        b.setMinWidth(v.getWidth());
        b.setMinHeight(v.getHeight());
        b.setBackgroundColor(0);
        b.setOnClickListener($ -> listener.onClick());
        b.setOnLongClickListener($ -> listener.onLongClick());
        rl.addView(b);
      });
      rl.removeOnLayoutChangeListener(l[0]);
    };
    rl.addOnLayoutChangeListener(l[0]);
    return rl;
  }
}
