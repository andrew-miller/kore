package com.example.kore.ui;

import com.example.kore.utils.F;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class UIUtils {

  public static void replaceWithTextEntry(final ViewGroup vg, final View v,
      final Context a, String hint, final F<String, Void> onDone) {
    vg.removeView(v);
    final EditText t = new EditText(a);
    t.requestFocus();
    t.setImeOptions(EditorInfo.IME_ACTION_DONE);
    t.setInputType(EditorInfo.TYPE_CLASS_TEXT);
    t.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          onDone.f(t.getText().toString());
          return true;
        }
        return false;
      }
    });
    t.setOnFocusChangeListener(new OnFocusChangeListener() {
      @Override
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
}
