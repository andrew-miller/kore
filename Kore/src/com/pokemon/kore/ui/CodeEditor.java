package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.CodeUtils.codeAt;
import static com.pokemon.kore.utils.CodeUtils.disassociate;
import static com.pokemon.kore.utils.CodeUtils.longestValidSubPath;
import static com.pokemon.kore.utils.CodeUtils.mapPath;
import static com.pokemon.kore.utils.CodeUtils.replaceCodeAt;
import static com.pokemon.kore.utils.CodeUtils.unit;
import static com.pokemon.kore.utils.CodeUtils.validCode;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.isPrefix;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.MapUtils.containsKey;
import static com.pokemon.kore.utils.Null.notNull;
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
import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.utils.Either;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.ListUtils;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.OptionalUtils;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Random;
import com.pokemon.kore.utils.Unit;

public class CodeEditor {
  private static final String STATE_CODE = "code";
  private static final String STATE_PATH = "path";
  private static final String STATE_PATH_SHADOW = "path_shadow";

  static class S {
    View nodeEditor;
    Code code = unit;
    List<Label> path;
    List<Label> pathShadow;
    public F<Code, Unit> initNodeEditor;
  }

  private static Pair<View, F<Unit, Bundle>> make(final Context context,
      Code code, final CodeLabelAliasMap codeLabelAliases,
      final Bijection<CanonicalCode, String> codeAliases,
      final List<Code> codes, List<Label> path, List<Label> pathShadow,
      final F<Code, Unit> done) {
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

    final F<List<Label>, Unit> setPath = new F<List<Label>, Unit>() {
      public Unit f(List<Label> path) {
        pathContainer.removeAllViews();
        pathContainer.addView(CodePath.make(context,
            new F<List<Label>, Unit>() {
              public Unit f(List<Label> p) {
                notNull(p);
                Optional<Code> oc = codeAt(p, s.code);
                if (oc.isNothing())
                  throw new RuntimeException("invalid path");
                if (!isPrefix(p, s.pathShadow)) {
                  s.pathShadow = p;
                  f(s.pathShadow);
                }
                s.path = p;
                s.initNodeEditor.f(oc.some().x);
                return unit();
              }
            }, s.code, path));
        return unit();
      }
    };

    final F<Pair<Code, Optional<List<Label>>>, Unit> codeEdited =
        new F<Pair<Code, Optional<List<Label>>>, Unit>() {
          public Unit f(Pair<Code, Optional<List<Label>>> a) {
            Code code2 =
                replaceCodeAt(s.code, s.path, Either.<Code, List<Label>> x(a.x));
            remapAliases(s.code, code2, ListUtils.<Label> nil(), a.y,
                codeLabelAliases, s.code);
            s.pathShadow = longestValidSubPath(s.pathShadow, code2);
            Pair<Code, Map<List<Label>, Map<Label, Label>>> p =
                disassociate(code2, s.path);
            mapAliases(code2, ListUtils.<Label> nil(), p.x,
                ListUtils.<Label> nil(), code2, p.y, codeLabelAliases);
            s.code = p.x;
            s.path = mapPath(s.path, p.y);
            s.pathShadow = mapPath(s.pathShadow, p.y);
            setPath.f(s.pathShadow);
            s.initNodeEditor.f(codeAt(s.path, s.code).some().x);
            return unit();
          }
        };

    s.initNodeEditor = new F<Code, Unit>() {
      public Unit f(Code c) {
        s.nodeEditor =
            CodeNodeEditor.make(context, c, s.code,
                new CodeNodeEditor.Listener() {
                  public void switchCodeOp() {
                    Code c = codeAt(s.path, s.code).some().x;
                    switch (c.tag) {
                    case PRODUCT:
                      c = Code.newUnion(c.labels);
                      break;
                    case UNION:
                      c = Code.newProduct(c.labels);
                      break;
                    default:
                      throw boom();
                    }
                    codeEdited.f(pair(c, OptionalUtils.<List<Label>> nothing()));
                  }

                  public void done() {
                    done.f(s.code);
                  }

                  public void newField() {
                    Code c = codeAt(s.path, s.code).some().x;
                    Map<Label, Either<Code, List<Label>>> m = c.labels;
                    Label l = null;
                    do {
                      if (l != null)
                        Log.e(CodeEditor.class.getName(),
                            "generated duplicate label");
                      l = new Label(Random.randomId());
                    } while (containsKey(m, l));
                    m = m.put(l, Either.<Code, List<Label>> x(unit));
                    c = new Code(c.tag, m);
                    codeEdited.f(pair(c, OptionalUtils.<List<Label>> nothing()));
                  }

                  public void changeLabelAlias(Label label, String alias) {
                    notNull(label, alias);
                    Code c = codeAt(s.path, s.code).some().x;
                    if (!containsKey(c.labels, label))
                      throw new RuntimeException("non-existent label");
                    CanonicalCode cc = new CanonicalCode(s.code, s.path);
                    if (codeLabelAliases.setAlias(cc, label, alias))
                      f(c);
                  }

                  public void
                      replaceField(Either<Code, List<Label>> cp, Label l) {
                    Code c = codeAt(s.path, s.code).some().x;
                    notNull(cp, l);
                    if (cp.tag == cp.tag.Y)
                      if (codeAt(cp.y(), s.code).isNothing())
                        throw new RuntimeException("invalid path");
                    c = new Code(c.tag, c.labels.put(l, cp));
                    codeEdited.f(pair(c, some(append(l, s.path))));
                  }

                  public void deleteField(Label l) {
                    Code c = codeAt(s.path, s.code).some().x;
                    Code c2 = new Code(c.tag, c.labels.delete(l));
                    if (validCode(replaceCodeAt(s.code, s.path,
                        Either.<Code, List<Label>> x(c2))))
                      codeEdited.f(pair(c2, some(append(l, s.path))));
                  }

                  public void selectCode(Label l) {
                    notNull(l);
                    Code c = codeAt(s.path, s.code).some().x;
                    Either<Code, List<Label>> cp = c.labels.get(l).some().x;
                    Code c2;
                    switch (cp.tag) {
                    case Y:
                      s.path = cp.y();
                      c2 = codeAt(s.path, s.code).some().x;
                      break;
                    case X:
                      s.path = append(l, s.path);
                      c2 = cp.x();
                      break;
                    default:
                      throw boom();
                    }
                    if (!isPrefix(s.path, s.pathShadow)) {
                      s.pathShadow = s.path;
                      setPath.f(s.pathShadow);
                    }
                    f(c2);
                  }
                }, codeAliases, codes, s.path, codeLabelAliases);
        ViewGroup cont = (ViewGroup) v.findViewById(R.id.container_code_editor);
        cont.removeAllViews();
        cont.addView(s.nodeEditor);
        return unit();
      }
    };

    F<Unit, Bundle> getState = new F<Unit, Bundle>() {
      public Bundle f(Unit x) {
        Bundle b = new Bundle();
        b.putSerializable(STATE_CODE, s.code);
        b.putSerializable(STATE_PATH, s.path);
        b.putSerializable(STATE_PATH_SHADOW, s.pathShadow);
        return b;
      }
    };

    s.initNodeEditor.f(codeAt(path, code).some().x);
    setPath.f(s.pathShadow);
    return pair(v, getState);
  }

