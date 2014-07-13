package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.PairUtils.pair;
import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;

import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public class DragDropEdges {
  public static View makeSeparator(Context context, DragBro dragBro,
      final Integer color, final Integer highlightedColor, Boolean vertical,
      final F<Object, Unit> dropped, final F<Object, Boolean> match) {
    final LinearLayout ll = new LinearLayout(context);
    ll.setPadding(10, 10, 0, 0);
    ll.setBackgroundColor(color);
    if (vertical)
      ll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
          LayoutParams.MATCH_PARENT));
    dragBro.setDragListener(ll, new DragBro.Listener() {
      public void exited(Object o) {
        if (match.f(o))
          ll.setBackgroundColor(color);
      }

      public void entered(Object o) {
        if (match.f(o))
          ll.setBackgroundColor(highlightedColor);
      }

      public void dropped(Object o) {
        if (match.f(o)) {
          ll.setBackgroundColor(color);
          dropped.f(o);
        }
      }
    });
    return ll;
  }

  public enum Side {
    LEFT, TOP, RIGHT, BOTTOM
  }

  public static View
      make(Context context, DragBro dragBro, View v, final Integer color,
          final Integer highlightedColor,
          final F<Pair<Side, Object>, Unit> dropped,
          final F<Object, Boolean> match) {
    FrameLayout middle = new FrameLayout(context);
    middle.setPadding(0, 0, 0, 0);
    middle.setBackgroundColor(color);
    middle.addView(v);
    dragBro.setDragListener(middle, new DragBro.Listener() {
      public void exited(Object o) {
      }

      public void entered(Object o) {
      }

      public void dropped(Object o) {
      }
    });
    final FrameLayout left = new FrameLayout(context);
    left.setPadding(10, 0, 0, 0);
    left.setBackgroundColor(color);
    left.addView(middle);
    dragBro.setDragListener(left, new DragBro.Listener() {
      public void exited(Object o) {
        if (match.f(o))
          left.setBackgroundColor(color);
      }

      public void entered(Object o) {
        if (match.f(o))
          left.setBackgroundColor(highlightedColor);
      }

      public void dropped(Object o) {
        if (match.f(o)) {
          left.setBackgroundColor(color);
          dropped.f(pair(Side.LEFT, o));
        }
      }
    });
    final FrameLayout right = new FrameLayout(context);
    right.setPadding(0, 0, 10, 0);
    right.setBackgroundColor(color);
    dragBro.setDragListener(right, new DragBro.Listener() {
      public void exited(Object o) {
        if (match.f(o))
          right.setBackgroundColor(color);
      }

      public void entered(Object o) {
        if (match.f(o))
          right.setBackgroundColor(highlightedColor);
      }

      public void dropped(Object o) {
        if (match.f(o)) {
          right.setBackgroundColor(color);
          dropped.f(pair(Side.RIGHT, o));
        }
      }
    });
    final FrameLayout top = new FrameLayout(context);
    top.setPadding(0, 10, 0, 0);
    top.setBackgroundColor(color);
    dragBro.setDragListener(top, new DragBro.Listener() {
      public void exited(Object o) {
        if (match.f(o))
          top.setBackgroundColor(color);
      }

      public void entered(Object o) {
        if (match.f(o))
          top.setBackgroundColor(highlightedColor);
      }

      public void dropped(Object o) {
        if (match.f(o)) {
          top.setBackgroundColor(color);
          dropped.f(pair(Side.TOP, o));
        }
      }
    });
    final FrameLayout bottom = new FrameLayout(context);
    bottom.setPadding(0, 0, 0, 10);
    bottom.setBackgroundColor(color);
    dragBro.setDragListener(bottom, new DragBro.Listener() {
      public void exited(Object o) {
        if (match.f(o))
          bottom.setBackgroundColor(color);
      }

      public void entered(Object o) {
        if (match.f(o))
          bottom.setBackgroundColor(highlightedColor);
      }

      public void dropped(Object o) {
        if (match.f(o)) {
          bottom.setBackgroundColor(color);
          dropped.f(pair(Side.BOTTOM, o));
        }
      }
    });
    right.addView(left);
    top.addView(right);
    bottom.addView(top);
    return bottom;
  }
}