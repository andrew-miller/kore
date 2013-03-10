package com.example.kore.ui;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.unsuck.Null;

public class Field extends Fragment {
  public static final String ARG_LABEL = "label";
  public static final String ARG_CODE_REF = "codeRef";
  public static final String ARG_ROOT_CODE = "rootCode";
  public static final String ARG_SELECTED = "selected";
  public static final String ARG_LABEL_ALIASES = "labelAliases";

  public static interface CodeSelectedListener {
    public void codeSelected(Label l);
  }

  public static interface LabelSelectedListener {
    public void labelSelected(Label l);
  }

  public static interface FieldChangedListener {
    public void fieldChanged(List<Label> path, Label label);
  }

  public static interface LabelAliasChangedListener {
    public void labelAliasChanged(Label label, String alias);
  }

  private CodeSelectedListener codeSelectedListener;
  private LabelSelectedListener labelSelectedListener;
  private FieldChangedListener fieldChangedListener;
  private LabelAliasChangedListener labelAliasChangedListener;

  private Label label;
  private Code codeRef;
  private Code rootCode;
  private boolean selected;
  private Map<Label, String> labelAliases;
  private FragmentActivity a;
  private Button labelButton;
  private Button codeButton;

  private final LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
      0, LayoutParams.MATCH_PARENT, 1);

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    codeSelectedListener = (CodeSelectedListener) activity;
    labelSelectedListener = (LabelSelectedListener) activity;
    fieldChangedListener = (FieldChangedListener) activity;
    labelAliasChangedListener = (LabelAliasChangedListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    Bundle args = getArguments();
    label = (Label) args.get(ARG_LABEL);
    codeRef = (Code) args.get(ARG_CODE_REF);
    rootCode = (Code) args.get(ARG_ROOT_CODE);
    selected = args.getBoolean(ARG_SELECTED);

    Map<Label, String> labelAliasesUnsafe = (Map<Label, String>) args
        .get(ARG_LABEL_ALIASES);
    labelAliases = Collections.unmodifiableMap(labelAliasesUnsafe);

    Null.notNull(label, rootCode);

    View v = inflater.inflate(R.layout.field, container, false);
    a = getActivity();

    labelButton = (Button) v.findViewById(R.id.label);
    initLabelButton();
    codeButton = (Button) v.findViewById(R.id.code);
    initCodeButton();
    return v;
  }

  private void initLabelButton() {
    labelButton.setBackgroundColor((int) Long.parseLong(
        label.label.substring(0, 8), 16));
    String labelAlias = labelAliases.get(label);
    if (labelAlias == null) {
      labelButton.setText(label.label);
    } else {
      labelButton.setText(labelAlias);
    }
    if (selected)
      labelButton.setText("---");
    labelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        labelSelectedListener.labelSelected(label);
      }
    });
    labelButton.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        final LinearLayout ll = (LinearLayout) v.getParent();
        ll.removeView(v);
        v = ll.getChildAt(0);
        ll.removeView(v);
        final EditText t = new EditText(a);
        t.requestFocus();
        t.setImeOptions(EditorInfo.IME_ACTION_DONE);
        t.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        t.setOnEditorActionListener(new OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
              labelAliasChangedListener.labelAliasChanged(label, t.getText()
                  .toString());
              hideKeyboard();
              return true;
            }
            return false;
          }

          private void hideKeyboard() {
            InputMethodManager inputManager = (InputMethodManager) a
                .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.toggleSoftInput(0, 0);
          }
        });
        t.setOnFocusChangeListener(new OnFocusChangeListener() {
          @Override
          public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
              ll.removeAllViews();
              labelButton = new Button(a);
              codeButton = new Button(a);
              ll.addView(labelButton, buttonParams);
              ll.addView(codeButton, buttonParams);
              initLabelButton();
              initCodeButton();
            }
          }
        });
        t.setHint(label.label);
        ll.addView(t, buttonParams);
        ll.addView(v, buttonParams);
        return true;
      }
    });
  }

  private void initCodeButton() {
    codeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        codeSelectedListener.codeSelected(label);
      }
    });

    codeButton.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        PopupMenu pm = new PopupMenu(a, v);
        Menu m = pm.getMenu();
        fillMenu(m, rootCode, null, "", new LinkedList<Label>());
        pm.show();
        return true;
      }

      private void fillMenu(Menu m, Code codeRef, Label l, String space,
          final List<Label> path) {
        String ls;
        if (l == null) {
          ls = "";
        } else {
          String la = labelAliases.get(l);
          ls = la == null ? l.toString() : la;
        }
        MenuItem i = m.add(space + ls.substring(0, Math.min(10, ls.length()))
            + " " + CodeUtils.renderCode(codeRef, labelAliases, 1));
        i.setOnMenuItemClickListener(new OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem i) {
            fieldChangedListener.fieldChanged(path, label);
            return true;
          }
        });
        for (Entry<Label, Code> e : codeRef.labels.entrySet()) {
          List<Label> path2 = new LinkedList<Label>(path);
          path2.add(e.getKey());
          fillMenu(m, e.getValue(), e.getKey(), space + " ", path2);
        }

      }

    });
    // codeButton.setText(CodeUtils.renderCode(codeRef, labelAliases, 1));
    codeButton.setText(codeRef.toString(labelAliases));
  }
}
