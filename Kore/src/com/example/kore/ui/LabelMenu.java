package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.PopupMenu;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.F;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.Optional;

public class LabelMenu {
  public static void make(Context context, View v, Code code,
      CodeLabelAliasMap codeLabelAliases, CanonicalCode cc,
      final F<Label, Void> f) {
    PopupMenu pm = new PopupMenu(context, v);
    Menu m = pm.getMenu();
    for (final Entry<Label, CodeOrPath> e : iter(code.labels.entrySet())) {
      Optional<String> a = codeLabelAliases.getAliases(cc).get(e.k);
      m.add(a.isNothing() ? e.k.toString() : a.some().x)
          .setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem _) {
              f.f(e.k);
              return true;
            }
          });
    }
    pm.show();
  }
}
