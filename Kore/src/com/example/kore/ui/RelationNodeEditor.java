package com.example.kore.ui;

import static com.example.kore.ui.RelationUtils.relationOrPathAt;
import static com.example.kore.ui.RelationUtils.resolve;
import static com.example.kore.utils.CodeUtils.reroot;
import static com.example.kore.utils.ListUtils.drop;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.length;
import static com.example.kore.utils.Null.notNull;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.CanonicalRelation;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.Either;
import com.example.kore.utils.Either3;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Unit;

public class RelationNodeEditor {
  interface Listener {
    void selectRelation(List<Either3<Label, Integer, Unit>> path);

    void done();

    void changeCodomain(Code c);

    void changeDomain(Code c);

    void extendComposition(List<Either3<Label, Integer, Unit>> path, Integer i,
        Either<Relation, List<Either3<Label, Integer, Unit>>> er);

    void extendUnion(List<Either3<Label, Integer, Unit>> path, Integer i,
        Either<Relation, List<Either3<Label, Integer, Unit>>> er);

    void replaceRelation(List<Either3<Label, Integer, Unit>> path,
        Either<Relation, List<Either3<Label, Integer, Unit>>> er);

    void selectPath(List<Either3<Label, Integer, Unit>> path);
  }

  class Move {
    Integer i;
  }

  /**
   * For <code>extendComposition</code>, <code>selectRelation</code>,
   * <code>extendUnion</code>, <code>replaceRelation</code>, and
   * <code>selectPath</code>, <code>path</code> starts at the relation which is
   * being edited (not the root)
   */
  public static View make(final Context context, final Relation rootRelation,
      final Listener listener, final List<Either3<Label, Integer, Unit>> path,
      final List<Code> codes, final CodeLabelAliasMap codeLabelAliases,
      final Bijection<CanonicalCode, String> codeAliases,
      Bijection<CanonicalRelation, String> relationAliases,
      RelationViewColors relationViewColors, List<Relation> relations) {
    notNull(context, rootRelation, listener, path, codes, codeLabelAliases,
        codeAliases, relationAliases, relationViewColors);
    Either<Relation, List<Either3<Label, Integer, Unit>>> rp =
        relationOrPathAt(path, rootRelation);
    Relation r = resolve(rootRelation, rp);
    View v =
        LayoutInflater.from(context).inflate(R.layout.relation_node_editor,
            null);
    LinearLayout fields = (LinearLayout) v.findViewById(R.id.layout_fields);
    v.findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        listener.done();
      }
    });

    Button newFieldButton = (Button) v.findViewById(R.id.button_new_field);
    newFieldButton.setOnTouchListener(new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN)
          v.startDrag(null, new DragShadowBuilder(), new ExtendRelation(), 0);
        return false;
      }
    });
    Button changeDomainButton =
        (Button) v.findViewById(R.id.button_change_domain);
    changeDomainButton.setText(CodeUtils.renderCode(RelationUtils.domain(r),
        ListUtils.<Label> nil(), codeLabelAliases, codeAliases, 1));
    changeDomainButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        PopupMenu pm = new PopupMenu(context, v);
        Menu m = pm.getMenu();
        for (final Code c : iter(codes))
          UIUtils.addCodeToMenu(m, c, ListUtils.<Label> nil(),
              codeLabelAliases, codeAliases, new F<List<Label>, Unit>() {
                public Unit f(List<Label> p) {
                  listener.changeDomain(reroot(c, p));
                  return unit();
                }
              });
        pm.show();
      }
    });
    Button changeCodomainButton =
        (Button) v.findViewById(R.id.button_change_codomain);
    changeCodomainButton.setText(CodeUtils.renderCode(
        RelationUtils.codomain(r), ListUtils.<Label> nil(), codeLabelAliases,
        codeAliases, 1));
    changeCodomainButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        PopupMenu pm = new PopupMenu(context, v);
        Menu m = pm.getMenu();
        for (final Code c : iter(codes))
          UIUtils.addCodeToMenu(m, c, ListUtils.<Label> nil(),
              codeLabelAliases, codeAliases, new F<List<Label>, Unit>() {
                public Unit f(List<Label> p) {
                  listener.changeCodomain(reroot(c, p));
                  return unit();
                }
              });
        pm.show();
      }
    });

    DragBro dragBro = new DragBro();
    View rv =
        RelationView.make(context, relationViewColors, dragBro, rootRelation,
            path, new RelationView.Listener() {
              public void extendUnion(List<Either3<Label, Integer, Unit>> p,
                  Integer i,
                  Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
                listener.extendUnion(drop(p, length(path)), i, er);
              }

              public void extendComposition(
                  List<Either3<Label, Integer, Unit>> p, Integer i,
                  Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
                listener.extendComposition(drop(p, length(path)), i, er);
              }

              public void select(List<Either3<Label, Integer, Unit>> p) {
                listener.selectRelation(drop(p, length(path)));
              }

              public void selectPath(List<Either3<Label, Integer, Unit>> p) {
                listener.selectPath(drop(p, length(path)));
              }

              public boolean dontAbbreviate(
                  List<Either3<Label, Integer, Unit>> p) {
                return p.equals(path);
              }

              public void replaceRelation(
                  List<Either3<Label, Integer, Unit>> p,
                  Either<Relation, List<Either3<Label, Integer, Unit>>> er) {
                listener.replaceRelation(drop(p, length(path)), er);
              }
            }, codeLabelAliases, relationAliases, relations);
    // workaround that you can't drag onto the outer 10 pixels
    FrameLayout f = new FrameLayout(context);
    f.setPadding(10, 10, 10, 10);
    f.addView(rv);
    fields.addView(f);
    return v;
  }
}