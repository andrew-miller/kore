package com.example.kore.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeRef;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.unsuck.Null;

public class CodeEditorActivity extends FragmentActivity implements
    ActionBar.TabListener, Field.CodeSelectedListener,
    CodeEditor.CodeEditedListener, Path.SubpathSelectedListener,
    Field.LabelSelectedListener, Field.FieldChangedListener,
    Field.LabelAliasChangedListener, CodeEditor.DoneListener {

  private static final String STATE_SELECTED_NAVIGATION_ITEM =
      "selected_navigation_item";
  private static final String STATE_CODE = "code";
  private static final String STATE_PATH = "path";
  private static final String STATE_LABEL_ALIASES = "label_aliases";

  public static final String RESULT_CODE = "code";
  public static final String RESULT_LABEL_ALIASES = "label_aliases";

  public static final String ARG_CODE = "code";
  public static final String ARG_LABEL_ALIASES = "label_aliases";

  private CodeEditor codeEditor;
  private Code code = CodeUtils.unit;
  private LinkedList<Label> path = new LinkedList<Label>();
  private Path pathFragment;
  private HashMap<Label, String> labelAliases = new HashMap<Label, String>();

  @SuppressWarnings("unchecked")
  @Override
  protected void onCreate(Bundle b) {
    super.onCreate(b);
    setContentView(R.layout.activity_code_editor);

    final ActionBar actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    actionBar.addTab(actionBar.newTab().setText(R.string.tab_new_function)
        .setTabListener(this));
    actionBar.addTab(actionBar.newTab().setText(R.string.tab_new_product)
        .setTabListener(this));
    actionBar.addTab(actionBar.newTab().setText(R.string.tab_new_sum)
        .setTabListener(this));

    pathFragment =
        (Path) getSupportFragmentManager().findFragmentById(R.id.fragment_path);

    code = (Code) getIntent().getSerializableExtra(ARG_CODE);
    labelAliases =
        new HashMap<Label, String>((HashMap<Label, String>) getIntent()
            .getSerializableExtra(ARG_LABEL_ALIASES));

    if (b != null) {
      actionBar.setSelectedNavigationItem(b
          .getInt(STATE_SELECTED_NAVIGATION_ITEM));
      code = (Code) b.get(STATE_CODE);
      path = (LinkedList<Label>) b.get(STATE_PATH);
      labelAliases = (HashMap<Label, String>) b.get(STATE_LABEL_ALIASES);
    }

    initCodeEditor(CodeUtils.followPath(path, code));
    pathFragment.setPath(code, path);

  }

  @Override
  public void onSaveInstanceState(Bundle b) {
    super.onSaveInstanceState(b);
    b.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
        .getSelectedNavigationIndex());
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
  public void onTabSelected(ActionBar.Tab tab,
      FragmentTransaction fragmentTransaction) {
  }

  @Override
  public void onTabUnselected(ActionBar.Tab tab,
      FragmentTransaction fragmentTransaction) {
  }

  @Override
  public void onTabReselected(ActionBar.Tab tab,
      FragmentTransaction fragmentTransaction) {
  }

  @Override
  public void onCodeEdited(Code c) {
    code = replaceCurrentCode(code, path, c);
    pathFragment.setPath(code, path);
    initCodeEditor(c);
  }

  private Code replaceCurrentCode(Code c, List<Label> p, Code newCode) {
    if (p.size() == 0) {
      return newCode;
    }
    Map<Label, CodeRef> m = new HashMap<Label, CodeRef>(c.labels);
    Label l = p.get(0);
    m.put(
        l,
        CodeRef.newCode(replaceCurrentCode(m.get(l).code,
            p.subList(1, p.size()), newCode)));
    return new Code(c.tag, m);
  }

  @Override
  public void codeSelected(Label l) {
    Null.notNull(l);
    Code c = CodeUtils.followPath(path, code);
    CodeRef cr = c.labels.get(l);
    if (cr == null)
      throw new RuntimeException("non-existent label");
    if (cr.tag != CodeRef.Tag.CODE)
      throw new RuntimeException("you can't go there");
    path.add(l);
    pathFragment.setPath(code, path);
    initCodeEditor(cr.code);
  }

  @Override
  public void onCodeInPathSelected(List<Label> subpath) {
    LinkedList<Label> p = new LinkedList<Label>(subpath);
    Code c = CodeUtils.followPath(subpath, code);
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
