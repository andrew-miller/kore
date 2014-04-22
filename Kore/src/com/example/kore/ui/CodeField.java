package com.example.kore.ui;

import static com.example.kore.utils.CodeUtils.linkTree;
import static com.example.kore.utils.CodeUtils.linkTreeToCode;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.LinkTreeUtils.rebase;
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
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.Either;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Map;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class CodeField extends FrameLayout {
  public static interface Listener {
    public void codeSelected();

    public void labelSelected();

    public void fieldReplaced(Either<Code, List<Label>> cp);

    public void labelAliasChanged(String alias);
  }

  private final Context a;
  private final Listener listener;
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

  public CodeField(Context context, Listener listener, Label label,
      Either<Code, List<Label>> codeOrPath, Code rootCode, boolean selected,
      CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, List<Code> codes,
      List<Label> path, Optional<String> labelAlias) {
    super(context);
    notNull(listener, label, codeOrPath, rootCode, codeAliases, codes, path,
        labelAlias);
    this.listener = listener;
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
        listener.labelSelected();
      }
    });
    labelButton.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(final View v) {
        UIUtils.replaceWithTextEntry((ViewGroup) v.getParent(), v, a,
            label.label, new F<String, Void>() {
              public Void f(String s) {
                listener.labelAliasChanged(s);
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
        listener.codeSelected();
      }
    });
    codeButton.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(View v) {
        PopupMenu pm = new PopupMenu(a, v);
        Menu m = pm.getMenu();
        addRootCodeToMenu(m, Either.<Code, List<Label>> x(rootCode), "", "",
            ListUtils.<Label> nil());
        m.add("---");
        for (final Code c : iter(codes))
          UIUtils.addCodeToMenu(m, c, ListUtils.<Label> nil(),
              codeLabelAliases, codeAliases, new F<List<Label>, Unit>() {
                public Unit f(List<Label> p) {
                  listener.fieldReplaced(Either
                      .<Code, List<Label>> x(linkTreeToCode(rebase(
                          append(label, path), linkTree(reroot(c, p))))));
                  return unit();
                }
              });

        pm.show();
        return true;
      }

      private void addRootCodeToMenu(Menu m, Either<Code, List<Label>> cp,
          String ls, String space, final List<Label> path) {
        MenuItem i =
            m.add(space
                + ls.substring(0, Math.min(10, ls.length()))
                + " "
                + CodeUtils.renderCode(rootCode, path, codeLabelAliases,
                    codeAliases, 1));
        if (cp.tag == cp.tag.X) {
          i.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem i) {
              if (CodeUtils.validCode(CodeUtils.replaceCodeAt(rootCode,
                  append(label, CodeField.this.path),
                  Either.<Code, List<Label>> y(path))))
                listener.fieldReplaced(Either.<Code, List<Label>> y(path));
              return true;
            }
          });
          Map<Label, String> las =
              codeLabelAliases.getAliases(new CanonicalCode(rootCode, path));
          for (Pair<Label, Either<Code, List<Label>>> e : iter(cp.x().labels
              .entrySet())) {
            Optional<String> ls2 = las.get(e.x);
            addRootCodeToMenu(m, e.y, ls2.isNothing() ? e.x.label
                : ls2.some().x, space + "  ", append(e.x, path));
          }
        }
      }

    });
    codeButton.setText(CodeUtils.renderCode(rootCode, append(label, path),
        codeLabelAliases, codeAliases, 1));
  }
}
