package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
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
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.Optional;
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
          // WTF if you change the above line to this, it messes up:
          // fl.removeView(v);
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
        m.add(space
            + ls.substring(0, Math.min(10, ls.length()))
            + " "
            + CodeUtils
                .renderCode(root, path, codeLabelAliases, codeAliases, 1));
    i.setOnMenuItemClickListener(new OnMenuItemClickListener() {
      public boolean onMenuItemClick(MenuItem _) {
        f.f(path);
        return true;
      }
    });
    Map<Label, String> las =
        codeLabelAliases.getAliases(new CanonicalCode(root, path));
    if (cp.tag == CodeOrPath.Tag.CODE) {
      for (Entry<Label, CodeOrPath> e : iter(cp.code.labels.entrySet())) {
        Optional<String> ls2 = las.get(e.k);
        addCodeToMenu(m, root, append(e.k, path), e.v,
            ls2.isNothing() ? e.k.label : ls2.some().x, space + "  ",
            codeLabelAliases, codeAliases, f);
      }
    }
  }

}
