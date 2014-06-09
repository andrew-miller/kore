package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.codomain;
import static com.example.kore.ui.RelationUtils.domain;
import static com.example.kore.ui.RelationUtils.inAbstraction;
import static com.example.kore.ui.RelationUtils.relationOrPathAt;
import static com.example.kore.ui.RelationUtils.renderRelation;
import static com.example.kore.ui.RelationUtils.resolve;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.renderCode;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.OptionalUtils.some;
import static com.example.kore.utils.PairUtils.pair;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Projection;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class UIUtils {

  public static void replaceWithTextEntry(final ViewGroup vg, final View v,
      final Context a, String hint, final F<String, Void> onDone) {
    vg.removeView(v);
    final EditText t = new EditText(a);
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
    t.setOnFocusChangeListener(new OnFocusChangeListener() {
      public void onFocusChange(View _, boolean hasFocus) {
        if (!hasFocus) {
          ((InputMethodManager) a
              .getSystemService(Context.INPUT_METHOD_SERVICE))
              .hideSoftInputFromWindow(t.getWindowToken(), 0);
          vg.removeAllViews();
          vg.addView(v);
        }
      }
    });
    t.setHint(hint);
    vg.addView(t);
    ((InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE))
        .showSoftInput(t, 0);
  }

  public static void
      addCodeToMenu(Menu m, final Code root, final List<Label> path,
          CodeLabelAliasMap codeLabelAliases,
          Bijection<CanonicalCode, String> codeAliases,
          final F<List<Label>, Unit> f) {
    addCodeToMenu(m, root, path, Either.<Code, List<Label>> x(root), "", "",
        codeLabelAliases, codeAliases, f);
  }

  private static void
      addCodeToMenu(Menu m, final Code root, final List<Label> path,
          Either<Code, List<Label>> cp, String ls, String space,
          CodeLabelAliasMap codeLabelAliases,
          Bijection<CanonicalCode, String> codeAliases,
          final F<List<Label>, Unit> f) {
    MenuItem i =
        m.add(space + ls.substring(0, Math.min(10, ls.length())) + " "
            + renderCode(root, path, codeLabelAliases, codeAliases, 1));
    i.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      public boolean onMenuItemClick(MenuItem _) {
        f.f(path);
        return true;
      }
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

  public static void addRelationTypesToMenu(final Context context,
      final RelationViewColors rvc, final ViewGroup vg,
      final F<Relation.Tag, Unit> f, Relation root,
      List<Either3<Label, Integer, Unit>> path) {
    F<Relation.Tag, Unit> add = new F<Relation.Tag, Unit>() {
      public Unit f(final Relation.Tag t) {
        Button b = new Button(context);
        b.setBackgroundColor(rvc.relationcolors.m.get(t).some().x.x);
        vg.addView(Overlay.make(context, b, new Overlay.Listener() {
          public boolean onLongClick() {
            return false;
          }

          public void onClick() {
            f.f(t);
          }
        }));
        return unit();
      }
    };
    Relation r = resolve(root, relationOrPathAt(path, root));
    add.f(Relation.Tag.UNION);
    if (equal(domain(r), unit)) {
      if (codomain(r).tag == Code.Tag.PRODUCT)
        add.f(Relation.Tag.PRODUCT);
      if (codomain(r).tag == Code.Tag.UNION)
        if (!codomain(r).labels.entrySet().isEmpty())
          add.f(Relation.Tag.LABEL);
    }
    add.f(Relation.Tag.ABSTRACTION);
    add.f(Relation.Tag.COMPOSITION);
    if (inAbstraction(root, path))
      add.f(Relation.Tag.PROJECTION);
  }

  public static void addRelationLabelsToMenu(ViewGroup vg, Context context,
      View v, CodeLabelAliasMap codeLabelAliases, CanonicalCode cc,
      final F<Label, Void> f) {
    for (final Pair<Label, ?> e : iter(cc.code.labels.entrySet())) {
      Optional<String> a = codeLabelAliases.getAliases(cc).xy.get(e.x);
      Button b = new Button(context);
      b.setText(a.isNothing() ? e.x.toString() : a.some().x);
      b.setOnClickListener(new OnClickListener() {
        public void onClick(View _) {
          f.f(e.x);
        }
      });
      vg.addView(b);
    }
  }

  /** <tt>c</tt> and <tt>out</tt> are root codes */
  public static void addProjectionsToMenu(Pair<PopupWindow, ViewGroup> p,
      Context context, View v, CodeLabelAliasMap codeLabelAliases, Code c,
      Code out, F<List<Label>, Void> select) {
    addProjectionsToMenu(p, context, v, codeLabelAliases,
        ListUtils.<Label> nil(), c, out, select);
  }

  private static void addProjectionsToMenu(
      final Pair<PopupWindow, ViewGroup> p, final Context context,
      final View v, final CodeLabelAliasMap codeLabelAliases,
      final List<Label> proj, final Code c, final Code out,
      final F<List<Label>, Void> select) {
    Code o = reroot(c, proj);
    Button b = new Button(context);
    p.y.addView(b);
    b.setText(renderRelation(some(c), Either
        .<Relation, List<Either3<Label, Integer, Unit>>> x(Relation
            .projection(new Projection(proj, o))), codeLabelAliases));
    b.setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        Log.e("WAT", "WAT");
        select.f(proj);
      }
    });
    b.setEnabled(equal(o, out));
    for (final Pair<Label, ?> e : iter(o.labels.entrySet())) {
      Optional<String> a =
          codeLabelAliases.getAliases(new CanonicalCode(c, proj)).xy.get(e.x);
      Button b2 = new Button(context);
      b2.setText(a.isNothing() ? e.x.toString() : a.some().x);
      b2.setOnClickListener(new OnClickListener() {
        public void onClick(View _) {
          p.x.dismiss();
          Pair<PopupWindow, ViewGroup> p = makePopupWindow(context);
          p.x.showAsDropDown(v);
          addProjectionsToMenu(p, context, v, codeLabelAliases,
              append(e.x, proj), c, out, select);
        }
      });
      p.y.addView(b2);
    }
  }

  public static Pair<PopupWindow, ViewGroup> makePopupWindow(Context context) {
    final LinearLayout ll = new LinearLayout(context);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT));
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
    ViewGroup vgll = ll;
    return pair(pw, vgll);
  }
}