package com.example.kore.ui;

import static com.example.kore.utils.CodeUtils.rebase;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Null.notNull;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupMenu;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.CodeOrPath.Tag;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Unit;

public class CodeField extends FrameLayout {
  public static interface CodeSelectedListener {
    public void codeSelected();
  }

  public static interface LabelSelectedListener {
    public void labelSelected();
  }

  public static interface FieldReplacedListener {
    public void fieldReplaced(CodeOrPath cp);
  }

  public static interface LabelAliasChangedListener {
    public void labelAliasChanged(String alias);
  }

  private final Context a;
  private final CodeSelectedListener codeSelectedListener;
  private final LabelSelectedListener labelSelectedListener;
  private final FieldReplacedListener fieldReplacedListener;
  private final LabelAliasChangedListener labelAliasChangedListener;
  private final Label label;
  private final Code rootCode;
  private final boolean selected;
  private final CodeLabelAliasMap codeLabelAliases;
  private final Button labelButton;
  private final Button codeButton;
  private final Map<CanonicalCode, String> codeAliases;
  private final List<Code> codes;
  private final List<Label> path;
  private final Optional<String> labelAlias;

  public CodeField(Context context, CodeSelectedListener codeSelectedListener,
      LabelSelectedListener labelSelectedListener,
      FieldReplacedListener fieldReplacedListener,
      LabelAliasChangedListener labelAliasChangedListener, Label label,
      CodeOrPath codeOrPath, Code rootCode, boolean selected,
      CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, List<Code> codes,
      List<Label> path, Optional<String> labelAlias) {
    super(context);
    notNull(codeSelectedListener, labelSelectedListener, fieldReplacedListener,
        labelAliasChangedListener, label, codeOrPath, rootCode, codeAliases,
        codes, path, labelAlias);
    this.codeSelectedListener = codeSelectedListener;
    this.labelSelectedListener = labelSelectedListener;
    this.fieldReplacedListener = fieldReplacedListener;
    this.labelAliasChangedListener = labelAliasChangedListener;
    this.label = label;
    this.rootCode = rootCode;
    this.selected = selected;
    this.codeLabelAliases = codeLabelAliases;
    this.codeAliases = codeAliases;
    this.codes = codes;
    this.path = path;
    this.labelAlias = labelAlias;
    this.a = context;
    View v =
        LayoutInflater.from(context).inflate(R.layout.code_field, this, true);
    labelButton = (Button) v.findViewById(R.id.button_label);
    initLabelButton();
    codeButton = (Button) v.findViewById(R.id.button_code);
    initCodeButton();
  }

  private void initLabelButton() {
    labelButton.setBackgroundColor((int) Long.parseLong(
        label.label.substring(0, 8), 16));
    labelButton.setText(labelAlias.isNothing() ? label.label : labelAlias
        .some().x);
    if (selected)
      labelButton.setText("---");
    labelButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        labelSelectedListener.labelSelected();
      }
    });
    labelButton.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(final View v) {
        UIUtils.replaceWithTextEntry((ViewGroup) v.getParent(), v, a,
            label.label, new F<String, Void>() {
              public Void f(String s) {
                labelAliasChangedListener.labelAliasChanged(s);
                return null;
              }
            });
        return true;
      }
    });
  }

  private void initCodeButton() {
    codeButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        codeSelectedListener.codeSelected();
      }
    });
    codeButton.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(View v) {
        PopupMenu pm = new PopupMenu(a, v);
        Menu m = pm.getMenu();
        addRootCodeToMenu(m, CodeOrPath.newCode(rootCode), "", "",
            ListUtils.<Label> nil());
        m.add("---");
        for (final Code c : iter(codes))
          UIUtils.addCodeToMenu(m, c, ListUtils.<Label> nil(),
              codeLabelAliases, codeAliases, new F<List<Label>, Unit>() {
                public Unit f(List<Label> p) {
                  fieldReplacedListener.fieldReplaced(CodeOrPath
                      .newCode(rebase(append(label, path), reroot(c, p))));
                  return unit();
                }
              });

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
            public boolean onMenuItemClick(MenuItem i) {
              if (CodeUtils.validCode(CodeUtils.replaceCodeAt(rootCode,
                  append(label, CodeField.this.path), CodeOrPath.newPath(path))))
                fieldReplacedListener.fieldReplaced(CodeOrPath.newPath(path));
              return true;
            }
          });
          Map<Label, String> las =
              codeLabelAliases.getAliases(new CanonicalCode(rootCode, path));
          for (Entry<Label, CodeOrPath> e : iter(cp.code.labels.entrySet())) {
            Optional<String> ls2 = las.get(e.k);
            addRootCodeToMenu(m, e.v, ls2.isNothing() ? e.k.label
                : ls2.some().x, space + "  ", append(e.k, path));
          }
        }
      }

    });
    codeButton.setText(CodeUtils.renderCode(rootCode, append(label, path),
        codeLabelAliases, codeAliases, 1));
  }
}
