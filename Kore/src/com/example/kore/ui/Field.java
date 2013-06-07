package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.CodeOrPath.Tag;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.MapUtils;
import com.example.kore.utils.Optional;

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
  public static final String ARG_CODE_LABEL_ALIASES = "code_label_aliases";
  public static final String ARG_CODE_ALIASES = "code_aliases";
  public static final String ARG_CODES = "codes";
  /** The path to the code containing this field */
  public static final String ARG_PATH = "path";
  public static final String ARG_LABEL_ALIAS = "label_alias";

  public static interface CodeSelectedListener {
    public void codeSelected(Label l);
  }

  public static interface LabelSelectedListener {
    public void labelSelected(Label l);
  }

  public static interface FieldReplacedListener {
    public void fieldReplaced(CodeOrPath cp, Label l);
  }

  public static interface LabelAliasChangedListener {
    public void labelAliasChanged(Label l, String alias);
  }

  private CodeSelectedListener codeSelectedListener;
  private LabelSelectedListener labelSelectedListener;
  private FieldReplacedListener fieldReplacedListener;
  private LabelAliasChangedListener labelAliasChangedListener;

  private Label label;
  private CodeOrPath codeOrPath;
  private Code rootCode;
  private boolean selected;
  private HashMap<CanonicalCode, HashMap<Label, String>> codeLabelAliases;
  private FragmentActivity a;
  private Button labelButton;
  private Button codeButton;
  private Map<CanonicalCode, String> codeAliases;
  private List<Code> codes;
  private List<Label> path;
  private Optional<String> labelAlias;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    codeSelectedListener = (CodeSelectedListener) activity;
    labelSelectedListener = (LabelSelectedListener) activity;
    fieldReplacedListener = (FieldReplacedListener) activity;
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
    codeLabelAliases =
        MapUtils
            .cloneNestedMap((HashMap<CanonicalCode, HashMap<Label, String>>) args
                .get(ARG_CODE_LABEL_ALIASES));
    codeAliases = (Map<CanonicalCode, String>) args.get(ARG_CODE_ALIASES);
    codes = (List<Code>) args.get(ARG_CODES);
    codes.checkType(Code.class);
    path = (List<Label>) args.get(ARG_PATH);
    path.checkType(Label.class);
    labelAlias = (Optional<String>) args.get(ARG_LABEL_ALIAS);
    labelAlias.checkType(String.class);
    notNull(label, codeOrPath, rootCode, codeAliases);

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
    labelButton.setText(labelAlias.isNothing() ? label.label : labelAlias
        .some().x);
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
        addRootCodeToMenu(m, CodeOrPath.newCode(rootCode), "", "",
            nil(Label.class));
        m.add("---");
        for (Code c : iter(codes))
          addOtherCodeToMenu(c, m, CodeOrPath.newCode(c), "", "",
              nil(Label.class));

        pm.show();
        return true;
      }

      private void addRootCodeToMenu(Menu m, CodeOrPath cp, String ls,
          String space, final List<Label> path) {
        MenuItem i =
            m.add(space
                + ls.substring(0, Math.min(10, ls.length()))
                + " "
                + CodeUtils.renderCode(rootCode, path, codeLabelAliases,
                    codeAliases, 1));
        if (cp.tag == Tag.CODE) {
          i.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem i) {
              if (CodeUtils.validCode(CodeUtils.replaceCodeAt(rootCode,
                  append(label, Field.this.path), CodeOrPath.newPath(path))))
                fieldReplacedListener.fieldReplaced(CodeOrPath.newPath(path),
                    label);
              return true;
            }
          });
          HashMap<Label, String> las =
              codeLabelAliases.get(new CanonicalCode(rootCode, path));
          for (Entry<Label, CodeOrPath> e : cp.code.labels.entrySet()) {
            ls = las == null ? null : las.get(e.getKey());
            addRootCodeToMenu(m, e.getValue(), ls == null ? e.getKey().label
                : ls, space + "  ", append(e.getKey(), path));
          }
        }
      }

      private void addOtherCodeToMenu(final Code root, Menu m, CodeOrPath cp,
          String ls, String space, final List<Label> path) {
        MenuItem i =
            m.add(space
                + ls.substring(0, Math.min(10, ls.length()))
                + " "
                + CodeUtils.renderCode(root, path, codeLabelAliases,
                    codeAliases, 1));
        i.setOnMenuItemClickListener(new OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem i) {
            fieldReplacedListener.fieldReplaced(
                CodeOrPath.newCode(CodeUtils.rebase(
                    append(label, Field.this.path),
                    CodeUtils.reRoot(root, path))), label);
            return true;
          }
        });
        HashMap<Label, String> las =
            codeLabelAliases.get(new CanonicalCode(root, path));
        if (cp.tag == CodeOrPath.Tag.CODE) {
          for (Entry<Label, CodeOrPath> e : cp.code.labels.entrySet()) {
            ls = las == null ? null : las.get(e.getKey());
            addOtherCodeToMenu(root, m, e.getValue(),
                ls == null ? e.getKey().label : ls, space + "  ",
                append(e.getKey(), path));
          }
        }
      }

    });
    codeButton.setText(CodeUtils.renderCode(rootCode, append(label, path),
        codeLabelAliases, codeAliases, 1));
  }
}
