package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.emptyRelationViewListener;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Space;

import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.OptionalUtils;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class RelationMenu {

  public static
      Pair<PopupWindow, ViewGroup>
      make(
          Context context,
          Relation relation,
          List<Either3<Label, Integer, Unit>> path,
          View v,
          RelationViewColors relationViewColors,
          CodeLabelAliasMap relationAliases,
          Bijection<CanonicalRelation, String> codeLabelAliases,
          List<Relation> relations,
          boolean ref,
          final F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Unit> select) {
    final Pair<PopupWindow, ViewGroup> p = UIUtils.makePopupWindow(context);
    p.x.showAsDropDown(v);
    if (ref) {
      p.y.addView(RelationRefView.make(context,
          OptionalUtils.<Pair<Integer, String>> nothing(), new F<Unit, Unit>() {
            public Unit f(Unit x) {
              p.x.dismiss();
              select.f(Either
                  .<Relation, List<Either3<Label, Integer, Unit>>> y(ListUtils
                      .<Either3<Label, Integer, Unit>> nil()));
              return unit();
            }
          }));
      Space s = new Space(context);
      s.setMinimumHeight(1);
      p.y.addView(s);
    }
    UIUtils.addEmptyRelationsToMenu(context, relationViewColors, p.y,
        new F<Relation, Unit>() {
          public Unit f(Relation r) {
            select.f(Either
                .<Relation, List<Either3<Label, Integer, Unit>>> x(r));
            return unit();
          }

        }, relation, path, relationAliases, codeLabelAliases, relations);
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
      p.y.addView(Overlay.make(context, RelationView.make(context,
          relationViewColors, new DragBro(), r,
          ListUtils.<Either3<Label, Integer, Unit>> nil(),
          emptyRelationViewListener, relationAliases, codeLabelAliases,
          relations), new Overlay.Listener() {
        public boolean onLongClick() {
          return false;
        }

        public void onClick() {
          select.f(Either.<Relation, List<Either3<Label, Integer, Unit>>> x(r));
        }
      }));
    }
    return p;
  }
}
