package com.example.kore.ui;

import static com.example.kore.ui.PatternUtils.emptyPattern;
import static com.example.kore.ui.PatternUtils.renderPattern;
import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.CodeUtils.codeAt;
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
import com.example.kore.codes.Pattern;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.Optional;

public class PatternMenu {
  public interface Listener {
    void select(Pattern p);
  }

  public static void make(final View v, final Context context,
      final Code rootCode, final List<Label> path,
      final CodeLabelAliasMap codeLabelAliases, final Listener listener) {
    CanonicalCode cc = new CanonicalCode(rootCode, path);
    Code code = codeAt(path, rootCode).some().x;
    PopupMenu pm = new PopupMenu(context, v);
    Menu m = pm.getMenu();
    switch (code.tag) {
    case PRODUCT:
      m.add("*").setOnMenuItemClickListener(new OnMenuItemClickListener() {
        public boolean onMenuItemClick(MenuItem _) {
          listener.select(emptyPattern);
          return true;
        }
      });
      Map<Label, Pattern> mp = Map.empty();
      for (final Entry<Label, CodeOrPath> e : iter(code.labels.entrySet()))
        mp = mp.put(e.k, emptyPattern);
      final Pattern p = new Pattern(mp);
      m.add(renderPattern(p, rootCode, path, codeLabelAliases))
          .setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem _) {
              listener.select(p);
              return true;
            }
          });
      break;
    case UNION:
      for (final Entry<Label, CodeOrPath> e : iter(code.labels.entrySet())) {
        Optional<String> a = codeLabelAliases.getAliases(cc).get(e.k);
        m.add((a.isNothing() ? e.k : a.some().x) + " *")
            .setOnMenuItemClickListener(new OnMenuItemClickListener() {
              public boolean onMenuItemClick(MenuItem _) {
                listener.select(new Pattern(Map.<Label, Pattern> empty().put(
                    e.k, emptyPattern)));
                return true;
              }
            });
      }
      break;
    default:
      throw boom();
    }
    pm.show();
  }
}