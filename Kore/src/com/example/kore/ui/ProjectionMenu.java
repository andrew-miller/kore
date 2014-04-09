package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.renderRelation;
import static com.example.kore.utils.CodeUtils.equal;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.OptionalUtils.some;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.PopupMenu;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.codes.Relation.Projection;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Unit;

public class ProjectionMenu {
  /** <tt>c</tt> and <tt>out</tt> are root codes */
  public static void make(Context context, View v,
      CodeLabelAliasMap codeLabelAliases, Code c, Code out,
      F<List<Label>, Void> select) {
    make(context, v, codeLabelAliases, ListUtils.<Label> nil(), c, out, select);
  }

  private static void make(final Context context, final View v,
      final CodeLabelAliasMap codeLabelAliases, final List<Label> proj,
      final Code c, final Code out, final F<List<Label>, Void> select) {
    PopupMenu pm = new PopupMenu(context, v);
    Menu m = pm.getMenu();
    Code o = reroot(c, proj);
    m.add(
        renderRelation(some(c), Either
            .<Relation, List<Either3<Label, Integer, Unit>>> x(Relation
                .projection(new Projection(proj, o))), codeLabelAliases))
        .setEnabled(equal(o, out))
        .setOnMenuItemClickListener(new OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem _) {
            select.f(proj);
            return true;
          }
        });
    for (final Pair<Label, ?> e : iter(o.labels.entrySet())) {
      Optional<String> a =
          codeLabelAliases.getAliases(new CanonicalCode(c, proj)).get(e.x);
      m.add(a.isNothing() ? e.x.toString() : a.some().x)
          .setOnMenuItemClickListener(new OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem _) {
              make(context, v, codeLabelAliases, append(e.x, proj), c, out,
                  select);
              return true;
            }
          });
    }
    pm.show();
  }
}
