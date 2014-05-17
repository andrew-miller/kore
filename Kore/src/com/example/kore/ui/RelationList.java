package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.kore.R;
import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Optional;
import com.example.kore.utils.OptionalUtils;
import com.example.kore.utils.Unit;

public class RelationList {

  public static interface Listener {
    public void select(Relation r);

    public boolean changeAlias(Relation relation,
        List<Either3<Label, Integer, Unit>> path, String alias);
  }

  public static View make(final Context context, final Listener listener,
      List<Relation> relations, CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalRelation, String> relationAliases) {
    notNull(context, relations, listener, codeLabelAliases, relationAliases);
    View v = LayoutInflater.from(context).inflate(R.layout.relation_list, null);
    LinearLayout relationListLayout =
        (LinearLayout) v.findViewById(R.id.layout_relation_list);
    for (final Relation relation : iter(relations)) {
      final FrameLayout fl = new FrameLayout(context);
      Button b = new Button(context);
      Optional<String> relationName =
          relationAliases.xy.get(new CanonicalRelation(relation, ListUtils
              .<Either3<Label, Integer, Unit>> nil()));
      final String strRelation =
          relationName.isNothing() ? RelationUtils.renderRelation(OptionalUtils
              .<Code> nothing(), Either
              .<Relation, List<Either3<Label, Integer, Unit>>> x(relation),
              codeLabelAliases) : relationName.some().x;
      b.setText(strRelation);
      b.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          listener.select(relation);
        }
      });
      fl.addView(b);

      b.setOnLongClickListener(new OnLongClickListener() {
        public boolean onLongClick(final View v) {
          UIUtils.replaceWithTextEntry(fl, v, context, strRelation,
              new F<String, Void>() {
                public Void f(String s) {
                  listener.changeAlias(relation,
                      ListUtils.<Either3<Label, Integer, Unit>> nil(), s);
                  return null;
                }
              });
          return true;
        }
      });

      relationListLayout.addView(fl);
    }
    return v;
  }
}