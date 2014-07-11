package com.example.kore.ui;

import static com.example.kore.codes.ValueUtils.eval;
import static com.example.kore.codes.ValueUtils.toRelation;
import static com.example.kore.ui.BundleUtils.deserializeBundle;
import static com.example.kore.ui.BundleUtils.serializeBundle;
import static com.example.kore.ui.RelationUtils.changeDomain;
import static com.example.kore.ui.RelationUtils.domain;
import static com.example.kore.ui.RelationUtils.unit_unit;
import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.map;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.OptionalUtils.some;
import static com.example.kore.utils.PairUtils.pair;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Value;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either.Tag;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Optional;
import com.example.kore.utils.OptionalUtils;
import com.example.kore.utils.Pair;
import com.example.kore.utils.PairUtils;
import com.example.kore.utils.Ref;
import com.example.kore.utils.Tree;
import com.example.kore.utils.Unit;

public class RunArea {

  private static
      Pair<View, Pair<F<Optional<Relation>, Unit>, F<Unit, List<Tree<Unit, Optional<String>>>>>>
      make(List<Tree<Unit, Optional<String>>> ts, final Context context,
          final List<Code> codes, final CodeLabelAliasMap codeLabelAliases,
          final Bijection<CanonicalCode, String> codeAliases,
          final Bijection<CanonicalRelation, String> relationAliases,
          final List<Relation> relations,
          final RelationViewColors relationViewColors) {
    final LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setBackgroundColor(0xFFFFFFFF);
    final Ref<List<Optional<Pair<F<Unit, Bundle>, F<Unit, List<Tree<Unit, Optional<String>>>>>>>> children =
        new Ref<List<Optional<Pair<F<Unit, Bundle>, F<Unit, List<Tree<Unit, Optional<String>>>>>>>>(
            ListUtils
                .<Optional<Pair<F<Unit, Bundle>, F<Unit, List<Tree<Unit, Optional<String>>>>>>> nil());
    final F<Optional<Either<Relation, Pair<Bundle, List<Tree<Unit, Optional<String>>>>>>, Unit> add2 =
        new F<Optional<Either<Relation, Pair<Bundle, List<Tree<Unit, Optional<String>>>>>>, Unit>() {
          public
              Unit
              f(Optional<Either<Relation, Pair<Bundle, List<Tree<Unit, Optional<String>>>>>> or) {
            if (or.isNothing()) {
              Button b = new Button(context);
              b.setBackgroundColor(0xFF000000);
              ll.addView(b);
              children
                  .set(ListUtils.append(
                      OptionalUtils
                          .<Pair<F<Unit, Bundle>, F<Unit, List<Tree<Unit, Optional<String>>>>>> nothing(),
                      children.get()));
            } else {
              final Pair<View, Pair<F<Optional<Relation>, Unit>, F<Unit, List<Tree<Unit, Optional<String>>>>>> cRA =
                  make(
                      or.some().x.tag == Tag.X ? ListUtils.<Tree<Unit, Optional<String>>> nil()
                          : or.some().x.y().y, context, codes,
                      codeLabelAliases, codeAliases, relationAliases,
                      relations, relationViewColors);
              F<Relation, Unit> done = new F<Relation, Unit>() {
                public Unit f(Relation r) {
                  if (!equal(domain(r), unit))
                    return unit();
                  Optional<Value> v = eval(r);
                  cRA.y.x.f(v.isNothing() ? OptionalUtils.<Relation> nothing()
                      : some(toRelation(v.some().x)));
                  return unit();
                }
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
          }
        };
    F<Optional<Relation>, Unit> add = new F<Optional<Relation>, Unit>() {
      public Unit f(Optional<Relation> or) {
        add2.f(or.isNothing() ? OptionalUtils
            .<Either<Relation, Pair<Bundle, List<Tree<Unit, Optional<String>>>>>> nothing()
            : some(Either
                .<Relation, Pair<Bundle, List<Tree<Unit, Optional<String>>>>> x(or
                    .some().x)));
        return unit();
      }
    };
    for (Tree<Unit, Optional<String>> t : iter(ts))
      if (t.v.isNothing())
        add2.f(OptionalUtils
            .<Either<Relation, Pair<Bundle, List<Tree<Unit, Optional<String>>>>>> nothing());
      else
        add2.f(some(Either
            .<Relation, Pair<Bundle, List<Tree<Unit, Optional<String>>>>> y(pair(
                deserializeBundle(t.v.some().x),
                map(PairUtils.<Unit, Tree<Unit, Optional<String>>> snd(),
                    t.edges)))));
    F<Unit, List<Tree<Unit, Optional<String>>>> getState =
        new F<Unit, List<Tree<Unit, Optional<String>>>>() {
          public List<Tree<Unit, Optional<String>>> f(Unit _) {
            List<Tree<Unit, Optional<String>>> l = nil();
            for (Optional<Pair<F<Unit, Bundle>, F<Unit, List<Tree<Unit, Optional<String>>>>>> c : iter(children
                .get()))
              l =
                  ListUtils
                      .append(
                          c.isNothing() ? new Tree<Unit, Optional<String>>(
                              OptionalUtils.<String> nothing(),
                              ListUtils
                                  .<Pair<Unit, Tree<Unit, Optional<String>>>> nil())
                              : new Tree<Unit, Optional<String>>(
                                  some(serializeBundle(c.some().x.x.f(unit()))),
                                  map(new F<Tree<Unit, Optional<String>>, Pair<Unit, Tree<Unit, Optional<String>>>>() {
                                    public
                                        Pair<Unit, Tree<Unit, Optional<String>>>
                                        f(Tree<Unit, Optional<String>> t) {
                                      return pair(unit(), t);
                                    }
                                  }, c.some().x.y.f(unit()))), l);

            return l;
          }
        };
    return pair((View) ll, pair(add, getState));
  }

  public static Pair<Pair<View, F<Unit, Unit>>, F<Unit, Bundle>> make(Bundle b,
      final Context context, final List<Code> codes,
      final CodeLabelAliasMap codeLabelAliases,
      final Bijection<CanonicalCode, String> codeAliases,
      final Bijection<CanonicalRelation, String> relationAliases,
      final List<Relation> relations,
      final RelationViewColors relationViewColors) {
    Pair<View, Pair<F<Optional<Relation>, Unit>, F<Unit, List<Tree<Unit, Optional<String>>>>>> p =
        make((List<Tree<Unit, Optional<String>>>) b.getSerializable("s"),
            context, codes, codeLabelAliases, codeAliases, relationAliases,
            relations, relationViewColors);
    return make(p, context, codes, codeLabelAliases, codeAliases,
        relationAliases, relations, relationViewColors);
  }

  public static Pair<Pair<View, F<Unit, Unit>>, F<Unit, Bundle>> make(
      final Context context, final List<Code> codes,
      final CodeLabelAliasMap codeLabelAliases,
      final Bijection<CanonicalCode, String> codeAliases,
      final Bijection<CanonicalRelation, String> relationAliases,
      final List<Relation> relations,
      final RelationViewColors relationViewColors) {
    Pair<View, Pair<F<Optional<Relation>, Unit>, F<Unit, List<Tree<Unit, Optional<String>>>>>> p =
        make(ListUtils.<Tree<Unit, Optional<String>>> nil(), context, codes,
            codeLabelAliases, codeAliases, relationAliases, relations,
            relationViewColors);
    return make(p, context, codes, codeLabelAliases, codeAliases,
        relationAliases, relations, relationViewColors);
  }

  private static
      Pair<Pair<View, F<Unit, Unit>>, F<Unit, Bundle>>
      make(
          final Pair<View, Pair<F<Optional<Relation>, Unit>, F<Unit, List<Tree<Unit, Optional<String>>>>>> p,
          final Context context, final List<Code> codes,
          final CodeLabelAliasMap codeLabelAliases,
          final Bijection<CanonicalCode, String> codeAliases,
          final Bijection<CanonicalRelation, String> relationAliases,
          final List<Relation> relations,
          final RelationViewColors relationViewColors) {
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    final FrameLayout menuAnchor = new FrameLayout(context);
    ll.addView(menuAnchor);
    final ScrollView sv = new ScrollView(context);
    sv.addView(p.x);
    ll.addView(sv);
    F<Unit, Unit> choose = new F<Unit, Unit>() {
      public Unit f(Unit _) {
        RelationMenu
            .make(
                context,
                unit_unit,
                ListUtils.<Either3<Label, Integer, Unit>> nil(),
                menuAnchor,
                relationViewColors,
                codeLabelAliases,
                relationAliases,
                relations,
                false,
                new F<Either<Relation, List<Either3<Label, Integer, Unit>>>, Unit>() {
                  public Unit f(
                      Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
                    final Relation r =
                        equal(unit, domain(er.x())) ? er.x() : changeDomain(
                            er.x(),
                            ListUtils.<Either3<Label, Integer, Unit>> nil(),
                            unit);
                    p.y.x.f(some(r));
                    return unit();
                  }
                });
        return unit();
      }
    };
    F<Unit, Bundle> getState = new F<Unit, Bundle>() {
      public Bundle f(Unit _) {
        Bundle b = new Bundle();
        b.putSerializable("s", p.y.y.f(unit()));
        return b;
      }
    };
    return pair(pair((View) ll, choose), getState);
  }
}
