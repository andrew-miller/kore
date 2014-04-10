package com.example.kore.ui;

import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.CodeUtils.codeAt;
import static com.example.kore.utils.CodeUtils.replaceCodeAt;
import static com.example.kore.utils.CodeUtils.unit;
import static com.example.kore.utils.CodeUtils.validCode;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.isSubList;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.MapUtils.containsKey;
import static com.example.kore.utils.Null.notNull;
import static com.example.kore.utils.Unit.unit;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.Either;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Map;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Random;
import com.example.kore.utils.Unit;

public class CodeEditor extends FrameLayout {
  private static final String STATE_CODE = "code";
  private static final String STATE_PATH = "path";
  private static final String STATE_PATH_SHADOW = "path_shadow";

  private CodeNodeEditor nodeEditor;
  private Code code = CodeUtils.unit;
  private List<Label> path;
  private final CodeLabelAliasMap codeLabelAliases;
  private final Map<CanonicalCode, String> codeAliases;
  private List<Label> pathShadow;
  private final List<Code> codes;
  private final Context context;
  private final ViewGroup pathContainer;
  private final F<Code, Unit> done;

  public CodeEditor(Context context, Code code,
      CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, List<Code> codes,
      List<Label> path, List<Label> pathShadow, F<Code, Unit> done) {
    super(context);
    notNull(code, codeLabelAliases, codeAliases, codes, done);
    this.context = context;
    this.code = code;
    this.codeLabelAliases = codeLabelAliases;
    this.codeAliases = codeAliases;
    this.codes = codes;
    this.done = done;
    this.path = path;
    this.pathShadow = pathShadow;
    LayoutInflater.from(context).inflate(R.layout.code_editor, this, true);
    pathContainer = (ViewGroup) findViewById(R.id.container_path);

    initNodeEditor(CodeUtils.codeAt(path, code).some().x);
    setPath(pathShadow);
  }

  public CodeEditor(Context context, Code code,
      CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, List<Code> codes,
      F<Code, Unit> done) {
    this(context, code, codeLabelAliases, codeAliases, codes, ListUtils
        .<Label> nil(), ListUtils.<Label> nil(), done);
  }

  public CodeEditor(Context context, Bundle b,
      CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, List<Code> codes,
      F<Code, Unit> done) {
    this(context, (Code) b.get(STATE_CODE), codeLabelAliases, codeAliases,
        codes, (List<Label>) b.get(STATE_PATH), (List<Label>) b
            .get(STATE_PATH_SHADOW), done);
  }

  public Bundle getState() {
    Bundle b = new Bundle();
    b.putSerializable(STATE_CODE, code);
    b.putSerializable(STATE_PATH, path);
    b.putSerializable(STATE_PATH_SHADOW, pathShadow);
    return b;
  }

  private void setPath(List<Label> path) {
    pathContainer.removeAllViews();
    pathContainer.addView(new CodePath(context, new F<List<Label>, Unit>() {
      public Unit f(List<Label> p) {
        notNull(p);
        Optional<Code> oc = CodeUtils.codeAt(p, code);
        if (oc.isNothing())
          throw new RuntimeException("invalid path");
        if (!isSubList(p, pathShadow)) {
          pathShadow = p;
          setPath(pathShadow);
        }
        CodeEditor.this.path = p;
        initNodeEditor(oc.some().x);
        return unit();
      }
    }, code, path));
  }

  private void codeEdited(Code c, List<Label> invalidatedPath) {
    Code code2 = replaceCodeAt(code, path, Either.<Code, List<Label>> x(c));
    remapAliases(code, code2, ListUtils.<Label> nil(), invalidatedPath);
    pathShadow = CodeUtils.longestValidSubPath(pathShadow, code2);
    Pair<Code, Map<List<Label>, Map<Label, Label>>> p =
        CodeUtils.disassociate(code2, path);
    mapAliases(code2, ListUtils.<Label> nil(), p.x, ListUtils.<Label> nil(),
        code2, p.y);
    code = p.x;
    path = CodeUtils.mapPath(path, p.y);
    pathShadow = CodeUtils.mapPath(pathShadow, p.y);
    setPath(pathShadow);
    initNodeEditor(CodeUtils.codeAt(path, code).some().x);
  }

