package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.isSubList;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ViewGroup;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.CodeOrPath.Tag;
import com.example.kore.codes.Label;
import com.example.kore.utils.Boom;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.List;
import com.example.kore.utils.MapUtils;
import com.example.kore.utils.Optional;
import com.example.kore.utils.Random;

public class CodeEditorActivity extends FragmentActivity implements
    NodeEditor.NodeEditorListener, Path.SubpathSelectedListener,
    NodeEditor.DoneListener {

  private static final String STATE_CODE = "code";
  private static final String STATE_PATH = "path";
  private static final String STATE_CODE_LABEL_ALIASES = "code_label_aliases";
  private static final String STATE_CODE_ALIASES = "code_aliases";
  private static final String STATE_PATH_SHADOW = "path_shadow";
  private static final String STATE_CODES = "codes";

  public static final String RESULT_CODE = "code";
  public static final String RESULT_LABEL_ALIASES = "label_aliases";

  public static final String ARG_CODE = "code";
  public static final String ARG_CODE_LABEL_ALIASES = "code_label_aliases";
  public static final String ARG_CODE_ALIASES = "code_aliases";
  public static final String ARG_CODES = "codes";

  private NodeEditor codeEditor;
  private Code code = CodeUtils.unit;
  private List<Label> path = nil(Label.class);
  private Path pathFragment;
  private HashMap<CanonicalCode, HashMap<Label, String>> codeLabelAliases;
  private HashMap<CanonicalCode, String> codeAliases;
  private List<Label> pathShadow = nil(Label.class);
  private List<Code> codes;

  @Override
  protected void onCreate(Bundle b) {
    super.onCreate(b);
    setContentView(R.layout.activity_code_editor);

    pathFragment =
        (Path) getSupportFragmentManager().findFragmentById(R.id.fragment_path);

    code = (Code) getIntent().getSerializableExtra(ARG_CODE);
    codeLabelAliases =
        MapUtils
            .cloneNestedMap((HashMap<CanonicalCode, HashMap<Label, String>>) getIntent()
                .getSerializableExtra(ARG_CODE_LABEL_ALIASES));
    codeAliases =
        new HashMap<CanonicalCode, String>(
            (HashMap<CanonicalCode, String>) getIntent().getSerializableExtra(
                ARG_CODE_ALIASES));
    codes = (List<Code>) getIntent().getSerializableExtra(ARG_CODES);
    codes.checkType(Code.class);
    notNull(code, codeAliases);

    if (b != null) {
      code = (Code) b.get(STATE_CODE);
      path = (List<Label>) b.get(STATE_PATH);
      codeLabelAliases =
          (HashMap<CanonicalCode, HashMap<Label, String>>) b
              .get(STATE_CODE_LABEL_ALIASES);
      codeAliases = (HashMap<CanonicalCode, String>) b.get(STATE_CODE_ALIASES);
      pathShadow = (List<Label>) b.get(STATE_PATH_SHADOW);
      codes = (List<Code>) b.get(STATE_CODES);
    }

    initCodeEditor(CodeUtils.codeAt(path, code).some().x);
    pathFragment.setPath(code, pathShadow);

  }

  @Override
  public void onSaveInstanceState(Bundle b) {
    super.onSaveInstanceState(b);
    b.putSerializable(STATE_CODE, code);
    b.putSerializable(STATE_PATH, path);
    b.putSerializable(STATE_CODE_LABEL_ALIASES, codeLabelAliases);
    b.putSerializable(STATE_CODE_ALIASES, codeAliases);
    b.putSerializable(STATE_PATH_SHADOW, pathShadow);
    b.putSerializable(STATE_CODES, codes);
  }

  private void onCodeEdited(Code c, List<Label> invalidatedPath) {
    Code code2 = CodeUtils.replaceCodeAt(code, path, CodeOrPath.newCode(c));
    remapAliases(code, code2, nil(Label.class), invalidatedPath);
    pathShadow = CodeUtils.longestValidSubPath(pathShadow, code2);
    pathFragment.setPath(code2, pathShadow);
    code = code2;
    initCodeEditor(c);
  }

  @Override
  public void newField() {
    Code c = CodeUtils.codeAt(path, code).some().x;
    Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>(c.labels);
    Label l = null;
    do {
      if (l != null)
        Log.e(CodeEditorActivity.class.getName(), "generated duplicate label");
      l = new Label(Random.randomId());
    } while (m.containsKey(l));
    m.put(l, CodeOrPath.newCode(CodeUtils.unit));
    c = new Code(c.tag, m);
    onCodeEdited(c, null);
  }

  @Override
  public void deleteField(Label l) {
    Code c = CodeUtils.codeAt(path, code).some().x;
    Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>(c.labels);
    m.remove(l);
    Code c2 = new Code(c.tag, m);
    if (CodeUtils.validCode(c2))
      onCodeEdited(c2, append(l, path));
  }

  @Override
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

  private void remapAliases(Code c, Code c2, List<Label> path,
      List<Label> invalidatedPath) {
    if (path.equals(invalidatedPath))
      return;
    HashMap<Label, String> la =
        codeLabelAliases.get(new CanonicalCode(code, path));
    if (la != null)
      codeLabelAliases.put(new CanonicalCode(c2, path),
          new HashMap<Label, String>(la));
    for (Entry<Label, CodeOrPath> e : c.labels.entrySet())
      if (e.getValue().tag == Tag.CODE)
        remapAliases(e.getValue().code, c2, append(e.getKey(), path),
            invalidatedPath);
  }

  @Override
  public void codeSelected(Label l) {
    notNull(l);
    Code c = CodeUtils.codeAt(path, code).some().x;
    CodeOrPath cr = c.labels.get(l);
    if (cr == null)
      throw new RuntimeException("non-existent label");
    if (cr.tag != CodeOrPath.Tag.CODE)
      throw new RuntimeException("you can't go there");
    path = append(l, path);
    if (!isSubList(path, pathShadow)) {
      pathShadow = path;
      pathFragment.setPath(code, pathShadow);
    }
    initCodeEditor(cr.code);
  }

  @Override
  public void onCodeInPathSelected(List<Label> p) {
    p.checkType(Label.class);
    Optional<Code> oc = CodeUtils.codeAt(p, code);
    if (oc.isNothing())
      throw new RuntimeException("invalid path");
    if (!isSubList(p, pathShadow)) {
      pathShadow = p;
      pathFragment.setPath(code, pathShadow);
    }
    path = p;
    initCodeEditor(oc.some().x);
  }

  @Override
  public void fieldReplaced(CodeOrPath cp, Label l) {
    Code c = CodeUtils.codeAt(path, code).some().x;
    notNull(cp, l);
    Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>(c.labels);
    if (cp.tag == Tag.PATH)
      if (CodeUtils.codeAt(cp.path, code).isNothing())
        throw new RuntimeException("invalid path");
    m.put(l, cp);
    c = new Code(c.tag, m);
    onCodeEdited(c, append(l, path));
  }

  @Override
  public void labelAliasChanged(Label label, String alias) {
    notNull(label, alias);
    Code c = CodeUtils.codeAt(path, code).some().x;
    if (!c.labels.containsKey(label))
      throw new RuntimeException("non-existent label");
    CanonicalCode cc = new CanonicalCode(code, path);
    HashMap<Label, String> labelAliases = codeLabelAliases.get(cc);
    if (labelAliases == null) {
      labelAliases = new HashMap<Label, String>();
      codeLabelAliases.put(cc, labelAliases);
    }
    labelAliases.put(label, alias);
    initCodeEditor(c);
  }

  private void initCodeEditor(Code c) {
    codeEditor =
        new NodeEditor(this, c, code, this, this,
            new HashMap<CanonicalCode, String>(codeAliases), codes, path,
            MapUtils.cloneNestedMap(codeLabelAliases));
    ViewGroup cont = (ViewGroup) findViewById(R.id.container_code_editor);
    cont.removeAllViews();
    cont.addView(codeEditor);
  }

  @Override
  public void onDone() {
    setResult(
        0,
        new Intent().putExtra(RESULT_CODE, code).putExtra(RESULT_LABEL_ALIASES,
            codeLabelAliases));
    finish();
  }
}
