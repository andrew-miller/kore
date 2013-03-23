package com.example.kore.ui;

import java.util.LinkedList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import com.example.kore.R;

public class CodeEditor extends Fragment {
  private LinearLayout fields;
  private final LinkedList<String> list = new LinkedList<String>();
  private int x;
  private boolean sleep;
  private int inits;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.code_editor, container, false);
    fields = (LinearLayout) v.findViewById(R.id.layout_fields);
    ((Button) v.findViewById(R.id.button_clear))
        .setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            list.clear();
            init();
          }
        });
    ((Button) v.findViewById(R.id.button_new_field))
        .setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            list.add("" + x++);
            init();
          }
        });
    init();
    sleep = true;
    return v;
  }

  private void init() {
    String prefix = "" + inits++;
    fields.removeAllViews();
    FragmentTransaction fragmentTransaction =
        getFragmentManager().beginTransaction();
    for (String s : list) {
      Field f = new Field();
      Bundle b = new Bundle();
      b.putString("X", prefix + " - " + s);
      f.setArguments(b);
      fragmentTransaction.add(R.id.layout_fields, f);
    }
    Log.d("BLAH " + prefix, "zzzz");
    if (sleep) {
      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    sleep = !sleep;
    fragmentTransaction.commit();

  }

}
