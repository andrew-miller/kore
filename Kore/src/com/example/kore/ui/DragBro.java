package com.example.kore.ui;

import java.util.IdentityHashMap;

import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

/** Signals drag and drop, but only on the top View that this is tracking */
public class DragBro {
  private final IdentityHashMap<View, Listener> active =
      new IdentityHashMap<View, Listener>();
  private boolean current = false;

  public interface Listener {
    void dropped(Object o);

    void entered(Object o);

    void exited(Object o);
  }

  public void setDragListener(final View v, final Listener listener) {
    v.setOnDragListener(new OnDragListener() {
      // XXX haven't even bothered to find out why this works.
      // probably dependent on order of event delivery
      public boolean onDrag(View _, DragEvent e) {
        switch (e.getAction()) {
        case DragEvent.ACTION_DRAG_LOCATION:
          if (current)
            break;
        case DragEvent.ACTION_DRAG_ENTERED:
          for (java.util.Map.Entry<View, Listener> ent : active.entrySet())
            ent.getValue().exited(e.getLocalState());
          active.clear();
          active.put(v, listener);
          listener.entered(e.getLocalState());
          current = true;
          break;
        case DragEvent.ACTION_DRAG_EXITED:
        case DragEvent.ACTION_DRAG_ENDED:
          listener.exited(e.getLocalState());
          current = false;
          break;
        case DragEvent.ACTION_DROP:
          current = false;
          listener.dropped(e.getLocalState());
        }
        return true;
      }
    });
  }
}
