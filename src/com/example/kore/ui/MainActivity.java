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
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;

public class MainActivity extends FragmentActivity implements
    ActionBar.TabListener, Field.CodeSelectedListener,
    CodeEditor.CodeEditedListener, Path.SubpathSelectedListener,
    Field.LabelSelectedListener {

  private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

  private CodeEditor codeEditor;
  private Code code = CodeUtils.unit;
  private List<Label> path = new LinkedList<Label>();
  private Path pathFragment;

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

    codeEditor = new CodeEditor();
    Bundle b = new Bundle();
    b.putSerializable(CodeEditor.ARG_CODE, CodeUtils.unit);
    codeEditor.setArguments(b);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.fieldContainer, codeEditor).commit();
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
  }

  private Code replaceCurrentCode(Code c, List<Label> p, Code newCode) {
    if (p.size() == 0) {
      return newCode;
    }
    Map<Label, Code> m = new HashMap<Label, Code>(c.labels);
    Label l = p.get(0);
    m.put(l, replaceCurrentCode(m.get(l), p.subList(1, p.size()), newCode));
    return new Code(c.tag, m);
  }

  @Override
  public void codeSelected(Label l, Code c) {
    path.add(l);
    pathFragment.setPath(code, path);

    codeEditor = new CodeEditor();
    Bundle b = new Bundle();
    b.putSerializable(CodeEditor.ARG_CODE, c);
    codeEditor.setArguments(b);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.fieldContainer, codeEditor).commit();
  }

  @Override
  public void onCodeInPathSelected(List<Label> subpath) {
    Code c = code;
    List<Label> p = new LinkedList<Label>();
    for (Label l : subpath) {
      p.add(l);
      if ((c = c.labels.get(l)) == null) {
        throw new RuntimeException("selected nonexistent path");
      }
    }
    path = p;
    codeEditor = new CodeEditor();
    Bundle b = new Bundle();
    b.putSerializable(CodeEditor.ARG_CODE, c);
    codeEditor.setArguments(b);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.fieldContainer, codeEditor).commit();
  }

  @Override
  public void labelSelected(Label l) {
    codeEditor.labelSelected(l);
  }
}
