package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.CodeUtils.canReplace;
import static com.pokemon.kore.utils.CodeUtils.codeAt2;
import static com.pokemon.kore.utils.CodeUtils.hash;
import static com.pokemon.kore.utils.CodeUtils.icode;
import static com.pokemon.kore.utils.CodeUtils.longestValidSubPath2;
import static com.pokemon.kore.utils.CodeUtils.replaceCodeAt2;
import static com.pokemon.kore.utils.CodeUtils.unit2;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.isPrefix;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.MapUtils.containsKey;
import static com.pokemon.kore.utils.Null.notNull;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.OptionalUtils.some;
import static com.pokemon.kore.utils.PairUtils.pair;
import static com.pokemon.kore.utils.Unit.unit;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pokemon.kore.R;
import com.pokemon.kore.codes.Code2;
import com.pokemon.kore.codes.Code2.Link;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.З2Bytes;
import com.pokemon.kore.utils.CodeUtils.Resolver;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Random;
import com.pokemon.kore.utils.Unit;

public class CodeEditor2 {
  private static final String STATE_CODE = "code";
  private static final String STATE_PATH = "path";
  private static final String STATE_PATH_SHADOW = "path_shadow";

  static class S {
    View nodeEditor;
    Code2 code = unit2;
    List<Label> path;
    List<Label> pathShadow;
    F<Unit, Unit> initNodeEditor;
    Map<З2Bytes, Code2> newCodes = Map.empty();
  }

  private static Pair<View, F<Unit, Bundle>> make(final Context context,
      Code2 code, final CodeLabelAliasMap2 codeLabelAliases,
      final Bijection<Link, String> codeAliases, final List<Code2> codes,
      List<Label> path, List<Label> pathShadow, Resolver rs,
      final F<Pair<Code2, Map<З2Bytes, Code2>>, Unit> done) {
    notNull(context, code, codeLabelAliases, codeAliases, codes, path,
        pathShadow, done);
    final View v =
        LayoutInflater.from(context).inflate(R.layout.code_editor, null);
    final ViewGroup pathContainer =
        (ViewGroup) v.findViewById(R.id.container_path);

    final S s = new S();
    s.pathShadow = pathShadow;
    s.path = path;
    s.code = code;

    Resolver r = new Resolver() {
      public Optional<Code2> resolve(З2Bytes hash) {
        Optional<Code2> oh = rs.resolve(hash);
        return oh.isNothing() ? s.newCodes.get(hash) : oh;
      }
    };

    final F<List<Label>, Unit> setPath = new F<List<Label>, Unit>() {
      public Unit f(List<Label> $path) {
        pathContainer.removeAllViews();
        pathContainer.addView(CodePath2.make(context, p -> {
          notNull(p);
          Optional<Code2> oc = codeAt2(p, s.code, r);
          if (oc.isNothing())
            throw new RuntimeException("invalid path");
          if (!isPrefix(p, s.pathShadow)) {
            s.pathShadow = p;
            f(s.pathShadow);
          }
          s.path = p;
          s.initNodeEditor.f(unit());
          return unit();
        }, icode(s.code, r), $path));
        return unit();
      }
    };

    final F<Pair<Code2, Optional<List<Label>>>, Unit> codeEdited =
        a -> {
          Pair<Map<З2Bytes, Code2>, Code2> p =
              replaceCodeAt2(s.code, s.path, Either3.x(a.x), r);
          for (Pair<З2Bytes, Code2> e : iter(p.x.entrySet()))
            s.newCodes = s.newCodes.put(e.x, e.y);
          Code2 code2 = p.y;
          remapAliases(s.code, code2, nil(), a.y, codeLabelAliases, s.code);
          s.pathShadow = longestValidSubPath2(s.pathShadow, icode(code2, r));
          s.code = code2;
          s.initNodeEditor.f(unit());
          return unit();
        };

    s.initNodeEditor = new F<Unit, Unit>() {
      public Unit f(Unit _) {
        s.nodeEditor =
            CodeNodeEditor2.make(context, s.code,
                new CodeNodeEditor2.Listener() {
                  public void switchCodeOp() {
                    Code2 c = codeAt2(s.path, s.code, r).some().x;
                    switch (c.tag) {
                    case PRODUCT:
                      c = Code2.newUnion(c.labels);
                      break;
                    case UNION:
                      c = Code2.newProduct(c.labels);
                      break;
                    default:
                      throw boom();
                    }
                    codeEdited.f(pair(c, nothing()));
                  }

                  public void done() {
                    done.f(pair(s.code, s.newCodes));
                  }

                  public void newField() {
                    Code2 c = codeAt2(s.path, s.code, r).some().x;
                    Map<Label, Either3<Code2, List<Label>, Link>> m = c.labels;
                    Label l = null;
                    do {
                      if (l != null)
                        Log.e(CodeEditor2.class.getName(),
                            "generated duplicate label");
                      l = new Label(Random.randomId());
                    } while (containsKey(m, l));
                    m = m.put(l, Either3.x(unit2));
                    c = new Code2(c.tag, m);
                    codeEdited.f(pair(c, nothing()));
                  }

                  public void changeLabelAlias(Label label, String alias) {
                    notNull(label, alias);
                    Code2 c = codeAt2(s.path, s.code, r).some().x;
                    if (!containsKey(c.labels, label))
                      throw new RuntimeException("non-existent label");
                    Link l = new Link(hash(s.code), s.path);
                    if (codeLabelAliases.setAlias(l, label, alias))
                      f(unit());
                  }

                  public void replaceField(
                      Either3<Code2, List<Label>, Link> cpl, Label l) {
                    Code2 c = codeAt2(s.path, s.code, r).some().x;
                    notNull(cpl, l);
                    if (cpl.tag == cpl.tag.Y)
                      if (codeAt2(cpl.y(), s.code, r).isNothing())
                        throw new RuntimeException("invalid path");
                    c = new Code2(c.tag, c.labels.put(l, cpl));
                    codeEdited.f(pair(c, some(append(l, s.path))));
                  }

                  public void deleteField(Label l) {
                    Code2 c = codeAt2(s.path, s.code, r).some().x;
                    Code2 c2 = new Code2(c.tag, c.labels.delete(l));
                    if (canReplace(s.code, s.path, Either3.x(c2), r))
                      codeEdited.f(pair(c2, some(append(l, s.path))));
                  }

                  public void selectCode(Label l) {
                    notNull(l);
                    Code2 c = codeAt2(s.path, s.code, r).some().x;
                    Either3<Code2, List<Label>, Link> cpl =
                        c.labels.get(l).some().x;
                    switch (cpl.tag) {
                    case Z:
                      s.path = append(l, s.path);
                      break;
                    case Y:
                      s.path = cpl.y();
                      break;
                    case X:
                      s.path = append(l, s.path);
                      break;
                    }
                    if (!isPrefix(s.path, s.pathShadow)) {
                      s.pathShadow = s.path;
                      setPath.f(s.pathShadow);
                    }
                    f(unit());
                  }
                }, codeAliases, codes, s.path, codeLabelAliases, r);
        ViewGroup cont = (ViewGroup) v.findViewById(R.id.container_code_editor);
        cont.removeAllViews();
        cont.addView(s.nodeEditor);
        return unit();
      }
    };

    F<Unit, Bundle> getState = $ -> {
      Bundle b = new Bundle();
      b.putSerializable(STATE_CODE, s.code);
      b.putSerializable(STATE_PATH, s.path);
      b.putSerializable(STATE_PATH_SHADOW, s.pathShadow);
      return b;
    };

    s.initNodeEditor.f(unit());
    setPath.f(s.pathShadow);
    return pair(v, getState);
  }

