package com.pokemon.kore.ui;

import static com.pokemon.kore.codes.ValueUtils.eval;
import static com.pokemon.kore.codes.ValueUtils.toRelation;
import static com.pokemon.kore.ui.BundleUtils.deserializeBundle;
import static com.pokemon.kore.ui.BundleUtils.serializeBundle;
import static com.pokemon.kore.ui.RelationUtils.changeDomain;
import static com.pokemon.kore.ui.RelationUtils.domain;
import static com.pokemon.kore.ui.RelationUtils.unit_unit;
import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.CodeUtils.equal;
import static com.pokemon.kore.utils.CodeUtils.unit;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.map;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.OptionalUtils.some;
import static com.pokemon.kore.utils.PairUtils.pair;
import static com.pokemon.kore.utils.PairUtils.snd;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.CanonicalRelation;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.codes.Value;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either.Tag;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.ListUtils;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Ref;
import com.pokemon.kore.utils.Tree;
import com.pokemon.kore.utils.Unit;

public class RunArea {

  private static
      Pair<View, Pair<F<Optional<Relation>, Unit>, F<Unit, List<Tree<Unit, Optional<String>>>>>>
      make(List<Tree<Unit, Optional<String>>> ts, Context context,
          List<Code> codes, CodeLabelAliasMap codeLabelAliases,
          Bijection<CanonicalCode, String> codeAliases,
          Bijection<CanonicalRelation, String> relationAliases,
          List<Relation> relations,
          RelationViewColors relationViewColors) {
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setBackgroundColor(0xFFFFFFFF);
    Ref<List<Optional<Pair<F<Unit, Bundle>, F<Unit, List<Tree<Unit, Optional<String>>>>>>>> children =
        new Ref<>(nil());
    F<Optional<Either<Relation, Pair<Bundle, List<Tree<Unit, Optional<String>>>>>>, Unit> add2 =
        or -> {
          if (or.isNothing()) {
            Button b = new Button(context);
            b.setBackgroundColor(0xFF000000);
            ll.addView(b);
            children.set(ListUtils.append(nothing(), children.get()));
          } else {
            Pair<View, Pair<F<Optional<Relation>, Unit>, F<Unit, List<Tree<Unit, Optional<String>>>>>> cRA =
                make(
                    or.some().x.tag == Tag.X ? nil()
                        : or.some().x.y().y, context, codes,
                    codeLabelAliases, codeAliases, relationAliases,
                    relations, relationViewColors);
            F<Relation, Unit> done = r -> {
              if (!equal(domain(r), unit))
                return unit();
              Optional<Value> v = eval(r);
              cRA.y.x.f(v.isNothing() ? nothing()
                  : some(toRelation(v.some().x)));
              return unit();
            };
            Pair<View, F<Unit, Bundle>> rE;
            switch (or.some().x.tag) {
            case X:
              rE =
                  RelationEditor.make(context, or.some().x.x(), codes,
                      codeLabelAliases, codeAliases, relationAliases,
                      relations, relationViewColors, done);
              break;
            case Y:
              rE =
                  RelationEditor.make(context, codes, codeLabelAliases,
                      codeAliases, relationAliases, relations,
                      relationViewColors, or.some().x.y().x, done);
              break;
            default:
              throw boom();
            }
            ll.addView(rE.x);
            FrameLayout pad = new FrameLayout(context);
            pad.addView(cRA.x);
            pad.setPadding(10, 0, 0, 1);
            pad.setBackgroundColor(0xFF000000);
            ll.addView(pad);
            children.set(ListUtils.append(some(pair(rE.y, cRA.y.y)),
                children.get()));
          }
          return unit();
        };
    F<Optional<Relation>, Unit> add = or -> {
      add2.f(or.isNothing() ? nothing() : some(Either.x(or.some().x)));
      return unit();
    };
    for (Tree<Unit, Optional<String>> t : iter(ts))
      if (t.v.isNothing())
        add2.f(nothing());
      else
        add2.f(some(Either
            .y(pair(
                deserializeBundle(t.v.some().x),
                map(snd(), t.edges)))));
    F<Unit, List<Tree<Unit, Optional<String>>>> getState = $ -> {
          List<Tree<Unit, Optional<String>>> l = nil();
          for (Optional<Pair<F<Unit, Bundle>, F<Unit, List<Tree<Unit, Optional<String>>>>>> c : iter(children
              .get()))
            l =
                ListUtils
                    .append(
                        c.isNothing() ? new Tree<>(nothing(), nil())
                            : new Tree<>(
                                some(serializeBundle(c.some().x.x.f(unit()))),
                                map(t -> pair(unit(), t), c.some().x.y.f(unit()))), l);
          return l;
        };
    return pair((View) ll, pair(add, getState));
  }

  public static Pair<Pair<View, F<Unit, Unit>>, F<Unit, Bundle>> make(Bundle b,
      Context context, List<Code> codes,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      List<Relation> relations,
      RelationViewColors relationViewColors) {
    Pair<View, Pair<F<Optional<Relation>, Unit>, F<Unit, List<Tree<Unit, Optional<String>>>>>> p =
        make((List<Tree<Unit, Optional<String>>>) b.getSerializable("s"),
            context, codes, codeLabelAliases, codeAliases, relationAliases,
            relations, relationViewColors);
    return make(p, context, codes, codeLabelAliases, codeAliases,
        relationAliases, relations, relationViewColors);
  }

  public static Pair<Pair<View, F<Unit, Unit>>, F<Unit, Bundle>> make(
      Context context, List<Code> codes,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      List<Relation> relations,
      RelationViewColors relationViewColors) {
    Pair<View, Pair<F<Optional<Relation>, Unit>, F<Unit, List<Tree<Unit, Optional<String>>>>>> p =
        make(nil(), context, codes,
            codeLabelAliases, codeAliases, relationAliases, relations,
            relationViewColors);
    return make(p, context, codes, codeLabelAliases, codeAliases,
        relationAliases, relations, relationViewColors);
  }

  private static
      Pair<Pair<View, F<Unit, Unit>>, F<Unit, Bundle>>
      make(
          Pair<View, Pair<F<Optional<Relation>, Unit>, F<Unit, List<Tree<Unit, Optional<String>>>>>> p,
          Context context, List<Code> codes,
          CodeLabelAliasMap codeLabelAliases,
          Bijection<CanonicalCode, String> codeAliases,
          Bijection<CanonicalRelation, String> relationAliases,
          List<Relation> relations,
          RelationViewColors relationViewColors) {
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    FrameLayout menuAnchor = new FrameLayout(context);
    ll.addView(menuAnchor);
    ScrollView sv = new ScrollView(context);
    sv.addView(p.x);
    ll.addView(sv);
    F<Unit, Unit> choose = $ -> {
      RelationMenu
          .make(
              context,
              unit_unit,
              nil(),
              menuAnchor,
              relationViewColors,
              codeLabelAliases,
              relationAliases,
              relations,
              false,
              er -> {
                Relation r =
                    equal(unit, domain(er.x())) ? er.x() : changeDomain(
                        er.x(),
                        nil(),
                        unit);
                p.y.x.f(some(r));
                return unit();
              }
          );
      return unit();
    };
    F<Unit, Bundle> getState = $ -> {
      Bundle b = new Bundle();
      b.putSerializable("s", p.y.y.f(unit()));
      return b;
    };
    return pair(pair((View) ll, choose), getState);
  }
}
