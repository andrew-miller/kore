package com.pokemon.kore.ui;

import static com.pokemon.kore.ui.RelationUtils.codomain;
import static com.pokemon.kore.ui.RelationUtils.domain;
import static com.pokemon.kore.ui.RelationUtils.hashLink;
import static com.pokemon.kore.ui.RelationUtils.relationOrPathAt;
import static com.pokemon.kore.ui.RelationUtils.resolve;
import static com.pokemon.kore.utils.CodeUtils.hashLink;
import static com.pokemon.kore.utils.CodeUtils.icode;
import static com.pokemon.kore.utils.CodeUtils.renderCode3;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Null.notNull;
import static com.pokemon.kore.utils.PairUtils.pair;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.pokemon.kore.R;
import com.pokemon.kore.codes.Code2;
import com.pokemon.kore.codes.IRelation;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation2;
import com.pokemon.kore.codes.Relation2.Link;
import com.pokemon.kore.utils.CodeUtils;
import com.pokemon.kore.utils.CodeUtils.Resolver;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Unit;

public class RelationNodeEditor2 {
  interface Listener {
    void selectRelation(List<Either3<Label, Integer, Unit>> path);

    void done();

    void changeCodomain(Code2.Link link);

    void changeDomain(Code2.Link link);

    void extendComposition(List<Either3<Label, Integer, Unit>> path, Integer i,
        Either<Relation2, List<Either3<Label, Integer, Unit>>> er);

    void extendUnion(List<Either3<Label, Integer, Unit>> path, Integer i,
        Either<Relation2, List<Either3<Label, Integer, Unit>>> er);

    void replaceRelation(List<Either3<Label, Integer, Unit>> path,
        Either<Relation2, List<Either3<Label, Integer, Unit>>> er);

    void selectPath(List<Either3<Label, Integer, Unit>> path);

    boolean canReplaceWithRef(List<Either3<Label, Integer, Unit>> path);
  }

  class Move {
    Integer i;
  }

  /**
   * For <code>extendComposition</code>, <code>selectRelation</code>,
   * <code>extendUnion</code>, <code>replaceRelation</code>,
   * <code>selectPath</code>, and <code>canReplaceWithRef</code>,
   * <code>path</code> starts at the relation which is being edited (not
   * <code>rootRelation</code>)
   */
  public static View make(Context context, IRelation rootRelation,
      Listener listener, List<Either3<Label, Integer, Unit>> path,
      List<Code2> codes, CodeLabelAliasMap2 codeLabelAliases,
      Bijection<Code2.Link, String> codeAliases,
      Bijection<Link, String> relationAliases,
      RelationViewColors2 relationViewColors, List<Relation2> relations,
      Resolver cr) {
    notNull(context, rootRelation, listener, path, codes, codeLabelAliases,
        codeAliases, relationAliases, relationViewColors);
    IRelation r = resolve(rootRelation, path).x;
    View v =
        LayoutInflater.from(context).inflate(R.layout.relation_node_editor,
            null);
    LinearLayout fields = (LinearLayout) v.findViewById(R.id.layout_fields);
    v.findViewById(R.id.button_done).setOnClickListener($ -> listener.done());

    Button newFieldButton = (Button) v.findViewById(R.id.button_new_field);
    newFieldButton.setOnTouchListener(($v, e) -> {
      if (e.getAction() == MotionEvent.ACTION_DOWN)
        $v.startDrag(null, new DragShadowBuilder(), new ExtendRelation(), 0);
      return false;
    });
    Button changeDomainButton =
        (Button) v.findViewById(R.id.button_change_domain);
    changeDomainButton.setText(renderCode3(domain(r), nil(), codeLabelAliases,
        codeAliases, 1));
    changeDomainButton.setOnClickListener($v -> {
      PopupMenu pm = new PopupMenu(context, $v);
      Menu m = pm.getMenu();
      for (Code2 c : iter(codes))
        UIUtils.addCodeToMenu3(m, icode(c, cr), nil(), codeLabelAliases,
            codeAliases, p -> {
              listener.changeDomain(hashLink(pair(c, p)));
              return unit();
            });
      pm.show();
    });
    Button changeCodomainButton =
        (Button) v.findViewById(R.id.button_change_codomain);
    changeCodomainButton.setText(CodeUtils.renderCode3(codomain(r), nil(),
        codeLabelAliases, codeAliases, 1));
    changeCodomainButton.setOnClickListener($v -> {
      PopupMenu pm = new PopupMenu(context, $v);
      Menu m = pm.getMenu();
      for (Code2 c : iter(codes))
        UIUtils.addCodeToMenu3(m, icode(c, cr), nil(), codeLabelAliases,
            codeAliases, p -> {
              listener.changeCodomain(hashLink(pair(c, p)));
              return unit();
            });
      pm.show();
    });

    Either<IRelation, List<Either3<Label, Integer, Unit>>> rp =
        relationOrPathAt(path, rootRelation);
    DragBro dragBro = new DragBro();
    View rv =
        RelationView2.make(
            context,
            relationViewColors,
            dragBro,
            rp.tag == Either.Tag.X ? Either.x(rp.x()) : Either.y(pair(
                relationAliases.xy.get(hashLink(r.link)), rp.y())),
            new RelationView2.Listener() {
              public void extendUnion(List<Either3<Label, Integer, Unit>> p,
                  Integer i,
                  Either<Relation2, List<Either3<Label, Integer, Unit>>> er) {
                listener.extendUnion(p, i, er);
              }

              public void extendComposition(
                  List<Either3<Label, Integer, Unit>> p, Integer i,
                  Either<Relation2, List<Either3<Label, Integer, Unit>>> er) {
                listener.extendComposition(p, i, er);
              }

              public void select(List<Either3<Label, Integer, Unit>> p) {
                listener.selectRelation(p);
              }

              public void selectPath(List<Either3<Label, Integer, Unit>> p) {
                listener.selectPath(p);
              }

              public boolean dontAbbreviate(
                  List<Either3<Label, Integer, Unit>> p) {
                return p.equals(path);
              }

              public boolean canReplaceWithRef(
                  List<Either3<Label, Integer, Unit>> path) {
                return listener.canReplaceWithRef(path);
              }

              public void replaceRelation(
                  List<Either3<Label, Integer, Unit>> p,
                  Either<Relation2, List<Either3<Label, Integer, Unit>>> er) {
                listener.replaceRelation(p, er);
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