  public static Pair<View, F<Unit, Bundle>> make(Context context, Code2 code,
      CodeLabelAliasMap2 codeLabelAliases, Bijection<Link, String> codeAliases,
      List<Code2> codes, Resolver r,
      F<Pair<Code2, Map<З2Bytes, Code2>>, Unit> done) {
    return make(context, code, codeLabelAliases, codeAliases, codes, nil(),
        nil(), r, done);
  }

  public static Pair<View, F<Unit, Bundle>> make(Context context, Bundle b,
      CodeLabelAliasMap2 codeLabelAliases, Bijection<Link, String> codeAliases,
      List<Code2> codes, Resolver r,
      F<Pair<Code2, Map<З2Bytes, Code2>>, Unit> done) {
    return make(context, (Code2) b.get(STATE_CODE), codeLabelAliases,
        codeAliases, codes, (List<Label>) b.get(STATE_PATH),
        (List<Label>) b.get(STATE_PATH_SHADOW), r, done);
  }

  private static void remapAliases(Code2 c, Code2 c2, List<Label> path,
      Optional<List<Label>> invalidatedPath,
      CodeLabelAliasMap2 codeLabelAliases, Code2 code) {
    if (some(path).equals(invalidatedPath))
      return;
    codeLabelAliases.setAliases(new Link(hash(c2), path),
        codeLabelAliases.getAliases(new Link(hash(code), path)));
    for (Pair<Label, Either3<Code2, List<Label>, Link>> e : iter(c.labels
        .entrySet()))
      if (e.y.tag == e.y.tag.X)
        remapAliases(e.y.x(), c2, append(e.x, path), invalidatedPath,
            codeLabelAliases, code);
  }
}
