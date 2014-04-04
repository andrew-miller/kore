package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.edges;
import static com.example.kore.ui.RelationUtils.enclosingAbstraction;
import static com.example.kore.ui.RelationUtils.renderPathElement;
import static com.example.kore.ui.RelationUtils.renderRelation;
import static com.example.kore.utils.CodeUtils.renderCode;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.OptionalUtils.some;
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
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Abstraction;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;
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

  public static void addCodeToMenu(Menu m, final Code root,
      final List<Label> path, CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, final F<List<Label>, Unit> f) {
    addCodeToMenu(m, root, path, CodeOrPath.newCode(root), "", "",
        codeLabelAliases, codeAliases, f);
  }

  private static void addCodeToMenu(Menu m, final Code root,
      final List<Label> path, CodeOrPath cp, String ls, String space,
      CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, final F<List<Label>, Unit> f) {
    MenuItem i =
        m.add(space + ls.substring(0, Math.min(10, ls.length())) + " "
            + renderCode(root, path, codeLabelAliases, codeAliases, 1));
    i.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      public boolean onMenuItemClick(MenuItem _) {
        f.f(path);
        return true;
      }
    });
    Map<Label, String> las =
        codeLabelAliases.getAliases(new CanonicalCode(root, path));
    if (cp.tag == CodeOrPath.Tag.CODE)
      for (Entry<Label, CodeOrPath> e : iter(cp.code.labels.entrySet())) {
        Optional<String> ls2 = las.get(e.k);
        addCodeToMenu(m, root, append(e.k, path), e.v,
            ls2.isNothing() ? e.k.label : ls2.some().x, space + "  ",
            codeLabelAliases, codeAliases, f);
      }
  }

  /**
   * filter out relations that don't have the domain/codomain <tt>d</tt>/
   * <tt>c</tt>
   */
  public static void addRelationToMenu(Menu m, final Relation root,
      final List<Either3<Label, Integer, Unit>> path,
      CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalRelation, String> relationAliases,
      final F<List<Either3<Label, Integer, Unit>>, Unit> f) {
    addRelationToMenu(m, root, path,
        Either.<Relation, List<Either3<Label, Integer, Unit>>> x(root), "", "",
        codeLabelAliases, relationAliases, f);
  }

  private static void addRelationToMenu(Menu m, final Relation root,
      final List<Either3<Label, Integer, Unit>> path,
      final Either<Relation, List<Either3<Label, Integer, Unit>>> rp, String ls,
      String space, CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalRelation, String> relationAliases,
      final F<List<Either3<Label, Integer, Unit>>, Unit> f) {
    Optional<Abstraction> ea =
        rp.isY() ? OptionalUtils.<Abstraction> nothing()
            : enclosingAbstraction(path, root);
    Optional<String> ra =
        relationAliases.get(new CanonicalRelation(root, path));
    m.add(
        space
            + ls.substring(0, Math.min(10, ls.length()))
            + " "
            + (ra.isNothing() ? renderRelation(ea.isNothing()
                ? OptionalUtils.<Code> nothing()
                : some(ea.some().x.i), rp, codeLabelAliases) : ra.some().x))
        .setOnMenuItemClickListener(new OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem _) {
            f.f(path);
            return true;
          }
        }).setEnabled(!rp.isY());
    if (!rp.isY())
      for (Pair<Either3<Label, Integer, Unit>, Either<Relation, List<Either3<Label, Integer, Unit>>>> e : iter(edges(rp
          .x()))) {
        addRelationToMenu(m, root, append(e.x, path), e.y,
            renderPathElement(e.x), space + "  ", codeLabelAliases,
            relationAliases, f);
      }
  }
}
