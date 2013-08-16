package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;

import java.util.HashSet;
import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends FragmentActivity implements
    CodeList.CodeSelectListener, CodeList.CodeAliasChangedListener,
    CodeEditor.DoneListener {

  private static final String STATE_CODES = "codes";
  private static final String STATE_RECENT_CODES = "recent_codes";
  private static final String STATE_CODE_LABEL_ALIASES = "code_label_aliases";
  private static final String STATE_CODE_ALIASES = "code_aliases";
  private static final String STATE_CODE_EDITOR = "code_editor";

  private HashSet<Code> codes = new HashSet<Code>();
  private List<Code> recentCodes = nil();
  private Map<CanonicalCode, Map<Label, String>> codeLabelAliases = Map.empty();
  private Map<CanonicalCode, String> codeAliases = Map.empty();
  private View mainLayout;
  private ViewGroup codeEditorContainer;
  private CodeEditor codeEditor;

  @Override
  protected void onCreate(Bundle b) {
    super.onCreate(b);

    setContentView(R.layout.activity_main);
    mainLayout = findViewById(R.id.main_layout);
    codeEditorContainer = (ViewGroup) findViewById(R.id.container_code_editor);

    ((Button) findViewById(R.id.button_new_code))
        .setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            startCodeEditor(CodeUtils.unit);
          }
        });

    Bundle codeEditorState = null;
    if (b != null) {
      codes = (HashSet<Code>) b.get(STATE_CODES);
      recentCodes = (List<Code>) b.get(STATE_RECENT_CODES);
      codeLabelAliases =
          (Map<CanonicalCode, Map<Label, String>>) b
              .get(STATE_CODE_LABEL_ALIASES);
      codeAliases = (Map<CanonicalCode, String>) b.get(STATE_CODE_ALIASES);
      codeEditorState = b.getBundle(STATE_CODE_EDITOR);
    }

    initRecentCodes();

    if (codeEditorState != null) {
      codeEditor = new CodeEditor(this, codeEditorState, this);
      mainLayout.setVisibility(View.GONE);
      codeEditorContainer.addView(codeEditor);
      codeEditorContainer.setVisibility(View.VISIBLE);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    /*
     * calling initRecentCodes here instead of onActivityResult because calling
     * it from onActivityResult causes IllegalStateException
     */
    initRecentCodes();
  }

  @Override
  public void onSaveInstanceState(Bundle b) {
    super.onSaveInstanceState(b);
    b.putSerializable(STATE_CODES, codes);
    b.putSerializable(STATE_RECENT_CODES, recentCodes);
    b.putSerializable(STATE_CODE_LABEL_ALIASES, codeLabelAliases);
    b.putSerializable(STATE_CODE_ALIASES, codeAliases);
    if (codeEditor != null)
      b.putBundle(STATE_CODE_EDITOR, codeEditor.getState());
  }

  private void initRecentCodes() {
    CodeList cl =
        new CodeList(this, this, recentCodes, codeLabelAliases, this,
            codeAliases);
    ViewGroup v = (ViewGroup) findViewById(R.id.container_recent_codes);
    v.removeAllViews();
    v.addView(cl);
  }

  private void startCodeEditor(Code c) {
    codeEditor =
        new CodeEditor(this, c, codeLabelAliases, codeAliases,
            recentCodes, this);
    mainLayout.setVisibility(View.GONE);
    codeEditorContainer.addView(codeEditor);
    codeEditorContainer.setVisibility(View.VISIBLE);
  }

  @Override
  public void onCodeSelected(Code c) {
    notNull(c);
    startCodeEditor(c);
  }

  @Override
  public void codeAliasChanged(Code code, List<Label> path, String alias) {
    notNull(code, alias);
    codeAliases = codeAliases.put(new CanonicalCode(code, path), alias);
    initRecentCodes();
  }

  @Override
  public void onDone(Code code,
      Map<CanonicalCode, Map<Label, String>> codeLabelAliases) {
    notNull(code, codeLabelAliases);
    codeEditor = null;
    codeEditorContainer.removeAllViews();
    codeEditorContainer.setVisibility(View.GONE);
    mainLayout.setVisibility(View.VISIBLE);
    if (!codes.contains(code))
      recentCodes = cons(code, recentCodes);
    codes.add(code);
    this.codeLabelAliases = codeLabelAliases;
    initRecentCodes();
  }

}