  public static Pair<View, F<Unit, Bundle>> make(Context context, Code code,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases, List<Code> codes,
      F<Code, Unit> done) {
    return make(context, code, codeLabelAliases, codeAliases, codes,
        ListUtils.<Label> nil(), ListUtils.<Label> nil(), done);
  }

  public static Pair<View, F<Unit, Bundle>> make(Context context, Bundle b,
      CodeLabelAliasMap codeLabelAliases,
      Bijection<CanonicalCode, String> codeAliases, List<Code> codes,
      F<Code, Unit> done) {
    return make(context, (Code) b.get(STATE_CODE), codeLabelAliases,
        codeAliases, codes, (List<Label>) b.get(STATE_PATH),
        (List<Label>) b.get(STATE_PATH_SHADOW), done);
  }

  private static void mapAliases(Code cRoot, List<Label> cPath, Code c2root,
      List<Label> c2Path, Code cNode, Map<List<Label>, Map<Label, Label>> i,
      CodeLabelAliasMap codeLabelAliases) {
    CanonicalCode cc = new CanonicalCode(cRoot, cPath);
    CanonicalCode cc2 = new CanonicalCode(c2root, c2Path);
    Bijection<Label, String> la = codeLabelAliases.getAliases(cc);
    Bijection<Label, String> la2 = Bijection.empty();
    Map<Label, Label> m = i.get(cPath).some().x;
    for (Pair<Label, Either<Code, List<Label>>> e : iter(cNode.labels
        .entrySet())) {
      Label l = e.x;
      Label l2 = m.get(l).some().x;
      Optional<String> oa = la.xy.get(l);
      if (!oa.isNothing())
        la2 = la2.putX(l2, oa.some().x).some().x;
      if (e.y.tag == e.y.tag.X)
        mapAliases(cRoot, append(e.x, cPath), c2root, append(l2, c2Path),
            e.y.x(), i, codeLabelAliases);
    }
    codeLabelAliases.setAliases(cc2, la2);
  }

  private static void remapAliases(Code c, Code c2, List<Label> path,
      Optional<List<Label>> invalidatedPath,
      CodeLabelAliasMap codeLabelAliases, Code code) {
    if (some(path).equals(invalidatedPath))
      return;
    codeLabelAliases.setAliases(new CanonicalCode(c2, path),
        codeLabelAliases.getAliases(new CanonicalCode(code, path)));
    for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels.entrySet()))
      if (e.y.tag == e.y.tag.X)
        remapAliases(e.y.x(), c2, append(e.x, path), invalidatedPath,
            codeLabelAliases, code);
  }
}
