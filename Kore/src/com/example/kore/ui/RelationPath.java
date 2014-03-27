package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.codomain;
import static com.example.kore.ui.RelationUtils.domain;
import static com.example.kore.ui.RelationUtils.inAbstraction;
import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.ui.RelationUtils.subRelationOrPath;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupMenu;

import com.example.kore.R;
import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Tag;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Map;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class RelationPath {
  interface Listener {
    void selectPath(List<Either3<Label, Integer, Unit>> p);

    void changeRelationType(Tag y);

    void replaceRelation(
        Either<Relation, List<Either3<Label, Integer, Unit>>> er);
  }

  public static View make(final Context context, RelationColors rc,
      Listener listener, Relation root, List<Relation> relations,
      CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalRelation, String> relationAliases,
      List<Either3<Label, Integer, Unit>> path) {
    View v = LayoutInflater.from(context).inflate(R.layout.path, null);
    ViewGroup vg = (ViewGroup) v.findViewById(R.id.layout_path);
    make(context, rc, listener, root, relations, codeLabelAliases,
        relationAliases, ListUtils.<Either3<Label, Integer, Unit>> nil(), path,
        Either.<Relation, List<Either3<Label, Integer, Unit>>> x(root), vg);
    return v;
  }

  public static void make(final Context context, RelationColors rc,
      final Listener listener, final Relation root,
      final List<Relation> relations, final CodeLabelAliasMap codeLabelAliases,
      final Map<CanonicalRelation, String> relationAliases,
      final List<Either3<Label, Integer, Unit>> before,
      final List<Either3<Label, Integer, Unit>> after,
      final Either<Relation, List<Either3<Label, Integer, Unit>>> rp,
      ViewGroup vg) {
    final Relation r = rp.isY() ? relationAt(rp.y(), root).some().x : rp.x();
    final Button b = new Button(context);
    b.setBackgroundColor(rp.isY() ? 0xFF000000 // TODO unhardcode
        : rc.m.get(rp.x().tag).some().x.x);
    b.setWidth(0);
    b.setHeight(LayoutParams.MATCH_PARENT);
    b.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (after.isEmpty()) {
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
          if (equal(domain(r), unit)) {
            if (codomain(r).tag == Code.Tag.PRODUCT)
              add.f(Pair.pair("{}", Tag.PRODUCT));
            if (codomain(r).tag == Code.Tag.UNION)
              add.f(Pair.pair("'", Tag.LABEL));
          }
          add.f(Pair.pair("->", Tag.ABSTRACTION));
          add.f(Pair.pair("|", Tag.COMPOSITION));
          if (inAbstraction(root, before))
            add.f(Pair.pair(".", Tag.PROJECTION));
          m.add("---");
          for (final Relation r : iter(relations))
            UIUtils.addRelationToMenu(m, r,
                ListUtils.<Either3<Label, Integer, Unit>> nil(),
                codeLabelAliases, relationAliases,
                new F<List<Either3<Label, Integer, Unit>>, Unit>() {
                  public Unit f(List<Either3<Label, Integer, Unit>> p) {
                    listener.replaceRelation(Either
                        .<Relation, List<Either3<Label, Integer, Unit>>> x(r));
                    return unit();
                  }
                });
          if (!(before.isEmpty() & after.isEmpty())) {
            m.add("---");
            UIUtils.addRelationToMenu(m, root,
                ListUtils.<Either3<Label, Integer, Unit>> nil(),
                codeLabelAliases, relationAliases,
                new F<List<Either3<Label, Integer, Unit>>, Unit>() {
                  public Unit f(List<Either3<Label, Integer, Unit>> p) {
                    listener.replaceRelation(Either
                        .<Relation, List<Either3<Label, Integer, Unit>>> y(p));
                    return unit();
                  }
                });
          }
          pm.show();
        } else
          listener.selectPath(before);
      }
    });
    b.setOnTouchListener(new OnTouchListener() {
      public boolean onTouch(View _, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_CANCEL)
          b.startDrag(null, new DragShadowBuilder(), new SelectRelation(), 0);
        return false;
      }
    });
    vg.addView(b);
    Button b2 = new Button(context);
    if (!rp.isY() & !after.isEmpty()) {
      Either3<Label, Integer, Unit> e = after.cons().x;
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
      vg.addView(b2);
      make(context, rc, listener, root, relations, codeLabelAliases,
          relationAliases, append(e, before), after.cons().tail,
          subRelationOrPath(rp.x(), e).some().x, vg);
    }
  }
}
