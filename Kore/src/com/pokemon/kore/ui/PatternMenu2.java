package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.PatternUtils.emptyPattern;
import static com.pokemon.kore.ui.PatternUtils.renderPattern;
import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.CodeUtils.child;
import static com.pokemon.kore.utils.CodeUtils.equal;
import static com.pokemon.kore.utils.CodeUtils.hashLink;
import static com.pokemon.kore.utils.ListUtils.iter;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;

import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Pattern;
import com.pokemon.kore.utils.ICode;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;

public class PatternMenu2 {
  public interface Listener {
    void select(Pattern p);
  }

  public static void make(View v, Context context, ICode c,
      CodeLabelAliasMap2 codeLabelAliases, Pattern current, Listener listener) {
    PopupMenu pm = new PopupMenu(context, v);
    Menu m = pm.getMenu();
    switch (c.tag()) {
    case PRODUCT:
      m.add("*").setOnMenuItemClickListener($ -> {
        listener.select(emptyPattern);
        return true;
      });
      Map<Label, Pattern> mp = Map.empty();
      for (Pair<Label, ?> e : iter(c.labels().entrySet()))
        mp = mp.put(e.x, emptyPattern);
      Pattern p = new Pattern(mp);
      m.add(renderPattern(p, c, codeLabelAliases)).setOnMenuItemClickListener(
          $ -> {
            listener.select(p);
            return true;
          });
      break;
    case UNION:
      for (Pair<Label, ?> e : iter(c.labels().entrySet())) {
        Optional<String> a =
            codeLabelAliases.getAliases(hashLink(c.link())).xy.get(e.x);
        m.add((a.isNothing() ? e.x : a.some().x) + " *")
            .setOnMenuItemClickListener(
                $ -> {
                  listener
                      .select(new Pattern(
                          Map.<Label, Pattern> empty()
                              .put(
                                  e.x,
                                  !current.fields.entrySet().isEmpty()
                                      && equal(
                                          child(c, e.x),
                                          child(c, current.fields.entrySet()
                                              .cons().x.x)) ? current.fields
                                      .entrySet().cons().x.y : emptyPattern)));
                  return true;
                });
      }
      break;
    default:
      throw boom();
    }
    pm.show();
  }
}