package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.PatternUtils.emptyPattern;
import static com.pokemon.kore.ui.RelationUtils.emptyProduct;
import static com.pokemon.kore.ui.RelationUtils.emptyRelationViewListener;
import static com.pokemon.kore.ui.RelationUtils.unit_unit;
import static com.pokemon.kore.utils.CodeUtils.makeStaticCLAM;
import static com.pokemon.kore.utils.CodeUtils.unit;
import static com.pokemon.kore.utils.ListUtils.fromArray;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.PairUtils.pair;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;
import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Pattern;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.codes.Relation.Abstraction;
import com.pokemon.kore.codes.Relation.Composition;
import com.pokemon.kore.codes.Relation.Label_;
import com.pokemon.kore.codes.Relation.Projection;
import com.pokemon.kore.codes.Relation.Tag;
import com.pokemon.kore.codes.Relation.Union;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Random;
import com.pokemon.kore.utils.Ref;
import com.pokemon.kore.utils.SARef;
import com.pokemon.kore.utils.Unit;

public class Colors {

  public static View make(Context context, RelationViewColors rvc,
      F<RelationViewColors, Unit> done) {
    Ref<RelationViewColors> rRVC = new Ref<>(rvc);
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    FrameLayout cpf = new FrameLayout(context);
    ll.addView(cpf);
    LinearLayout constructs = new LinearLayout(context);
    constructs.setOrientation(LinearLayout.VERTICAL);
    ScrollView sv = new ScrollView(context);
    sv.addView(constructs);
    ll.addView(sv);
    F<Integer, ColorPicker> initCP = color -> {
      cpf.removeAllViews();
      Button doneButton = new Button(context);
      doneButton.setText("done");
      doneButton.setOnClickListener($ -> done.f(rRVC.get()));
      LinearLayout cpll = new LinearLayout(context);
      cpll.setOrientation(LinearLayout.VERTICAL);
      cpf.addView(doneButton);
      cpf.addView(cpll);
      ColorPicker cp = new ColorPicker(context);
      cpll.addView(cp);
      cp.setColor(color);
      cp.setOldCenterColor(color);
      SVBar svb = new SVBar(context);
      SaturationBar sb = new SaturationBar(context);
      ValueBar vb = new ValueBar(context);
      cp.addSVBar(svb);
      cp.addSaturationBar(sb);
      cp.addValueBar(vb);
      cpll.addView(svb);
      cpll.addView(sb);
      cpll.addView(vb);
      return cp;
    };
    SARef<F<Unit, Unit>> addRelations = new SARef<>();
    F<Pair<Tag, Relation>, View> makeRV =
        p -> Overlay.make(context, RelationView.make(context, rRVC.get(),
            new DragBro(), p.y,
            p.x == Tag.PROJECTION ? fromArray(Either3.z(unit())) : nil(),
            emptyRelationViewListener, makeStaticCLAM(Map.empty()),
            Bijection.empty(), nil()), new Overlay.Listener() {
          public boolean onLongClick() {
            return false;
          }

          public void onClick() {
            initCP
                .f(rRVC.get().relationcolors.m.get(p.x).some().x.x)
                .setOnColorChangedListener(
                    c -> {
                      RelationViewColors o = rRVC.get();
                      float[] hsv = new float[3];
                      c = c | 0xff000000;
                      Color.colorToHSV(c, hsv);
                      hsv[1] = (hsv[1] + 0.5f) % 1;
                      rRVC.set(new RelationViewColors(new RelationColors(
                          o.relationcolors.m.put(p.x,
                              pair(c, Color.HSVToColor(hsv)))),
                          o.aliasTextColor, o.labelTextColor, o.referenceColors));
                      addRelations.get().f(unit());
                    });
          }
        });
    Label t = new Label(Random.randomId());
    Label f = new Label(Random.randomId());
    Code bool =
        Code.newUnion(Map.<Label, Either<Code, List<Label>>> empty()
            .put(t, Either.x(unit)).put(f, Either.x(unit)));
    CodeLabelAliasMap clam =
        makeStaticCLAM(Map.<CanonicalCode, Bijection<Label, String>> empty()
            .put(
                new CanonicalCode(bool, nil()),
                Bijection.<Label, String> empty().putX(t, "true").some().x
                    .putX(f, "false").some().x));
    Relation negate =
        Relation.union(new Union(fromArray(Either.x(Relation
            .abstraction(new Abstraction(new Pattern(Map
                .<Label, Pattern> empty().put(t, emptyPattern)),
                Either.x(Relation.label(new Label_(f, Either.x(emptyProduct),
                    bool))), bool, bool))), Either.x(Relation
            .abstraction(new Abstraction(new Pattern(Map
                .<Label, Pattern> empty().put(f, emptyPattern)),
                Either.x(Relation.label(new Label_(t, Either.x(emptyProduct),
                    bool))), bool, bool)))), bool, bool));
    addRelations.set($ -> {
      constructs.removeAllViews();
      constructs.addView(f(context, makeRV.f(pair(Tag.ABSTRACTION, unit_unit)),
          "abstraction"));
      constructs.addView(f(context, makeRV.f(pair(Tag.PRODUCT, emptyProduct)),
          "product"));
      constructs.addView(f(
          context,
          makeRV.f(pair(Tag.COMPOSITION,
              Relation.composition(new Composition(nil(), unit, unit)))),
          "composition"));
      constructs.addView(f(context, makeRV.f(pair(Tag.PROJECTION, Relation
          .abstraction(new Abstraction(emptyPattern, Either.x(Relation
              .projection(new Projection(nil(), unit))), unit, unit)))),
          "projection"));
      constructs.addView(f(context, makeRV.f(pair(Tag.UNION,
          Relation.union(new Union(nil(), unit, unit)))), "union"));
      constructs.addView(f(
          context,
          makeRV.f(pair(Tag.LABEL,
              Relation.label(new Label_(t, Either.x(emptyProduct), unit)))),
          "label"));
      Button alias = new Button(context);
      alias.setOnClickListener($$ -> {
        initCP.f(rRVC.get().aliasTextColor).setOnColorChangedListener(
            c -> {
              RelationViewColors o = rRVC.get();
              rRVC.set(new RelationViewColors(o.relationcolors, c,
                  o.labelTextColor, o.referenceColors));
              addRelations.get().f(unit());
            });
      });
      alias.setText("alias");
      constructs.addView(alias);
      Button label = new Button(context);
      label.setOnClickListener($$ -> {
        initCP.f(rRVC.get().aliasTextColor).setOnColorChangedListener(
            c -> {
              RelationViewColors o = rRVC.get();
              rRVC.set(new RelationViewColors(o.relationcolors,
                  o.aliasTextColor, c, o.referenceColors));
              addRelations.get().f(unit());
            });
      });
      label.setText("label");
      constructs.addView(label);
      constructs.addView(f(context, Overlay.make(context, RelationView.make(
          context, rRVC.get(), new DragBro(), negate, nil(),
          emptyRelationViewListener, clam, Bijection.empty(), nil()),
          new Overlay.Listener() {
            public boolean onLongClick() {
              return false;
            }

            public void onClick() {
            }
          }), "sample"));
      return unit();
    });
    addRelations.get().f(unit());
    return ll;
  }

  private static LinearLayout f(Context context, View v, String l) {
    LinearLayout item = new LinearLayout(context);
    TextView label = new TextView(context);
    label.setText(l);
    item.addView(label);
    item.addView(v);
    return item;
  }

}
