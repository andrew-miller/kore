package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.codomain;
import static com.example.kore.ui.RelationUtils.domain;
import static com.example.kore.ui.RelationUtils.inAbstraction;
import static com.example.kore.ui.RelationUtils.subRelation;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Tag;
import com.example.kore.utils.Boom;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class RelationPath extends FrameLayout {

  interface Listener {
    void selectPath(List<Either3<Label, Integer, Unit>> p);

    void changeRelationType(Tag y);
  }

  public RelationPath(final Context context, final Listener listener,
      final Relation rootRelation,
      final List<Either3<Label, Integer, Unit>> path) {
    super(context);
    notNull(listener, rootRelation, path);
    View v = LayoutInflater.from(context).inflate(R.layout.path, this, true);
    ViewGroup pathVG = (ViewGroup) v.findViewById(R.id.layout_path);

    Relation relation = rootRelation;
    List<Either3<Label, Integer, Unit>> path_ = path;
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
      final Relation relation_ = relation;
      b.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          if (subpathS.equals(path)) {
            PopupMenu pm = new PopupMenu(context, v);
            final Menu m = pm.getMenu();
            F<Pair<String, Tag>, Void> add = new F<Pair<String, Tag>, Void>() {
              public Void f(final Pair<String, Tag> p) {
                m.add(p.x).setOnMenuItemClickListener(
                    new OnMenuItemClickListener() {
                      public boolean onMenuItemClick(MenuItem _) {
                        listener.changeRelationType(p.y);
                        return true;
                      }
                    });
                return null;
              }
            };
            add.f(Pair.pair("[]", Tag.UNION));
            if (equal(domain(relation_), unit)) {
              if (codomain(relation_).tag == Code.Tag.PRODUCT)
                add.f(Pair.pair("{}", Tag.PRODUCT));
              if (codomain(relation_).tag == Code.Tag.UNION)
                add.f(Pair.pair("'", Tag.LABEL));
            }
            add.f(Pair.pair("->", Tag.ABSTRACTION));
            add.f(Pair.pair("|", Tag.COMPOSITION));
            if (inAbstraction(path, rootRelation))
              add.f(Pair.pair(".", Tag.PROJECTION));
            pm.show();
          } else
            listener.selectPath(subpathS);
        }
      });
      pathVG.addView(b);
      if (path_.isEmpty())
        break;
      Either3<Label, Integer, Unit> e = path_.cons().x;
      path_ = path_.cons().tail;
      subpath = append(e, subpath);
      relation = subRelation(relation, e).some().x;
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
