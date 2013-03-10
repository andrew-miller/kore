package com.example.kore.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.unsuck.Null;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends FragmentActivity implements
    CodeList.CodeSelectListener {

  private static final String STATE_CODES = "codes";
  private static final String STATE_CODE_LABEL_ALIASES = "code_label_aliases";

  private LinkedList<Code> recentCodes = new LinkedList<Code>();
  private HashMap<Code, HashMap<Label, String>> codeLabelAliases =
      new HashMap<Code, HashMap<Label, String>>();

  @SuppressWarnings("unchecked")
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
      recentCodes = (LinkedList<Code>) b.get(STATE_CODES);
      codeLabelAliases =
          (HashMap<Code, HashMap<Label, String>>) b
              .get(STATE_CODE_LABEL_ALIASES);
    }

    initRecentCodes();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (data == null)
      return;
    Code code =
        (Code) data.getSerializableExtra(CodeEditorActivity.RESULT_CODE);
    @SuppressWarnings("unchecked")
    HashMap<Label, String> labelAliases =
        (HashMap<Label, String>) data
            .getSerializableExtra(CodeEditorActivity.RESULT_LABEL_ALIASES);
    Null.notNull(code, labelAliases);
    labelAliases = new HashMap<Label, String>(labelAliases);
    recentCodes.addFirst(code);
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
    b.putSerializable(STATE_CODES, recentCodes);
    b.putSerializable(STATE_CODE_LABEL_ALIASES, codeLabelAliases);
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
    recentCodesFragment.setArguments(b);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_recent_codes, recentCodesFragment).commit();
  }

  private void startCodeEditor(Code c, HashMap<Label, String> la) {
    startActivityForResult(
        new Intent(this, CodeEditorActivity.class).putExtra(
            CodeEditorActivity.ARG_CODE, c).putExtra(
            CodeEditorActivity.ARG_LABEL_ALIASES, la), 0);
  }

  @Override
  public void onCodeSelected(Code c) {
    HashMap<Label, String> la = codeLabelAliases.get(c);
    Null.notNull(la);
    startCodeEditor(c, la);
  }

}
