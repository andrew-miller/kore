package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.codomain;
import static com.example.kore.ui.RelationUtils.defaultValue;
import static com.example.kore.ui.RelationUtils.getRelation;
import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.fromArray;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Label_;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class Label_View {
  interface Listener {
    void replace(Relation r);
  }

  public static View make(final Context context,
      F<Either3<Label, Integer, Unit>, View> make, Integer color,
      final Relation relation, final List<Either3<Label, Integer, Unit>> path,
      Integer aliasTextColor, final CodeLabelAliasMap codeLabelAliases,
      final RelationViewColors relationViewColors,
      final Bijection<CanonicalRelation, String> relationAliases,
      final Listener listener) {
    final Relation r = relationAt(path, relation).some().x;
    LinearLayout ll = new LinearLayout(context);
    ll.setBackgroundColor(color);

    final View v;
    Optional<String> ola =
        codeLabelAliases.getAliases(new CanonicalCode(r.label().o, ListUtils
            .<Label> nil())).xy.get(r.label().label);
    if (ola.isNothing())
      v = LabelView.make(context, r.label().label, aliasTextColor);
    else {
      Button b = new Button(context);
      b.setText(ola.some().x);
      v = b;
    }
    v.setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        Pair<PopupWindow, ViewGroup> p = UIUtils.makePopupWindow(context);
        p.x.showAsDropDown(v);
        UIUtils.addRelationLabelsToMenu(p.y, context, v, codeLabelAliases,
            new CanonicalCode(r.label().o, ListUtils.<Label> nil()),
            new F<Label, Void>() {
              public Void f(Label l) {
                Relation sr =
                    getRelation(relation, r,
                        Either3.<Label, Integer, Unit> z(unit())).some().x;
                Code o = reroot(r.label().o, fromArray(l));
                listener.replace(Relation.label(new Label_(l, x(equal(
                    codomain(sr), o) ? sr : defaultValue(unit, o)), r.label().o)));
                return null;
              }

              private Either<Relation, List<Either3<Label, Integer, Unit>>> x(
                  Relation relation) {
                return Either.x(relation);
              }
            });
        UIUtils.addEmptyRelationsToMenu(context, relationViewColors, p.y,
            new F<Relation, Unit>() {
              public Unit f(Relation r) {
                listener.replace(r);
                return unit();
              }
            }, relation, path, codeLabelAliases, relationAliases);
      }
    });

    ll.addView(v);
    ll.addView(make.f(Either3.<Label, Integer, Unit> z(unit())));
    return ll;
  }
}