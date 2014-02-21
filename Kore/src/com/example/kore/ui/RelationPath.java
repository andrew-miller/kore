package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.kore.R;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.utils.Boom;
import com.example.kore.utils.Either3;
import com.example.kore.utils.List;
import com.example.kore.utils.Unit;

public class RelationPath extends FrameLayout {

  interface Listener {
    void pathSelected(List<Either3<Label, Integer, Unit>> p);
  }

  public RelationPath(Context context, final Listener listener,
      Relation relation, List<Either3<Label, Integer, Unit>> path) {
    super(context);
    notNull(listener, relation, path);
    View v = LayoutInflater.from(context).inflate(R.layout.path, this, true);
    ViewGroup pathVG = (ViewGroup) v.findViewById(R.id.layout_path);

    List<Either3<Label, Integer, Unit>> subpath = nil();
    while (true) {
      Button b = new Button(context);
      switch (relation.tag) {
      case PRODUCT:
        b.setText("{}");
        break;
      case UNION:
        b.setText("[]");
        break;
      case ABSTRACTION:
        b.setText("->");
        break;
      case COMPOSITION:
        b.setText("|");
        break;
      case PROJECTION:
        b.setText(".");
        break;
      case LABEL:
        b.setText("'");
        break;
      default:
        throw Boom.boom();
      }
      b.setWidth(0);
      b.setHeight(LayoutParams.MATCH_PARENT);
      final List<Either3<Label, Integer, Unit>> subpathS = subpath;
      b.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          listener.pathSelected(subpathS);
        }
      });
      pathVG.addView(b);
      if (path.isEmpty())
        break;
      Either3<Label, Integer, Unit> e = path.cons().x;
      path = path.cons().tail;
      subpath = append(e, subpath);
      relation = RelationUtils.subRelation(relation, e).some().x;
      Button b2 = new Button(context);
      switch (e.tag) {
      case X:
        b2.setBackgroundColor((int) Long.parseLong(e.x().label.substring(0, 8),
            16));
        break;
      case Y:
        b2.setText("" + e.y());
        break;
      case Z:
        b2.setText("-");
        break;
      }
      b2.setWidth(0);
      b2.setHeight(LayoutParams.MATCH_PARENT);
      pathVG.addView(b2);
    }

  }
}
