package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.nil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.List;
import com.example.kore.utils.Null;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends FragmentActivity implements
    CodeList.CodeSelectListener, CodeList.CodeAliasChangedListener {

  private static final String STATE_CODES = "codes";
  private static final String STATE_RECENT_CODES = "recent_codes";
  private static final String STATE_CODE_LABEL_ALIASES = "code_label_aliases";
  private static final String STATE_CODE_ALIASES = "code_aliases";

  private HashSet<Code> codes = new HashSet<Code>();
  private List<Code> recentCodes = nil(Code.class);
  private HashMap<Code, HashMap<Label, String>> codeLabelAliases =
      new HashMap<Code, HashMap<Label, String>>();
  private HashMap<Code, String> codeAliases = new HashMap<Code, String>();

  @Override
  protected void onCreate(Bundle b) {
    super.onCreate(b);
    setContentView(R.layout.activity_main);
    ((Button) findViewById(R.id.button_new_code))
        .setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            startCodeEditor(CodeUtils.unit, new HashMap<Label, String>());
          }
        });

    if (b != null) {
      codes = (HashSet<Code>) b.get(STATE_CODES);
      recentCodes = (List<Code>) b.get(STATE_RECENT_CODES);
      codeLabelAliases =
          (HashMap<Code, HashMap<Label, String>>) b
              .get(STATE_CODE_LABEL_ALIASES);
      codeAliases = (HashMap<Code, String>) b.get(STATE_CODE_ALIASES);
    }

    initRecentCodes();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (data == null)
      return;
    Code code =
        (Code) data.getSerializableExtra(CodeEditorActivity.RESULT_CODE);
    HashMap<Label, String> labelAliases =
        (HashMap<Label, String>) data
            .getSerializableExtra(CodeEditorActivity.RESULT_LABEL_ALIASES);
    Null.notNull(code, labelAliases);
    labelAliases = new HashMap<Label, String>(labelAliases);
    if (!codes.contains(code))
      recentCodes = cons(code, recentCodes);
    codes.add(code);
    codeLabelAliases.put(code, labelAliases);
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
  }

  private void initRecentCodes() {
    CodeList recentCodesFragment = new CodeList();
    Bundle b = new Bundle();
    b.putSerializable(CodeList.ARG_CODES, recentCodes);
    {
      HashMap<Code, HashMap<Label, String>> m =
          new HashMap<Code, HashMap<Label, String>>();
      for (Entry<Code, HashMap<Label, String>> e : codeLabelAliases.entrySet()) {
        m.put(e.getKey(), new HashMap<Label, String>(e.getValue()));
      }
      b.putSerializable(CodeList.ARG_CODE_LABEL_ALIASES, m);
    }
    b.putSerializable(CodeList.ARG_CODE_ALIASES, new HashMap<Code, String>(
        codeAliases));
    recentCodesFragment.setArguments(b);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_recent_codes, recentCodesFragment).commit();
  }

  private void startCodeEditor(Code c, HashMap<Label, String> la) {
    startActivityForResult(
        new Intent(this, CodeEditorActivity.class)
            .putExtra(CodeEditorActivity.ARG_CODE, c)
            .putExtra(CodeEditorActivity.ARG_LABEL_ALIASES,
                new HashMap<Label, String>(la))
            .putExtra(CodeEditorActivity.ARG_CODE_ALIASES,
                new HashMap<Code, String>(codeAliases)), 0);
  }

  @Override
  public void onCodeSelected(Code c) {
    HashMap<Label, String> la = codeLabelAliases.get(c);
    Null.notNull(la);
    startCodeEditor(c, la);
  }

  @Override
  public void codeAliasChanged(Code code, String alias) {
    Null.notNull(code, alias);
    codeAliases.put(code, alias);
    initRecentCodes();
  }

}
