package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.DragEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Composition;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.List;
import com.example.kore.utils.Unit;

public class CompositionEditor extends FrameLayout {

  public static interface Listener {
    void select(Integer i);

    void insert(Integer i);

    void move(Integer src, Integer dest);
  }

  class Move {
    Integer i;
  }

  public CompositionEditor(Context context, Composition c,
      CodeLabelAliasMap codeLabelAliases, final Listener listener) {
    super(context);
    notNull(c, listener);
    LinearLayout ll = new LinearLayout(context);
    ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT));
    int i = 0;
    for (Either<Relation, List<Either3<Label, Integer, Unit>>> r : iter(c.l)) {
      final int i_ = i;
      Button b = new Button(context);
      b.setOnLongClickListener(new OnLongClickListener() {
        public boolean onLongClick(View v) {
          Move m = new Move();
          m.i = i_;
          v.startDrag(null, new DragShadowBuilder(), m, 0);
          ((Button) v).setText("*");
          return false;
        }
      });
      b.setOnDragListener(new OnDragListener() {
        public boolean onDrag(View v, DragEvent e) {
          if (e.getAction() == DragEvent.ACTION_DROP) {
            Integer dest = e.getX() < v.getWidth() / 2 ? i_ : i_ + 1;
            if (e.getLocalState() instanceof Move)
              listener.move(((Move) e.getLocalState()).i, dest);
            else
              listener.insert(dest);
          }
          return true;
        }
      });
      b.setWidth(0);
      b.setHeight(LayoutParams.MATCH_PARENT);
      b.setText(RelationUtils.renderRelation(r, codeLabelAliases));
      b.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          listener.select(i_);
        }
      });
      ll.addView(b);
      i++;
    }
    HorizontalScrollView sv = new HorizontalScrollView(getContext());
    sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
        LayoutParams.WRAP_CONTENT));
    sv.addView(ll);
    addView(sv);
  }
}
