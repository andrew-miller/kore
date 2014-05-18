package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.codomain;
import static com.example.kore.ui.RelationUtils.domain;
import static com.example.kore.ui.RelationUtils.edges;
import static com.example.kore.ui.RelationUtils.enclosingAbstraction;
import static com.example.kore.ui.RelationUtils.inAbstraction;
import static com.example.kore.ui.RelationUtils.relationAt;
import static com.example.kore.ui.RelationUtils.renderPathElement;
import static com.example.kore.ui.RelationUtils.renderRelation;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.renderCode;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.isSubList;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.OptionalUtils.some;
import static com.example.kore.utils.PairUtils.pair;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Abstraction;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Optional;
import com.example.kore.utils.OptionalUtils;
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

  /**
   * Any path that is prefixed by <code>invalidPath</code> will be disabled.
   */
  public static void addRelationToMenu(Menu m, final Relation root,
      final List<Either3<Label, Integer, Unit>> path,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      Optional<List<Either3<Label, Integer, Unit>>> invalidPath,
      final F<List<Either3<Label, Integer, Unit>>, Unit> f) {
    addRelationToMenu(m, root, path,
        Either.<Relation, List<Either3<Label, Integer, Unit>>> x(root), "", "",
        codeLabelAliases, relationAliases, invalidPath, f);
  }

  private static void addRelationToMenu(Menu m, final Relation root,
      final List<Either3<Label, Integer, Unit>> path,
      final Either<Relation, List<Either3<Label, Integer, Unit>>> rp,
      String ls, String space, CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      Optional<List<Either3<Label, Integer, Unit>>> invalidPath,
      final F<List<Either3<Label, Integer, Unit>>, Unit> f) {
    Optional<Abstraction> ea =
        rp.tag == rp.tag.Y ? OptionalUtils.<Abstraction> nothing()
            : enclosingAbstraction(path, root);
    Optional<String> ra =
        relationAliases.xy.get(new CanonicalRelation(root, path));
    m.add(
        space
            + ls.substring(0, Math.min(10, ls.length()))
            + " "
            + (ra.isNothing() ? renderRelation(
                ea.isNothing() ? OptionalUtils.<Code> nothing() : some(ea
                    .some().x.i), rp, codeLabelAliases) : ra.some().x))
        .setOnMenuItemClickListener(new OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem _) {
            f.f(path);
            return true;
          }
        })
        .setEnabled(
            rp.tag == rp.tag.X
                & (invalidPath.isNothing() || !isSubList(invalidPath.some().x,
                    path)));
    if (rp.tag == rp.tag.X)
      for (Pair<Either3<Label, Integer, Unit>, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(edges(rp
          .x())))
        addRelationToMenu(m, root, append(e.x, path), e.y,
            renderPathElement(e.x), space + "  ", codeLabelAliases,
            relationAliases, invalidPath, f);
  }

  public static void addRelationTypesToMenu(final Menu m,
      final F<Relation.Tag, Unit> f, Relation root,
      List<Either3<Label, Integer, Unit>> path) {
    F<Pair<String, Relation.Tag>, Unit> add =
        new F<Pair<String, Relation.Tag>, Unit>() {
          public Unit f(final Pair<String, Relation.Tag> p) {
            m.add(p.x).setOnMenuItemClickListener(
                new OnMenuItemClickListener() {
                  public boolean onMenuItemClick(MenuItem _) {
                    f.f(p.y);
                    return true;
                  }
                });
            return unit();
          }
        };
    Relation r = relationAt(path, root).some().x;
    add.f(pair("[]", Relation.Tag.UNION));
    if (equal(domain(r), unit)) {
      if (codomain(r).tag == Code.Tag.PRODUCT)
        add.f(pair("{}", Relation.Tag.PRODUCT));
      if (codomain(r).tag == Code.Tag.UNION)
        if (!codomain(r).labels.entrySet().isEmpty())
          add.f(pair("'", Relation.Tag.LABEL));
    }
    add.f(pair("->", Relation.Tag.ABSTRACTION));
    add.f(pair("|", Relation.Tag.COMPOSITION));
    if (inAbstraction(root, path))
      add.f(pair(".", Relation.Tag.PROJECTION));
  }
}
