package com.example.kore.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.ListUtils;
import com.example.unsuck.Null;

public class CodeEditorActivity extends FragmentActivity implements
    Field.CodeSelectedListener, CodeEditor.CodeEditedListener,
    Path.SubpathSelectedListener, Field.LabelSelectedListener,
    Field.FieldChangedListener, Field.LabelAliasChangedListener,
    CodeEditor.DoneListener {

  private static final String STATE_CODE = "code";
  private static final String STATE_PATH = "path";
  private static final String STATE_LABEL_ALIASES = "label_aliases";
  private static final String STATE_CODE_ALIASES = "code_aliases";
  private static final String STATE_PATH_SHADOW = "path_shadow";

  public static final String RESULT_CODE = "code";
  public static final String RESULT_LABEL_ALIASES = "label_aliases";

  public static final String ARG_CODE = "code";
  public static final String ARG_LABEL_ALIASES = "label_aliases";
  public static final String ARG_CODE_ALIASES = "code_aliases";

  private CodeEditor codeEditor;
  private Code code = CodeUtils.unit;
  private LinkedList<Label> path = new LinkedList<Label>();
  private Path pathFragment;
  private HashMap<Label, String> labelAliases;
  private HashMap<Code, String> codeAliases;
  private LinkedList<Label> pathShadow = new LinkedList<Label>();

  @SuppressWarnings("unchecked")
  @Override
  protected void onCreate(Bundle b) {
    super.onCreate(b);
    setContentView(R.layout.activity_code_editor);

    pathFragment =
        (Path) getSupportFragmentManager().findFragmentById(R.id.fragment_path);

    code = (Code) getIntent().getSerializableExtra(ARG_CODE);
    labelAliases =
        new HashMap<Label, String>((HashMap<Label, String>) getIntent()
            .getSerializableExtra(ARG_LABEL_ALIASES));
    codeAliases =
        new HashMap<Code, String>((HashMap<Code, String>) getIntent()
            .getSerializableExtra(ARG_CODE_ALIASES));

    if (b != null) {
      code = (Code) b.get(STATE_CODE);
      path = (LinkedList<Label>) b.get(STATE_PATH);
      labelAliases = (HashMap<Label, String>) b.get(STATE_LABEL_ALIASES);
      codeAliases = (HashMap<Code, String>) b.get(STATE_LABEL_ALIASES);
      pathShadow = (LinkedList<Label>) b.get(STATE_PATH_SHADOW);
    }

    initCodeEditor(CodeUtils.followPath(path, code));
    pathFragment.setPath(code, pathShadow);

  }

  @Override
  public void onSaveInstanceState(Bundle b) {
    super.onSaveInstanceState(b);
    b.putSerializable(STATE_CODE, code);
    b.putSerializable(STATE_PATH, path);
    b.putSerializable(STATE_LABEL_ALIASES, labelAliases);
    b.putSerializable(STATE_CODE_ALIASES, codeAliases);
    b.putSerializable(STATE_PATH_SHADOW, pathShadow);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  @Override
  public void onCodeEdited(Code c) {
    code = CodeUtils.replaceCodeAt(code, path, c);
    pathShadow = CodeUtils.longestValidSubPath(pathShadow, code);
    pathFragment.setPath(code, pathShadow);
    initCodeEditor(c);
  }

  @Override
  public void codeSelected(Label l) {
    Null.notNull(l);
    Code c = CodeUtils.followPath(path, code);
    CodeOrPath cr = c.labels.get(l);
    if (cr == null)
      throw new RuntimeException("non-existent label");
    if (cr.tag != CodeOrPath.Tag.CODE)
      throw new RuntimeException("you can't go there");
    path.add(l);
    if (!ListUtils.isSubList(path, pathShadow)) {
      pathShadow = new LinkedList<Label>(path);
      pathFragment.setPath(code, pathShadow);
    }
    initCodeEditor(cr.code);
  }

  @Override
  public void onCodeInPathSelected(List<Label> p) {
    Code c = CodeUtils.followPath(p, code);
    if (c == null)
      throw new RuntimeException("invalid path");
    if (!ListUtils.isSubList(p, pathShadow)) {
      pathShadow = new LinkedList<Label>(p);
      pathFragment.setPath(code, pathShadow);
    }
    path = new LinkedList<Label>(p);
    initCodeEditor(c);
  }

  @Override
  public void labelSelected(Label l) {
    codeEditor.labelSelected(l);
  }

  @Override
  public void fieldChanged(List<Label> path, Label label) {
    codeEditor.fieldChanged(path, label);
  }

  @Override
  public void labelAliasChanged(Label label, String alias) {
    Null.notNull(label, alias);
    Code c = CodeUtils.followPath(path, code);
    if (!c.labels.containsKey(label))
      throw new RuntimeException("non-existent label");
    labelAliases.put(label, alias);
    initCodeEditor(c);
  }

  private void initCodeEditor(Code c) {
    codeEditor = new CodeEditor();
    Bundle b = new Bundle();
    b.putSerializable(CodeEditor.ARG_CODE, c);
    b.putSerializable(CodeEditor.ARG_ROOT_CODE, code);
    b.putSerializable(CodeEditor.ARG_LABEL_ALIASES, new HashMap<Label, String>(
        labelAliases));
    b.putSerializable(CodeEditor.ARG_CODE_ALIASES, new HashMap<Code, String>(
        codeAliases));
    codeEditor.setArguments(b);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_code_editor, codeEditor).commit();
  }

  @Override
  public void onDone(Code c) {
    setResult(
        0,
        new Intent().putExtra(RESULT_CODE, code).putExtra(RESULT_LABEL_ALIASES,
            new HashMap<Label, String>(labelAliases)));
    finish();
  }
}
