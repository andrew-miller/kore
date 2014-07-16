package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.emptyRelationViewListener;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;

import com.pokemon.kore.R;
import com.pokemon.kore.codes.CanonicalRelation;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.SARef;
import com.pokemon.kore.utils.Unit;

public class RelationList {

  public static interface Listener {
    public void select(Relation r);

    public boolean changeAlias(Relation relation,
        List<Either3<Label, Integer, Unit>> path, String alias);
  }

  public static View make(Context context, Listener listener,
      List<Relation> relations, CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      RelationViewColors rvc) {
    notNull(context, relations, listener, codeLabelAliases, relationAliases);
    View v = LayoutInflater.from(context).inflate(R.layout.relation_list, null);
    LinearLayout relationListLayout =
        (LinearLayout) v.findViewById(R.id.layout_relation_list);
    boolean first = true;
    for (Relation relation : iter(relations)) {
      if (!first) {
        Space s = new Space(context);
        s.setMinimumHeight(1);
        relationListLayout.addView(s);
      }
      first = false;
      FrameLayout fl = new FrameLayout(context);
      SARef<View> overlay = new SARef<>();
      overlay.set(Overlay.make(context, RelationView.make(context, rvc,
          new DragBro(), relation, nil(), emptyRelationViewListener,
          codeLabelAliases, relationAliases, relations),
          new Overlay.Listener() {
            public boolean onLongClick() {
              UIUtils.replaceWithTextEntry(fl, overlay.get(), context, "",
                  s -> {
                    listener.changeAlias(relation, nil(), s);
                    return null;
                  });
              return true;
            }

            public void onClick() {
              listener.select(relation);
            }
          }));
      fl.addView(overlay.get());
      relationListLayout.addView(fl);
    }
    return v;
  }
}