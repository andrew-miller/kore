package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.PopupMenu;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.F;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;

public class LabelMenu {
  public static void make(Context context, View v,
      CodeLabelAliasMap codeLabelAliases, CanonicalCode cc,
      final F<Label, Void> f) {
    PopupMenu pm = new PopupMenu(context, v);
    Menu m = pm.getMenu();
    for (final Pair<Label, CodeOrPath> e : iter(cc.code.labels.entrySet())) {
      Optional<String> a = codeLabelAliases.getAliases(cc).get(e.x);
      m.add(a.isNothing() ? e.x.toString() : a.some().x)
          .setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem _) {
              f.f(e.x);
              return true;
            }
          });
    }
    pm.show();
  }
}
