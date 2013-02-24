package com.example.kore.ui;

import java.util.Map.Entry;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.unsuck.Null;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

public class Field extends Fragment {
  public static final String ARG_LABEL = "label";
  public static final String ARG_CODE = "code";
  public static final String ARG_ROOT_CODE = "rootCode";
  public static final String ARG_SELECTED = "selected";

  public static interface CodeSelectedListener {
    public void codeSelected(Label l, Code c);
  }

  public static interface LabelSelectedListener {
    public void labelSelected(Label l);
  }

  private CodeSelectedListener codeSelectedListener;
  private LabelSelectedListener labelSelectedListener;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    codeSelectedListener = (CodeSelectedListener) activity;
    labelSelectedListener = (LabelSelectedListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    Bundle args = getArguments();
    final Label label = (Label) args.get(ARG_LABEL);
    final Code code = (Code) args.get(ARG_CODE);
    final Code rootCode = (Code) args.get(ARG_ROOT_CODE);
    final boolean selected = args.getBoolean(ARG_SELECTED);
    Null.notNull(label);
    Null.notNull(code);
    Null.notNull(rootCode);

    View v = inflater.inflate(R.layout.field, container, false);
    final FragmentActivity a = getActivity();

    Button labelButton = (Button) v.findViewById(R.id.label);
    labelButton.setBackgroundColor((int) Long.parseLong(
        label.label.substring(0, 8), 16));
    if (selected)
      labelButton.setText("---");
    labelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        labelSelectedListener.labelSelected(label);
      }
    });
    labelButton.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        PopupMenu pm = new PopupMenu(a, v);
        Menu m = pm.getMenu();
        m.add(label.label);
        pm.show();
        return true;
      }
    });

    Button codeButton = (Button) v.findViewById(R.id.code);
    codeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        codeSelectedListener.codeSelected(label, code);
      }
    });
    codeButton.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        PopupMenu pm = new PopupMenu(a, v);
        Menu m = pm.getMenu();
        fillMenu(m, rootCode, "", "");
        pm.show();
        return true;
      }

      private void fillMenu(Menu m, Code code, String label, String space) {
        Code c = code;
        m.add(space + label.substring(0, Math.min(10, label.length())) + " "
            + code);
        for (Entry<Label, Code> e : c.labels.entrySet()) {
          fillMenu(m, e.getValue(), e.getKey().toString(), space + " ");
        }

      }
    });
    String cs = code.toString();
    codeButton.setText(cs.substring(0, Math.min(10, cs.length())));
    return v;
  }
}
