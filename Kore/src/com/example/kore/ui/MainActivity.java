package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;

import java.util.HashSet;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Relation;
import com.example.kore.ui.CodeList.CodeAliasChangedListener;
import com.example.kore.ui.CodeList.CodeSelectListener;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;
import com.example.kore.utils.Optional;

public class MainActivity extends FragmentActivity {

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
  private ViewGroup relationEditorContainer;
  private CodeEditor codeEditor;
  private RelationEditor relationEditor;
  // if not null, a code editor is open
  private CodeEditor.DoneListener codeEditorDoneListener;
  // if not null, a relation editor is open
  private RelationEditor.DoneListener relationEditorDoneListener;

  CodeLabelAliasMap codeLabelAliasMap = new CodeLabelAliasMap() {
    public void setAlias(CanonicalCode c, Label l, String alias) {
      Optional<Map<Label, String>> o = codeLabelAliases.get(c);
      if (o.isNothing())
        codeLabelAliases =
            codeLabelAliases.put(c, Map.<Label, String> empty().put(l, alias));
      else
        codeLabelAliases = codeLabelAliases.put(c, o.some().x.put(l, alias));
    }

    public void deleteAlias(CanonicalCode c, Label l) {
      Optional<Map<Label, String>> o = codeLabelAliases.get(c);
      if (o.isNothing())
        codeLabelAliases =
            codeLabelAliases.put(c, Map.<Label, String> empty().delete(l));
      else
        codeLabelAliases = codeLabelAliases.put(c, o.some().x.delete(l));
    }

    public Map<Label, String> getAliases(CanonicalCode c) {
      Optional<Map<Label, String>> o = codeLabelAliases.get(c);
      if (o.isNothing())
        return Map.empty();
      return o.some().x;
    }

    public void setAliases(CanonicalCode c, Map<Label, String> aliases) {
      codeLabelAliases = codeLabelAliases.put(c, aliases);
    }
  };

  @Override
  protected void onCreate(Bundle b) {
    super.onCreate(b);

    setContentView(R.layout.activity_main);
    mainLayout = findViewById(R.id.main_layout);
    codeEditorContainer = (ViewGroup) findViewById(R.id.container_code_editor);
    relationEditorContainer =
        (ViewGroup) findViewById(R.id.container_relation_editor);

    findViewById(R.id.button_new_code).setOnClickListener(
        new OnClickListener() {
          public void onClick(View v) {
            startCodeEditor(CodeUtils.unit);
          }
        });

    findViewById(R.id.button_new_relation).setOnClickListener(
        new OnClickListener() {
          public void onClick(View v) {
            startRelationEditor(RelationUtils.unit_unit);
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
      newCodeEditorDoneListener();
      codeEditor =
          new CodeEditor(this, codeEditorState, codeLabelAliasMap,
              codeEditorDoneListener);
      mainLayout.setVisibility(View.GONE);
      codeEditorContainer.addView(codeEditor);
      codeEditorContainer.setVisibility(View.VISIBLE);
    }
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
    CodeSelectListener csl = new CodeList.CodeSelectListener() {
      public void onCodeSelected(Code c) {
        notNull(c);
        startCodeEditor(c);
      }
    };
    CodeAliasChangedListener cacl = new CodeList.CodeAliasChangedListener() {
      public void codeAliasChanged(Code code, List<Label> path, String alias) {
        notNull(code, alias);
        if (codeEditorDoneListener != null)
          throw new RuntimeException(
              "code list tried to change alias while code editor was open");
        codeAliases = codeAliases.put(new CanonicalCode(code, path), alias);
        initRecentCodes();
      }
    };
    CodeList cl =
        new CodeList(this, csl, recentCodes, codeLabelAliasMap, cacl,
            codeAliases);
    ViewGroup v = (ViewGroup) findViewById(R.id.container_recent_codes);
    v.removeAllViews();
    v.addView(cl);
  }

  private void startCodeEditor(Code c) {
    /*
     * Workaround android behavior (can't tell if bug or feature): Without this,
     * a user could create multiple superimposed CodeEditors. He could do this
     * by pressing on two codes in the recent code list at the same time, or by
     * quickly pressing the "new code" button multiple times.
     */
    if (codeEditorDoneListener != null)
      return;
    if (relationEditorDoneListener != null)
      return;

    newCodeEditorDoneListener();
    codeEditor =
        new CodeEditor(this, c, codeLabelAliasMap, codeAliases, recentCodes,
            codeEditorDoneListener);
    mainLayout.setVisibility(View.GONE);
    codeEditorContainer.addView(codeEditor);
    codeEditorContainer.setVisibility(View.VISIBLE);
  }

  private void startRelationEditor(Relation r) {
    // same workaround in startCodeEditor
    if (relationEditorDoneListener != null)
      return;
    if (codeEditorDoneListener != null)
      return;

    newRelationEditorDoneListener();
    relationEditor =
        new RelationEditor(this, r, relationEditorDoneListener, recentCodes,
            codeLabelAliasMap, codeAliases);
    mainLayout.setVisibility(View.GONE);
    relationEditorContainer.addView(relationEditor);
    relationEditorContainer.setVisibility(View.VISIBLE);
  }

  private void newCodeEditorDoneListener() {
    codeEditorDoneListener = new CodeEditor.DoneListener() {
      public void onDone(Code c) {
        if (this != codeEditorDoneListener)
          throw new RuntimeException(
              "got \"done editing\" event from non-current code editor");
        notNull(c);
        codeEditor = null;
        codeEditorContainer.removeAllViews();
        codeEditorContainer.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        if (!codes.contains(c))
          recentCodes = cons(c, recentCodes);
        codes.add(c);
        initRecentCodes();
        codeEditorDoneListener = null;
      }
    };
  }

  private void newRelationEditorDoneListener() {
    relationEditorDoneListener = new RelationEditor.DoneListener() {
      public void onDone(Relation r) {
        if (this != relationEditorDoneListener)
          throw new RuntimeException(
              "got \"done editing\" event from non-current relation editor");
        notNull(r);
        relationEditor = null;
        relationEditorContainer.removeAllViews();
        relationEditorContainer.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        relationEditorDoneListener = null;
      }
    };
  }

}
