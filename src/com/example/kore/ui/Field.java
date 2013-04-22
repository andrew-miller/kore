package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;

import java.util.Map;
import java.util.Map.Entry;
import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.F;
import com.example.kore.utils.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

public class Field extends Fragment {
  public static final String ARG_LABEL = "label";
  public static final String ARG_CODE_OR_PATH = "code_or_path";
  public static final String ARG_ROOT_CODE = "root_code";
  public static final String ARG_SELECTED = "selected";
  public static final String ARG_LABEL_ALIASES = "label_aliases";
  public static final String ARG_CODE_ALIASES = "code_aliases";

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
  private CodeOrPath codeOrPath;
  private Code rootCode;
  private boolean selected;
  private Map<Label, String> labelAliases;
  private FragmentActivity a;
  private Button labelButton;
  private Button codeButton;
  private Map<Code, String> codeAliases;

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
    codeOrPath = (CodeOrPath) args.get(ARG_CODE_OR_PATH);
    rootCode = (Code) args.get(ARG_ROOT_CODE);
    selected = args.getBoolean(ARG_SELECTED);
    labelAliases = (Map<Label, String>) args.get(ARG_LABEL_ALIASES);
    codeAliases = (Map<Code, String>) args.get(ARG_CODE_ALIASES);
    notNull(label, codeOrPath, rootCode, labelAliases, codeAliases);

    View v = inflater.inflate(R.layout.field, container, false);
    a = getActivity();

    labelButton = (Button) v.findViewById(R.id.button_label);
    initLabelButton();
    codeButton = (Button) v.findViewById(R.id.button_code);
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
      public boolean onLongClick(final View v) {
        UIUtils.replaceWithTextEntry((ViewGroup) v.getParent(), v, a,
            label.label, new F<String, Void>() {
              @Override
              public Void f(String s) {
                labelAliasChangedListener.labelAliasChanged(label, s);
                return null;
              }
            });
        return true;
      }
    });
  }

  private void initCodeButton() {
    if (codeOrPath.tag == CodeOrPath.Tag.CODE) {
      codeButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          codeSelectedListener.codeSelected(label);
        }
      });
    }
    codeButton.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        PopupMenu pm = new PopupMenu(a, v);
        Menu m = pm.getMenu();
        fillMenu(m, CodeOrPath.newCode(rootCode), null, "", nil(Label.class));
        pm.show();
        return true;
      }

      private void fillMenu(Menu m, CodeOrPath codeOrPath, Label l, String space,
          final List<Label> path) {
        String ls;
        if (l == null) {
          ls = "";
        } else {
          String la = labelAliases.get(l);
          ls = la == null ? l.toString() : la;
        }
        MenuItem i =
            m.add(space + ls.substring(0, Math.min(10, ls.length())) + " "
                + CodeUtils.renderCode(codeOrPath, labelAliases, codeAliases, 1));
        i.setOnMenuItemClickListener(new OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem i) {
            fieldChangedListener.fieldChanged(path, label);
            return true;
          }
        });
        if (codeOrPath.tag == CodeOrPath.Tag.CODE) {
          for (Entry<Label, CodeOrPath> e : codeOrPath.code.labels.entrySet()) {
            fillMenu(m, e.getValue(), e.getKey(), space + " ",
                append(e.getKey(), path));
          }
        }

      }

    });
    codeButton.setText(CodeUtils.renderCode(codeOrPath, labelAliases, codeAliases,
        1));
  }
}
