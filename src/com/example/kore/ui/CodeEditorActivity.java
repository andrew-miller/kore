package com.example.kore.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.example.kore.R;

public class CodeEditorActivity extends FragmentActivity {

  @Override
  protected void onCreate(Bundle b) {
    super.onCreate(b);
    setContentView(R.layout.activity_code_editor);
    CodeEditor e = new CodeEditor();
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.container_code_editor, e).commit();
  }

}
