package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.canReplace;
import static com.pokemon.kore.ui.RelationUtils.linkTree;
import static com.pokemon.kore.ui.RelationUtils.linkTreeToRelation2;
import static com.pokemon.kore.ui.RelationUtils.subRelationOrPath;
import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.CodeUtils.hashLink;
import static com.pokemon.kore.utils.CodeUtils.unit2;
import static com.pokemon.kore.utils.LinkTreeUtils.rebase;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.OptionalUtils.some;
import static com.pokemon.kore.utils.PairUtils.pair;
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
import com.pokemon.kore.codes.Code2;
import com.pokemon.kore.codes.IRelation;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation2;
import com.pokemon.kore.ui.RelationUtils.Resolver;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Unit;

public class RelationPath2 {
  interface Listener {
    void selectPath(List<Either3<Label, Integer, Unit>> p);

    void replaceRelation(
        Either<Relation2, List<Either3<Label, Integer, Unit>>> er);
  }

  public static View make(Context context, RelationColors2 rc,
      Listener listener, IRelation ir, List<Relation2> relations,
      CodeLabelAliasMap2 codeLabelAliases,
      Bijection<Relation2.Link, String> relationAliases,
      List<Either3<Label, Integer, Unit>> path, int referenceColor,
      RelationViewColors2 rvc, Resolver r) {
    View v = LayoutInflater.from(context).inflate(R.layout.path, null);
    ViewGroup vg = (ViewGroup) v.findViewById(R.id.layout_path);
    make(context, rc, listener, ir.link.x, relations, codeLabelAliases,
        relationAliases, nil(), path, some(ir), vg, referenceColor, rvc, r);
    return v;
  }

  private static void make(Context context, RelationColors2 rc,
      Listener listener, Relation2 root, List<Relation2> relations,
      CodeLabelAliasMap2 codeLabelAliases,
      Bijection<Relation2.Link, String> relationAliases,
      List<Either3<Label, Integer, Unit>> before,
      List<Either3<Label, Integer, Unit>> after, Optional<IRelation> ir,
      ViewGroup vg, int referenceColor, RelationViewColors2 rvc, Resolver r) {
    Button b = new Button(context);
    b.setBackgroundColor(ir.isNothing() ? referenceColor : rc.m.get(
        ir.some().x.ir.tag).some().x.x);
    b.setWidth(0);
    b.setHeight(LayoutParams.MATCH_PARENT);
    b.setOnClickListener(v -> {
      if (after.isEmpty()) {
        Code2.Link ul = hashLink(pair(unit2, nil()));
        if (canReplace(root, before, Either3.x(RelationUtils.dummy2(ul, ul)), r)) {
          RelationMenu2.make(context, ir.some().x, v, rvc, codeLabelAliases,
              relationAliases, relations, !before.isEmpty(), er -> {
                listener.replaceRelation(er.tag == er.tag.X ? Either
                    .x(linkTreeToRelation2(rebase(before, linkTree(er.x()))))
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
    if (!ir.isNothing() & !after.isEmpty()) {
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
      Optional<IRelation> ir2;
      if (ir.isNothing())
        ir2 = ir;
      else {
        Either<IRelation, List<Either3<Label, Integer, Unit>>> rp =
            subRelationOrPath(ir.some().x, e).some().x;
        switch (rp.tag) {
        case X:
          ir2 = some(rp.x());
        case Y:
          ir2 = nothing();
        default:
          throw boom();
        }
      }
      make(context, rc, listener, root, relations, codeLabelAliases,
          relationAliases, append(e, before), after.cons().tail, ir2, vg,
          referenceColor, rvc, r);
    }
  }
}