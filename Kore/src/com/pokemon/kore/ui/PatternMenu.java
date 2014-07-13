package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.PatternUtils.emptyPattern;
import static com.pokemon.kore.ui.PatternUtils.renderPattern;
import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.CodeUtils.codeAt;
import static com.pokemon.kore.utils.CodeUtils.directPath;
import static com.pokemon.kore.utils.CodeUtils.equal;
import static com.pokemon.kore.utils.CodeUtils.reroot;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.iter;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.PopupMenu;

import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Pattern;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;

public class PatternMenu {
  public interface Listener {
    void select(Pattern p);
  }

  public static void make(final View v, final Context context,
      final Code rootCode, final List<Label> path,
      final CodeLabelAliasMap codeLabelAliases, final Pattern current,
      final Listener listener) {
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
      for (final Pair<Label, ?> e : iter(code.labels.entrySet()))
        mp = mp.put(e.x, emptyPattern);
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
      for (final Pair<Label, ?> e : iter(code.labels.entrySet())) {
        Optional<String> a = codeLabelAliases.getAliases(cc).xy.get(e.x);
        m.add((a.isNothing() ? e.x : a.some().x) + " *")
            .setOnMenuItemClickListener(new OnMenuItemClickListener() {
              public boolean onMenuItemClick(MenuItem _) {
                listener.select(new Pattern(Map.<Label, Pattern> empty().put(
                    e.x,
                    !current.fields.entrySet().isEmpty()
                        && equal(
                            reroot(rootCode,
                                directPath(append(e.x, path), rootCode)),
                            reroot(
                                rootCode,
                                directPath(
                                    append(
                                        current.fields.entrySet().cons().x.x,
                                        path), rootCode))) ? current.fields
                        .entrySet().cons().x.y : emptyPattern)));
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
