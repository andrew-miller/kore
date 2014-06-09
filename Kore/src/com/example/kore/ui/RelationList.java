package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.emptyRelationViewListener;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;

import com.example.kore.R;
import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Ref;
import com.example.kore.utils.Unit;

public class RelationList {

  public static interface Listener {
    public void select(Relation r);

    public boolean changeAlias(Relation relation,
        List<Either3<Label, Integer, Unit>> path, String alias);
  }

  public static View make(final Context context, final Listener listener,
      List<Relation> relations, CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      RelationViewColors rvc) {
    notNull(context, relations, listener, codeLabelAliases, relationAliases);
    View v = LayoutInflater.from(context).inflate(R.layout.relation_list, null);
    LinearLayout relationListLayout =
        (LinearLayout) v.findViewById(R.id.layout_relation_list);
    boolean first = true;
    for (final Relation relation : iter(relations)) {
      if (!first) {
        Space s = new Space(context);
        s.setMinimumHeight(1);
        relationListLayout.addView(s);
      }
      first = false;
      final FrameLayout fl = new FrameLayout(context);
      final Ref<View> overlay = new Ref<View>(new View(context));
      overlay.set(Overlay.make(context, RelationView.make(context, rvc,
          new DragBro(), relation,
          ListUtils.<Either3<Label, Integer, Unit>> nil(),
          emptyRelationViewListener, codeLabelAliases, relationAliases),
          new Overlay.Listener() {
            public boolean onLongClick() {
              UIUtils.replaceWithTextEntry(fl, overlay.get(), context, "",
                  new F<String, Void>() {
                    public Void f(String s) {
                      listener.changeAlias(relation,
                          ListUtils.<Either3<Label, Integer, Unit>> nil(), s);
                      return null;
                    }
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