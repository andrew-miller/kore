package com.example.kore.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeRef;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;

public class MainActivity extends FragmentActivity implements
    ActionBar.TabListener, Field.CodeSelectedListener,
    CodeEditor.CodeEditedListener, Path.SubpathSelectedListener,
    Field.LabelSelectedListener, Field.FieldChangedListener,
    Field.LabelAliasChangedListener {

  private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

  private CodeEditor codeEditor;
  private Code code = CodeUtils.unit;
  private List<Label> path = new LinkedList<Label>();
  private Path pathFragment;
  private final Map<Label, String> labelAliases = new HashMap<Label, String>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final ActionBar actionBar = getActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    actionBar.addTab(actionBar.newTab().setText(R.string.tab_new_function)
        .setTabListener(this));
    actionBar.addTab(actionBar.newTab().setText(R.string.tab_new_product)
        .setTabListener(this));
    actionBar.addTab(actionBar.newTab().setText(R.string.tab_new_sum)
        .setTabListener(this));

    pathFragment = (Path) getSupportFragmentManager().findFragmentById(
        R.id.path);

    initCodeEditor(code);
    pathFragment.setPath(code, new LinkedList<Label>());

  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
    // Restore the previously serialized current tab position.
    if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
      getActionBar().setSelectedNavigationItem(
          savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    // Serialize the current tab position.
    outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
        .getSelectedNavigationIndex());
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
  public void codeSelected(Label l, Code c) {
    path.add(l);
    pathFragment.setPath(code, path);
    initCodeEditor(c);
  }

  @Override
  public void onCodeInPathSelected(List<Label> subpath) {
    Code c = code;
    List<Label> p = new LinkedList<Label>();
    for (Label l : subpath) {
      p.add(l);
      CodeRef cr;
      if ((cr = c.labels.get(l)) == null) {
        throw new RuntimeException("selected nonexistent path");
      }
      if (cr.tag != CodeRef.Tag.CODE) {
        throw new RuntimeException("you can't go there");
      }
      c = cr.code;
    }
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
    labelAliases.put(label, alias);
    Code c = code;
    for (Label l : path) {
      CodeRef cr = c.labels.get(l);
      assert cr.tag == CodeRef.Tag.CODE;
      c = cr.code;
    }
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
        .replace(R.id.fieldContainer, codeEditor).commit();
  }
}
