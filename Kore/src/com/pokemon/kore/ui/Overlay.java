package com.pokemon.kore.ui;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnLongClickListener;
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
    l[0] = new OnLayoutChangeListener() {
      public void onLayoutChange(View arg0, int arg1, int arg2, int arg3,
          int arg4, int arg5, int arg6, int arg7, int arg8) {
        // Adding views to rl doesn't work at this point. Dunno why.
        // Doing it in post instead works.
        rl.post(new Runnable() {
          public void run() {
            Button b = new Button(context);
            b.setMinWidth(v.getWidth());
            b.setMinHeight(v.getHeight());
            b.setBackgroundColor(0);
            b.setOnClickListener(new OnClickListener() {
              public void onClick(View _) {
                listener.onClick();
              }
            });
            b.setOnLongClickListener(new OnLongClickListener() {
              public boolean onLongClick(View arg0) {
                return listener.onLongClick();
              }
            });
            rl.addView(b);
          }
        });
        rl.removeOnLayoutChangeListener(l[0]);
      }
    };
    rl.addOnLayoutChangeListener(l[0]);
    return rl;
  }
}
