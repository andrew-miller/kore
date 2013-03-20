package com.example.kore.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeRef;
import com.example.kore.codes.Label;
import com.example.kore.codes.LabelOrd;
import com.example.kore.utils.CodeUtils;
import com.example.unsuck.Null;

import fj.data.List;
import fj.data.TreeMap;

public class CodeEditorActivity extends FragmentActivity implements
    Field.CodeSelectedListener, CodeEditor.CodeEditedListener,
    Path.SubpathSelectedListener, Field.LabelSelectedListener,
    Field.FieldChangedListener, Field.LabelAliasChangedListener,
    CodeEditor.DoneListener {

  private static final String STATE_CODE = "code";
  private static final String STATE_PATH = "path";
  private static final String STATE_LABEL_ALIASES = "label_aliases";

  public static final String RESULT_CODE = "code";
  public static final String RESULT_LABEL_ALIASES = "label_aliases";

  public static final String ARG_CODE = "code";
  public static final String ARG_LABEL_ALIASES = "label_aliases";

  private CodeEditor codeEditor;
  private Code code = CodeUtils.unit;
  private List<Label> path = List.nil();
  private Path pathFragment;
  private TreeMap<Label, String> labelAliases = TreeMap
      .<Label, String> empty(LabelOrd.ord());

  @Override
  protected void onCreate(Bundle b) {
    super.onCreate(b);
    setContentView(R.layout.activity_code_editor);

    pathFragment =
        (Path) getSupportFragmentManager().findFragmentById(R.id.fragment_path);

    code = (Code) getIntent().getSerializableExtra(ARG_CODE);
    labelAliases =
        (TreeMap<Label, String>) getIntent().getSerializableExtra(
            ARG_LABEL_ALIASES);

    if (b != null) {
      code = (Code) b.get(STATE_CODE);
      path = (List<Label>) b.get(STATE_PATH);
      labelAliases = (TreeMap<Label, String>) b.get(STATE_LABEL_ALIASES);
    }

    initCodeEditor(CodeUtils.followPath(path, code));
    pathFragment.setPath(code, path);

  }

  @Override
  public void onSaveInstanceState(Bundle b) {
    super.onSaveInstanceState(b);
    b.putSerializable(STATE_CODE, code);
    b.putSerializable(STATE_PATH, path);
    b.putSerializable(STATE_LABEL_ALIASES, labelAliases);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  @Override
  public void onCodeEdited(Code c) {
    code = replaceCurrentCode(code, path, c);
    pathFragment.setPath(code, path);
    initCodeEditor(c);
  }

  private Code replaceCurrentCode(Code c, List<Label> p, Code newCode) {
    if (p.isEmpty())
      return newCode;
    Label l = p.head();
    return new Code(c.tag, c.labels.set(
        l,
        CodeRef.newCode(replaceCurrentCode(c.labels.get(l).some().code,
            p.tail(), newCode))));
  }

  @Override
  public void codeSelected(Label l) {
    Null.notNull(l);
    Code c = CodeUtils.followPath(path, code);
    CodeRef cr = c.labels.get(l).some();
    if (cr.tag != CodeRef.Tag.CODE)
      throw new RuntimeException("you can't go there");
    path = path.append(List.single(l));
    pathFragment.setPath(code, path);
    initCodeEditor(cr.code);
  }

  @Override
  public void onCodeInPathSelected(List<Label> p) {
    Null.notNull(p);
    Code c = CodeUtils.followPath(p, code);
    if (c == null)
      throw new RuntimeException("invalid path");
    path = p;
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
    if (!c.labels.contains(label))
      throw new RuntimeException("non-existent label");
    labelAliases = labelAliases.set(label, alias);
    initCodeEditor(c);
  }

  private void initCodeEditor(Code c) {
    codeEditor = new CodeEditor();
    Bundle b = new Bundle();
    b.putSerializable(CodeEditor.ARG_CODE, c);
    b.putSerializable(CodeEditor.ARG_ROOT_CODE, code);
    b.putSerializable(CodeEditor.ARG_LABEL_ALIASES, labelAliases);
    codeEditor.setArguments(b);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_code_editor, codeEditor).commit();
  }

  @Override
  public void onDone(Code c) {
    setResult(
        0,
        new Intent().putExtra(RESULT_CODE, code).putExtra(RESULT_LABEL_ALIASES,
            labelAliases));
    finish();
  }
}
