package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.emptyRelationViewListener;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Space;

import com.pokemon.kore.codes.CanonicalRelation;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public class RelationMenu {

  public static
      Pair<PopupWindow, ViewGroup>
      make(
          Context context,
          Relation relation,
          List<Either3<Label, Integer, Unit>> path,
          View v,
          RelationViewColors relationViewColors,
          CodeLabelAliasMap codeLabelAliases,
          Bijection<CanonicalRelation, String> relationAliases,
          List<Relation> relations,
          boolean ref,
          final F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Unit> select) {
    final Pair<PopupWindow, ViewGroup> p = UIUtils.makePopupWindow(context);
    p.x.showAsDropDown(v);
    if (ref) {
      p.y.addView(RelationRefView.make(context,
          nothing(), $ -> {
            p.x.dismiss();
            select.f(Either.y(nil()));
            return unit();
          }));
      Space s = new Space(context);
      s.setMinimumHeight(1);
      p.y.addView(s);
    }
    UIUtils.addEmptyRelationsToMenu(context, relationViewColors, p.y, r -> {
          p.x.dismiss();
          select.f(Either.x(r));
          return unit();
        }, relation, path, codeLabelAliases, relationAliases, relations);
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
          relationViewColors, new DragBro(), r, nil(),
          emptyRelationViewListener, codeLabelAliases, relationAliases,
          relations), new Overlay.Listener() {
        public boolean onLongClick() {
          return false;
        }

        public void onClick() {
          p.x.dismiss();
          select.f(Either.x(r));
        }
      }));
    }
    return p;
  }
}
