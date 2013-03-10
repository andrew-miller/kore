package com.example.kore.ui;

import java.util.LinkedList;
import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.utils.CodeUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends FragmentActivity implements
    CodeList.CodeSelectListener {

  private static final String STATE_CODES = "codes";

  private LinkedList<Code> recentCodes = new LinkedList<Code>();

  @SuppressWarnings("unchecked")
  @Override
  protected void onCreate(Bundle b) {
    super.onCreate(b);
    setContentView(R.layout.activity_main);

    ((Button) findViewById(R.id.button_new_code))
        .setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            startCodeEditor(CodeUtils.unit);
          }
        });

    if (b != null) {
      recentCodes = (LinkedList<Code>) b.get(STATE_CODES);
    }

    initRecentCodes();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (data == null)
      return;
    Code code = (Code) data
        .getSerializableExtra(CodeEditorActivity.RESULT_CODE);
    recentCodes.addFirst(code);
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
  }

  private void initRecentCodes() {
    CodeList recentCodesFragment = new CodeList();
    Bundle b = new Bundle();
    b.putSerializable(CodeList.ARG_CODES, recentCodes);
    recentCodesFragment.setArguments(b);
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_recent_codes, recentCodesFragment).commit();
  }

  private void startCodeEditor(Code c) {
    startActivityForResult(new Intent(this, CodeEditorActivity.class).putExtra(
        CodeEditorActivity.ARG_CODE, c), 0);
  }

  @Override
  public void onCodeSelected(Code c) {
    startCodeEditor(c);
  }

}
