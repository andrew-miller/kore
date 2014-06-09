package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.emptyRelationViewListener;
import static com.example.kore.ui.RelationUtils.linkTree;
import static com.example.kore.ui.RelationUtils.replaceRelationOrPathAt;
import static com.example.kore.ui.RelationUtils.resolve;
import static com.example.kore.ui.RelationUtils.subRelationOrPath;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.LinkTreeUtils.validLinkTree;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Space;

import com.example.kore.R;
import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Tag;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.OptionalUtils;
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
      Bijection<CanonicalRelation, String> relationAliases,
      List<Either3<Label, Integer, Unit>> path, int referenceColor,
      RelationViewColors rvc) {
    View v = LayoutInflater.from(context).inflate(R.layout.path, null);
    ViewGroup vg = (ViewGroup) v.findViewById(R.id.layout_path);
    make(context, rc, listener, root, relations, codeLabelAliases,
        relationAliases, ListUtils.<Either3<Label, Integer, Unit>> nil(), path,
        Either.<Relation, List<Either3<Label, Integer, Unit>>> x(root), vg,
        referenceColor, rvc);
    return v;
  }

  private static void make(final Context context, RelationColors rc,
      final Listener listener, final Relation root,
      final List<Relation> relations, final CodeLabelAliasMap codeLabelAliases,
      final Bijection<CanonicalRelation, String> relationAliases,
      final List<Either3<Label, Integer, Unit>> before,
      final List<Either3<Label, Integer, Unit>> after,
      final Either<Relation, List<Either3<Label, Integer, Unit>>> rp,
      ViewGroup vg, int referenceColor, final RelationViewColors rvc) {
    final Relation r = resolve(root, rp);
    final Button b = new Button(context);
    b.setBackgroundColor(rp.tag == rp.tag.Y ? referenceColor : rc.m.get(
        rp.x().tag).some().x.x);
    b.setWidth(0);
    b.setHeight(LayoutParams.MATCH_PARENT);
    b.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (after.isEmpty()) {
          if (validLinkTree(linkTree(replaceRelationOrPathAt(
              root,
              before,
              Either
                  .<Relation, List<Either3<Label, Integer, Unit>>> x(RelationUtils
                      .dummy(unit, unit)))))) {
            Pair<PopupWindow, ViewGroup> p = UIUtils.makePopupWindow(context);
            UIUtils.addRelationTypesToMenu(context, rvc, p.y,
                new F<Relation.Tag, Unit>() {
                  public Unit f(Tag t) {
                    listener.changeRelationType(t);
                    return unit();
                  }
                }, root, before);
            Space s = new Space(context);
            s.setMinimumHeight(1);
            p.y.addView(s);
            boolean first = true;
            for (final Relation r : iter(relations)) {
              if (!first) {
                Space s2 = new Space(context);
                s2.setMinimumHeight(1);
                p.y.addView(s2);
              }
              first = false;
              p.y.addView(Overlay.make(context,
                  RelationView.make(context, rvc, new DragBro(), r,
                      ListUtils.<Either3<Label, Integer, Unit>> nil(),
                      emptyRelationViewListener, codeLabelAliases,
                      relationAliases), new Overlay.Listener() {
                    public boolean onLongClick() {
                      return false;
                    }

                    public void onClick() {
                      listener.replaceRelation(Either
                          .<Relation, List<Either3<Label, Integer, Unit>>> x(r));
                    }
                  }));
            }
            if (!before.isEmpty()) {
              Space s3 = new Space(context);
              s3.setMinimumHeight(1);
              p.y.addView(s3);
              p.y.addView(RelationRefView.make(context,
                  OptionalUtils.<Pair<Integer, String>> nothing(),
                  new F<Unit, Unit>() {
                    public Unit f(Unit x) {
                      listener.replaceRelation(Either
                          .<Relation, List<Either3<Label, Integer, Unit>>> y(ListUtils
                              .<Either3<Label, Integer, Unit>> nil()));
                      return unit();
                    }
                  }));
            }
            p.x.showAsDropDown(v);
          }
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
    if (rp.tag == rp.tag.X & !after.isEmpty()) {
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
          subRelationOrPath(rp.x(), e).some().x, vg, referenceColor, rvc);
    }
  }
}