package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.isSubList;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.MapUtils.containsKey;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.CodeOrPath.Tag;
import com.example.kore.codes.Label;
import com.example.kore.utils.Boom;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Pair;
import com.example.kore.utils.Random;

public class CodeEditor extends FrameLayout {

  private static final String STATE_CODE = "code";
  private static final String STATE_PATH = "path";
  private static final String STATE_CODE_ALIASES = "code_aliases";
  private static final String STATE_PATH_SHADOW = "path_shadow";
  private static final String STATE_CODES = "codes";

  private CodeNodeEditor nodeEditor;
  private Code code = CodeUtils.unit;
  private List<Label> path = nil();
  private final CodeLabelAliasMap codeLabelAliases;
  private final Map<CanonicalCode, String> codeAliases;
  private List<Label> pathShadow = nil();
  private final List<Code> codes;
  private final Context context;
  private final ViewGroup pathContainer;
  private final DoneListener doneListener;

  public interface DoneListener {
    public void onDone(Code c);
  }

  public CodeEditor(Context context, Code code,
      CodeLabelAliasMap codeLabelAliases,
      Map<CanonicalCode, String> codeAliases, List<Code> codes,
      DoneListener doneListener) {
    super(context);
    notNull(code, codeLabelAliases, codeAliases, codes, doneListener);
    this.context = context;
    this.code = code;
    this.codeLabelAliases = codeLabelAliases;
    this.codeAliases = codeAliases;
    this.codes = codes;
    this.doneListener = doneListener;
    LayoutInflater.from(context).inflate(R.layout.code_editor, this, true);
    pathContainer = (ViewGroup) findViewById(R.id.container_path);

    initNodeEditor(CodeUtils.codeAt(path, code).some().x);
    setPath(pathShadow);
  }

  public CodeEditor(Context context, Bundle b,
      CodeLabelAliasMap codeLabelAliases, DoneListener doneListener) {
    super(context);
    notNull(doneListener, b);
    this.context = context;
    code = (Code) b.get(STATE_CODE);
    path = (List<Label>) b.get(STATE_PATH);
    this.codeLabelAliases = codeLabelAliases;
    codeAliases = (Map<CanonicalCode, String>) b.get(STATE_CODE_ALIASES);
    pathShadow = (List<Label>) b.get(STATE_PATH_SHADOW);
    codes = (List<Code>) b.get(STATE_CODES);
    this.doneListener = doneListener;
    View v =
        LayoutInflater.from(context).inflate(R.layout.code_editor, this, true);
    pathContainer = (ViewGroup) findViewById(R.id.container_path);

    initNodeEditor(CodeUtils.codeAt(path, code).some().x);
    setPath(pathShadow);
  }

  public Bundle getState() {
    Bundle b = new Bundle();
    b.putSerializable(STATE_CODE, code);
    b.putSerializable(STATE_PATH, path);
    b.putSerializable(STATE_CODE_ALIASES, codeAliases);
    b.putSerializable(STATE_PATH_SHADOW, pathShadow);
    b.putSerializable(STATE_CODES, codes);
    return b;
  }

  private void setPath(List<Label> path) {
    pathContainer.removeAllViews();
    pathContainer.addView(new CodePath(context,
        new CodePath.SubpathSelectedListener() {
          public void pathSelected(List<Label> p) {
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
          }
        }, code, path));
  }

  private void onCodeEdited(Code c, List<Label> invalidatedPath) {
    Code code2 = CodeUtils.replaceCodeAt(code, path, CodeOrPath.newCode(c));
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
    for (Entry<Label, CodeOrPath> e : iter(cNode.labels.entrySet())) {
      Label l = e.k;
      Label l2 = m.get(l).some().x;
      Optional<String> oa = la.get(l);
      if (!oa.isNothing())
        la2 = la2.put(l2, oa.some().x);
      if (e.v.tag == CodeOrPath.Tag.CODE)
        mapAliases(cRoot, append(e.k, cPath), c2root, append(l2, c2Path),
            e.v.code, i);
    }
    codeLabelAliases.setAliases(cc2, la2);
  }

  private void remapAliases(Code c, Code c2, List<Label> path,
      List<Label> invalidatedPath) {
    if (path.equals(invalidatedPath))
      return;
    codeLabelAliases.setAliases(new CanonicalCode(c2, path),
        codeLabelAliases.getAliases(new CanonicalCode(code, path)));
    for (Entry<Label, CodeOrPath> e : iter(c.labels.entrySet()))
      if (e.v.tag == Tag.CODE)
        remapAliases(e.v.code, c2, append(e.k, path), invalidatedPath);
  }

  private void initNodeEditor(Code c) {
    nodeEditor =
        new CodeNodeEditor(context, c, code, new CodeNodeEditor.Listener() {
          public void switchCodeOp() {
            Code c = CodeUtils.codeAt(path, code).some().x;
            switch (c.tag) {
            case PRODUCT:
              c = Code.newUnion(c.labels);
              break;
            case UNION:
              c = Code.newProduct(c.labels);
              break;
            default:
              throw Boom.boom();
            }
            onCodeEdited(c, null);
          }

          public void onDone() {
            doneListener.onDone(code);
          }

          public void newField() {
            Code c = CodeUtils.codeAt(path, code).some().x;
            Map<Label, CodeOrPath> m = c.labels;
            Label l = null;
            do {
              if (l != null)
                Log.e(CodeEditor.class.getName(), "generated duplicate label");
              l = new Label(Random.randomId());
            } while (containsKey(m, l));
            m = m.put(l, CodeOrPath.newCode(CodeUtils.unit));
            c = new Code(c.tag, m);
            onCodeEdited(c, null);
          }

          public void labelAliasChanged(Label label, String alias) {
            notNull(label, alias);
            Code c = CodeUtils.codeAt(path, code).some().x;
            if (!containsKey(c.labels, label))
              throw new RuntimeException("non-existent label");
            CanonicalCode cc = new CanonicalCode(code, path);
            codeLabelAliases.setAlias(cc, label, alias);
            initNodeEditor(c);
          }

          public void fieldReplaced(CodeOrPath cp, Label l) {
            Code c = CodeUtils.codeAt(path, code).some().x;
            notNull(cp, l);
            if (cp.tag == Tag.PATH)
              if (CodeUtils.codeAt(cp.path, code).isNothing())
                throw new RuntimeException("invalid path");
            c = new Code(c.tag, c.labels.put(l, cp));
            onCodeEdited(c, append(l, path));
          }

          public void deleteField(Label l) {
            Code c = CodeUtils.codeAt(path, code).some().x;
            Code c2 = new Code(c.tag, c.labels.delete(l));
            if (CodeUtils.validCode(CodeUtils.replaceCodeAt(code, path,
                CodeOrPath.newCode(c2))))
              onCodeEdited(c2, append(l, path));
          }

          public void codeSelected(Label l) {
            notNull(l);
            Code c = CodeUtils.codeAt(path, code).some().x;
            CodeOrPath cp = c.labels.get(l).some().x;
            Code c2;
            if (cp.tag != CodeOrPath.Tag.CODE) {
              path = cp.path;
              c2 = CodeUtils.codeAt(path, code).some().x;
            } else {
              path = append(l, path);
              c2 = cp.code;
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
