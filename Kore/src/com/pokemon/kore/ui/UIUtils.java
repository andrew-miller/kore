package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.PatternUtils.emptyPattern;
import static com.pokemon.kore.ui.RelationUtils.codomain;
import static com.pokemon.kore.ui.RelationUtils.domain;
import static com.pokemon.kore.ui.RelationUtils.emptyRelationViewListener;
import static com.pokemon.kore.ui.RelationUtils.enclosingAbstraction;
import static com.pokemon.kore.ui.RelationUtils.relationOrPathAt;
import static com.pokemon.kore.ui.RelationUtils.renderRelation;
import static com.pokemon.kore.ui.RelationUtils.resolve;
import static com.pokemon.kore.utils.CodeUtils.equal;
import static com.pokemon.kore.utils.CodeUtils.hash;
import static com.pokemon.kore.utils.CodeUtils.renderCode;
import static com.pokemon.kore.utils.CodeUtils.renderCode3;
import static com.pokemon.kore.utils.CodeUtils.reroot;
import static com.pokemon.kore.utils.CodeUtils.unit;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.cons;
import static com.pokemon.kore.utils.ListUtils.fromArray;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.OptionalUtils.some;
import static com.pokemon.kore.utils.PairUtils.pair;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.CanonicalRelation;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Code2;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.codes.Relation.Abstraction;
import com.pokemon.kore.codes.Relation.Projection;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.ICode;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public class UIUtils {

  public static void replaceWithTextEntry(ViewGroup vg, View v, Context a,
      String hint, F<String, Void> onDone) {
    vg.removeView(v);
    EditText t = new EditText(a);
    t.requestFocus();
    t.setImeOptions(EditorInfo.IME_ACTION_DONE);
    t.setInputType(EditorInfo.TYPE_CLASS_TEXT);
    t.setOnEditorActionListener(new OnEditorActionListener() {
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          onDone.f(t.getText().toString());
          return true;
        }
        return false;
      }
    });
    t.setOnFocusChangeListener(($, hasFocus) -> {
      if (!hasFocus) {
        ((InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(t.getWindowToken(), 0);
        vg.removeAllViews();
        vg.addView(v);
      }
    });
    t.setHint(hint);
    vg.addView(t);
    ((InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE))
        .showSoftInput(t, 0);
  }

  public static void addCodeToMenu3(Menu m, ICode root, List<Label> path,
      CodeLabelAliasMap2 codeLabelAliases,
      Bijection<Code2.Link, String> codeAliases, F<List<Label>, Unit> f) {
    addCodeToMenu3(m, root, path, Either.x(root), "", "", codeLabelAliases,
        codeAliases, f);
  }

  private static void addCodeToMenu3(Menu m, ICode root, List<Label> path,
      Either<ICode, List<Label>> cp, String ls, String space,
      CodeLabelAliasMap2 codeLabelAliases,
      Bijection<Code2.Link, String> codeAliases, F<List<Label>, Unit> f) {
    MenuItem i =
        m.add(space + ls.substring(0, Math.min(10, ls.length())) + " "
            + renderCode3(root, path, codeLabelAliases, codeAliases, 1));
    i.setOnMenuItemClickListener($ -> {
      f.f(path);
      return true;
    });
    if (cp.tag == cp.tag.X) {
      Pair<Code2, List<Label>> p = cp.x().link();
      Bijection<Label, String> las =
          codeLabelAliases.getAliases(new Code2.Link(hash(p.x), p.y));
      for (Pair<Label, Either<ICode, List<Label>>> e : iter(cp.x().labels()
          .entrySet())) {
        Optional<String> ls2 = las.xy.get(e.x);
        addCodeToMenu3(m, root, append(e.x, path), e.y,
            ls2.isNothing() ? e.x.label : ls2.some().x, space + "  ",
            codeLabelAliases, codeAliases, f);
      }
    }
  }

  public static void addCodeToMenu2(Menu m, Code root, List<Label> path,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases, F<List<Label>, Unit> f) {
    addCodeToMenu2(m, root, path, Either.x(root), "", "", codeLabelAliases,
        codeAliases, f);
  }

  private static void addCodeToMenu2(Menu m, Code2 root, List<Label> path,
      Either<Code2, List<Label>> cp, String ls, String space,
      CodeLabelAliasMap2 codeLabelAliases,
      Bijection<Code2.Link, String> codeAliases, F<List<Label>, Unit> f) {
    MenuItem i =
        m.add(space + ls.substring(0, Math.min(10, ls.length())) + " "
            + renderCode(root, path, codeLabelAliases, codeAliases, 1));
    i.setOnMenuItemClickListener($ -> {
      f.f(path);
      return true;
    });
    Bijection<Label, String> las =
        codeLabelAliases.getAliases(new CanonicalCode(root, path));
    if (cp.tag == cp.tag.X)
      for (Pair<Label, Either<Code, List<Label>>> e : iter(cp.x().labels
          .entrySet())) {
        Optional<String> ls2 = las.xy.get(e.x);
        addCodeToMenu2(m, root, append(e.x, path), e.y,
            ls2.isNothing() ? e.x.label : ls2.some().x, space + "  ",
            codeLabelAliases, codeAliases, f);
      }
  }

  public static void addCodeToMenu(Menu m, Code root, List<Label> path,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases, F<List<Label>, Unit> f) {
    addCodeToMenu(m, root, path, Either.x(root), "", "", codeLabelAliases,
        codeAliases, f);
  }

  private static void addCodeToMenu(Menu m, Code root, List<Label> path,
      Either<Code, List<Label>> cp, String ls, String space,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases, F<List<Label>, Unit> f) {
    MenuItem i =
        m.add(space + ls.substring(0, Math.min(10, ls.length())) + " "
            + renderCode(root, path, codeLabelAliases, codeAliases, 1));
    i.setOnMenuItemClickListener($ -> {
      f.f(path);
      return true;
    });
    Bijection<Label, String> las =
        codeLabelAliases.getAliases(new CanonicalCode(root, path));
    if (cp.tag == cp.tag.X)
      for (Pair<Label, Either<Code, List<Label>>> e : iter(cp.x().labels
          .entrySet())) {
        Optional<String> ls2 = las.xy.get(e.x);
        addCodeToMenu(m, root, append(e.x, path), e.y,
            ls2.isNothing() ? e.x.label : ls2.some().x, space + "  ",
            codeLabelAliases, codeAliases, f);
      }
  }

  public static void addEmptyRelationsToMenu(Context context,
      RelationViewColors rvc, ViewGroup vg, F<Relation, Unit> f, Relation root,
      List<Either3<Label, Integer, Unit>> path,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      List<Relation> relations) {
    F<Unit, Unit> addSpace = x -> {
      Space s = new Space(context);
      s.setMinimumHeight(1);
      vg.addView(s);
      return unit();
    };
    F<Relation, Unit> add =
        r -> {
          List<Either3<Label, Integer, Unit>> n = nil();
          vg.addView(Overlay.make(context, RelationView.make(context, rvc,
              new DragBro(), r, n, emptyRelationViewListener, codeLabelAliases,
              relationAliases, relations), new Overlay.Listener() {
            public boolean onLongClick() {
              return false;
            }

            public void onClick() {
              f.f(r);
            }
          }));
          return unit();
        };
    Relation r = resolve(root, relationOrPathAt(path, root));
    Code d = domain(r);
    Code c = codomain(r);
    add.f(RelationUtils.emptyRelation(d, c, Relation.Tag.UNION));
    addSpace.f(unit());
    if (equal(domain(r), unit)) {
      if (codomain(r).tag == Code.Tag.PRODUCT) {
        add.f(RelationUtils.emptyRelation(d, c, Relation.Tag.PRODUCT));
        addSpace.f(unit());
      }
      if (codomain(r).tag == Code.Tag.UNION)
        if (!codomain(r).labels.entrySet().isEmpty()) {
          add.f(RelationUtils.emptyRelation(d, c, Relation.Tag.LABEL));
          addSpace.f(unit());
        }
    }
    add.f(RelationUtils.emptyRelation(d, c, Relation.Tag.ABSTRACTION));
    addSpace.f(unit());
    add.f(RelationUtils.emptyRelation(d, c, Relation.Tag.COMPOSITION));
    Optional<Abstraction> oea = enclosingAbstraction(path, root);
    if (!oea.isNothing()) {
      addSpace.f(unit());
      Relation p =
          RelationUtils.emptyRelation(oea.some().x.i, c,
              Relation.Tag.PROJECTION);
      Relation a =
          Relation.abstraction(new Abstraction(emptyPattern, Either.x(p), oea
              .some().x.i, c));
      vg.addView(Overlay.make(context, RelationView.make(context, rvc,
          new DragBro(), a, fromArray(Either3.z(unit())),
          emptyRelationViewListener, codeLabelAliases, relationAliases,
          relations), new Overlay.Listener() {
        public boolean onLongClick() {
          return false;
        }

        public void onClick() {
          f.f(p);
        }
      }));
    }
  }

  public static List<View> relationLabels(ViewGroup vg, Context context,
      View v, CodeLabelAliasMap codeLabelAliases, CanonicalCode cc,
      F<Label, Unit> f) {
    List<View> l = nil();
    for (Pair<Label, ?> e : iter(cc.code.labels.entrySet())) {
      Optional<String> a = codeLabelAliases.getAliases(cc).xy.get(e.x);
      Button b = new Button(context);
      b.setText(a.isNothing() ? e.x.toString() : a.some().x);
      b.setOnClickListener($ -> f.f(e.x));
      l = cons(b, l);
    }
    return l;
  }

  /** <tt>c</tt> and <tt>out</tt> are root codes */
  public static void addProjectionsToMenu(Pair<PopupWindow, ViewGroup> p,
      Context context, View v, CodeLabelAliasMap codeLabelAliases, Code c,
      Code out, F<List<Label>, Unit> select) {
    addProjectionsToMenu(p, context, v, codeLabelAliases, nil(), c, out, select);
  }

  private static void addProjectionsToMenu(Pair<PopupWindow, ViewGroup> p,
      Context context, View v, CodeLabelAliasMap codeLabelAliases,
      List<Label> proj, Code c, Code out, F<List<Label>, Unit> select) {
    Code o = reroot(c, proj);
    Button b = new Button(context);
    int i = 0;
    p.y.addView(b, i++);
    b.setText(renderRelation(some(c),
        Either.x(Relation.projection(new Projection(proj, o))),
        codeLabelAliases));
    b.setOnClickListener($ -> {
      p.x.dismiss();
      select.f(proj);
    });
    b.setEnabled(equal(o, out));
    for (Pair<Label, ?> e : iter(o.labels.entrySet())) {
      Optional<String> a =
          codeLabelAliases.getAliases(new CanonicalCode(c, proj)).xy.get(e.x);
      Button b2 = new Button(context);
      b2.setText(a.isNothing() ? e.x.toString() : a.some().x);
      b2.setOnClickListener($ -> {
        p.x.dismiss();
        Pair<PopupWindow, ViewGroup> p1 = makePopupWindow(context);
        p1.x.showAsDropDown(v);
        addProjectionsToMenu(p1, context, v, codeLabelAliases,
            append(e.x, proj), c, out, select);
      });
      p.y.addView(b2, i++);
    }
  }

  public static Pair<PopupWindow, ViewGroup> makePopupWindow(Context context) {
    LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT));
    ll.setBackgroundColor(0xffffffff);
    ll.setPadding(1, 1, 1, 1);
    ScrollView sv = new ScrollView(context);
    sv.addView(ll);
    PopupWindow pw = new PopupWindow(sv);
    pw.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
    pw.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
    {
      // HACK
      // http://stackoverflow.com/questions/3121232/android-popup-window-dismissal
      pw.setBackgroundDrawable(new BitmapDrawable());
      pw.setOutsideTouchable(true);
    }
    return pair(pw, ll);
  }
}