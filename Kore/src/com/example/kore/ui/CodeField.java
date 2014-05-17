package com.example.kore.ui;

import static com.example.kore.utils.CodeUtils.directPath;
import static com.example.kore.utils.CodeUtils.linkTree;
import static com.example.kore.utils.CodeUtils.linkTreeToCode;
import static com.example.kore.utils.CodeUtils.renderCode;
import static com.example.kore.utils.CodeUtils.replaceCodeAt;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.CodeUtils.validCode;
import static com.example.kore.utils.LinkTreeUtils.rebase;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Null.notNull;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.Either;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Unit;

public class CodeField {
  public static interface Listener {
    public void selectCode();

    public void selectLabel();

    public void replaceField(Either<Code, List<Label>> cp);

    public void changeLabelAlias(String alias);
  }

  public static View make(final Context context, final Listener listener,
      final Label label, Either<Code, List<Label>> codeOrPath,
      final Code rootCode, boolean selected,
      final CodeLabelAliasMap codeLabelAliases,
      final Bijection<CanonicalCode, String> codeAliases,
      final List<Code> codes, final List<Label> path,
      Optional<String> labelAlias) {
    notNull(context, listener, label, codeOrPath, rootCode, codeLabelAliases,
        codeAliases, codes, path, labelAlias);
    View v = LayoutInflater.from(context).inflate(R.layout.code_field, null);
    Button labelButton = (Button) v.findViewById(R.id.button_label);
    labelButton.setBackgroundColor((int) Long.parseLong(
        label.label.substring(0, 8), 16));
    labelButton.setText(labelAlias.isNothing() ? label.label : labelAlias
        .some().x);
    if (selected)
      labelButton.setText("---");
    labelButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        listener.selectLabel();
      }
    });
    labelButton.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(final View v) {
        UIUtils.replaceWithTextEntry((ViewGroup) v.getParent(), v, context,
            label.label, new F<String, Void>() {
              public Void f(String s) {
                listener.changeLabelAlias(s);
                return null;
              }
            });
        return true;
      }
    });
    Button codeButton = (Button) v.findViewById(R.id.button_code);
    codeButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        listener.selectCode();
      }
    });
    codeButton.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(View v) {
        PopupMenu pm = new PopupMenu(context, v);
        Menu m = pm.getMenu();
        UIUtils.addCodeToMenu(m, rootCode, ListUtils.<Label> nil(),
            codeLabelAliases, codeAliases, new F<List<Label>, Unit>() {
              public Unit f(List<Label> p) {
                p = directPath(p, rootCode);
                if (validCode(replaceCodeAt(rootCode, append(label, path),
                    Either.<Code, List<Label>> y(p))))
                  listener.replaceField(Either.<Code, List<Label>> y(p));
                return unit();
              }
            });
        m.add("---");
        for (final Code c : iter(codes))
          UIUtils.addCodeToMenu(m, c, ListUtils.<Label> nil(),
              codeLabelAliases, codeAliases, new F<List<Label>, Unit>() {
                public Unit f(List<Label> p) {
                  Either<Code, List<Label>> n =
                      Either.<Code, List<Label>> x(linkTreeToCode(rebase(
                          append(label, path), linkTree(reroot(c, p)))));
                  if (validCode(replaceCodeAt(rootCode, append(label, path), n)))
                    listener.replaceField(n);
                  return unit();
                }
              });
        pm.show();
        return true;
      }
    });
    codeButton.setText(renderCode(rootCode, append(label, path),
        codeLabelAliases, codeAliases, 1));
    return v;
  }

}
