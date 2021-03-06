package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.linkTree;
import static com.pokemon.kore.ui.RelationUtils.linkTreeToRelation;
import static com.pokemon.kore.ui.RelationUtils.replaceRelationOrPathAt;
import static com.pokemon.kore.ui.RelationUtils.resolve;
import static com.pokemon.kore.ui.RelationUtils.subRelationOrPath;
import static com.pokemon.kore.utils.CodeUtils.unit;
import static com.pokemon.kore.utils.LinkTreeUtils.rebase;
import static com.pokemon.kore.utils.LinkTreeUtils.validLinkTree;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.pokemon.kore.R;
import com.pokemon.kore.codes.CanonicalRelation;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Unit;

public class RelationPath {
  interface Listener {
    void selectPath(List<Either3<Label, Integer, Unit>> p);

    void replaceRelation(
        Either<Relation, List<Either3<Label, Integer, Unit>>> er);
  }

  public static View make(Context context, RelationColors rc,
      Listener listener, Relation root, List<Relation> relations,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      List<Either3<Label, Integer, Unit>> path, int referenceColor,
      RelationViewColors rvc) {
    View v = LayoutInflater.from(context).inflate(R.layout.path, null);
    ViewGroup vg = (ViewGroup) v.findViewById(R.id.layout_path);
    make(context, rc, listener, root, relations, codeLabelAliases,
        relationAliases, nil(), path, Either.x(root), vg, referenceColor, rvc);
    return v;
  }

  private static void make(Context context, RelationColors rc,
      Listener listener, Relation root, List<Relation> relations,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      List<Either3<Label, Integer, Unit>> before,
      List<Either3<Label, Integer, Unit>> after,
      Either<Relation, List<Either3<Label, Integer, Unit>>> rp, ViewGroup vg,
      int referenceColor, RelationViewColors rvc) {
    Relation r = resolve(root, rp);
    Button b = new Button(context);
    b.setBackgroundColor(rp.tag == rp.tag.Y ? referenceColor : rc.m.get(
        rp.x().tag).some().x.x);
    b.setWidth(0);
    b.setHeight(LayoutParams.MATCH_PARENT);
    b.setOnClickListener(v -> {
      if (after.isEmpty()) {
        if (validLinkTree(linkTree(replaceRelationOrPathAt(root, before,
            Either.x(RelationUtils.dummy(unit, unit)))))) {
          RelationMenu.make(context, root, before, v, rvc, codeLabelAliases,
              relationAliases, relations, !before.isEmpty(), er -> {
                listener.replaceRelation(er.tag == er.tag.X ? Either
                    .x(linkTreeToRelation(rebase(before, linkTree(er.x()))))
                    : er);
                return unit();
              });
        }
      } else
        listener.selectPath(before);
    });
    b.setOnTouchListener(($, e) -> {
      if (e.getAction() == MotionEvent.ACTION_CANCEL)
        b.startDrag(null, new DragShadowBuilder(), new SelectRelation(), 0);
      return false;
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