  private void mapAliases(Code cRoot, List<Label> cPath, Code c2root,
      List<Label> c2Path, Code cNode, Map<List<Label>, Map<Label, Label>> i) {
    CanonicalCode cc = new CanonicalCode(cRoot, cPath);
    CanonicalCode cc2 = new CanonicalCode(c2root, c2Path);
    Map<Label, String> la = codeLabelAliases.getAliases(cc);
    Map<Label, String> la2 = Map.empty();
    Map<Label, Label> m = i.get(cPath).some().x;
    for (Pair<Label, Either<Code, List<Label>>> e : iter(cNode.labels
        .entrySet())) {
      Label l = e.x;
      Label l2 = m.get(l).some().x;
      Optional<String> oa = la.get(l);
      if (!oa.isNothing())
        la2 = la2.put(l2, oa.some().x);
      if (e.y.tag == e.y.tag.X)
        mapAliases(cRoot, append(e.x, cPath), c2root, append(l2, c2Path),
            e.y.x(), i);
    }
    codeLabelAliases.setAliases(cc2, la2);
  }

  private void remapAliases(Code c, Code c2, List<Label> path,
      List<Label> invalidatedPath) {
    if (path.equals(invalidatedPath))
      return;
    codeLabelAliases.setAliases(new CanonicalCode(c2, path),
        codeLabelAliases.getAliases(new CanonicalCode(code, path)));
    for (Pair<Label, Either<Code, List<Label>>> e : iter(c.labels.entrySet()))
      if (e.y.tag == e.y.tag.X)
        remapAliases(e.y.x(), c2, append(e.x, path), invalidatedPath);
  }

  private void initNodeEditor(Code c) {
    nodeEditor =
        new CodeNodeEditor(context, c, code, new CodeNodeEditor.Listener() {
          public void switchCodeOp() {
            Code c = codeAt(path, code).some().x;
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
            codeEdited(c, null);
          }

          public void done() {
            done.f(code);
          }

          public void newField() {
            Code c = CodeUtils.codeAt(path, code).some().x;
            Map<Label, Either<Code, List<Label>>> m = c.labels;
            Label l = null;
            do {
              if (l != null)
                Log.e(CodeEditor.class.getName(), "generated duplicate label");
              l = new Label(Random.randomId());
            } while (containsKey(m, l));
            m = m.put(l, Either.<Code, List<Label>> x(unit));
            c = new Code(c.tag, m);
            codeEdited(c, null);
          }

          public void changeLabelAlias(Label label, String alias) {
            notNull(label, alias);
            Code c = CodeUtils.codeAt(path, code).some().x;
            if (!containsKey(c.labels, label))
              throw new RuntimeException("non-existent label");
            CanonicalCode cc = new CanonicalCode(code, path);
            codeLabelAliases.setAlias(cc, label, alias);
            initNodeEditor(c);
          }

          public void replaceField(Either<Code, List<Label>> cp, Label l) {
            Code c = CodeUtils.codeAt(path, code).some().x;
            notNull(cp, l);
            if (cp.tag == cp.tag.Y)
              if (codeAt(cp.y(), code).isNothing())
                throw new RuntimeException("invalid path");
            c = new Code(c.tag, c.labels.put(l, cp));
            codeEdited(c, append(l, path));
          }

          public void deleteField(Label l) {
            Code c = CodeUtils.codeAt(path, code).some().x;
            Code c2 = new Code(c.tag, c.labels.delete(l));
            if (validCode(replaceCodeAt(code, path,
                Either.<Code, List<Label>> x(c2))))
              codeEdited(c2, append(l, path));
          }

          public void selectCode(Label l) {
            notNull(l);
            Code c = CodeUtils.codeAt(path, code).some().x;
            Either<Code, List<Label>> cp = c.labels.get(l).some().x;
            Code c2;
            switch (cp.tag) {
            case Y:
              path = cp.y();
              c2 = CodeUtils.codeAt(path, code).some().x;
              break;
            case X:
              path = append(l, path);
              c2 = cp.x();
              break;
            default:
              throw boom();
            }
            if (!isSubList(path, pathShadow)) {
              pathShadow = path;
              setPath(pathShadow);
            }
            initNodeEditor(c2);
          }
        }, codeAliases, codes, path, codeLabelAliases);
    ViewGroup cont = (ViewGroup) findViewById(R.id.container_code_editor);
    cont.removeAllViews();
    cont.addView(nodeEditor);
  }

}
