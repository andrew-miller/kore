package com.example.kore.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeRef;
import com.example.kore.codes.Label;
import com.example.unsuck.Null;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

public class Field extends Fragment {
  public static final String ARG_LABEL = "label";
  public static final String ARG_CODE_REF = "codeRef";
  public static final String ARG_ROOT_CODE = "rootCode";
  public static final String ARG_SELECTED = "selected";

  public static interface CodeSelectedListener {
    public void codeSelected(Label l, Code c);
  }

  public static interface LabelSelectedListener {
    public void labelSelected(Label l);
  }

  public static interface FieldChangedListener {
    public void fieldChanged(List<Label> path, Label label);
  }

  private CodeSelectedListener codeSelectedListener;
  private LabelSelectedListener labelSelectedListener;
  private FieldChangedListener fieldChangedListener;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    codeSelectedListener = (CodeSelectedListener) activity;
    labelSelectedListener = (LabelSelectedListener) activity;
    fieldChangedListener = (FieldChangedListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    Bundle args = getArguments();
    final Label label = (Label) args.get(ARG_LABEL);
    final CodeRef codeRef = (CodeRef) args.get(ARG_CODE_REF);
    final Code rootCode = (Code) args.get(ARG_ROOT_CODE);
    final boolean selected = args.getBoolean(ARG_SELECTED);
    Null.notNull(label, codeRef, rootCode);

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
    if (codeRef.tag == CodeRef.Tag.CODE) {
      codeButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          codeSelectedListener.codeSelected(label, codeRef.code);
        }
      });
    }
    codeButton.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        PopupMenu pm = new PopupMenu(a, v);
        Menu m = pm.getMenu();
        fillMenu(m, CodeRef.newCode(rootCode), "", "", new LinkedList<Label>());
        pm.show();
        return true;
      }

      private void fillMenu(Menu m, CodeRef codeRef, String l, String space,
          final List<Label> path) {
        MenuItem i = m.add(space + l.substring(0, Math.min(10, l.length()))
            + " " + renderCodeRef(codeRef));
        i.setOnMenuItemClickListener(new OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem i) {
            fieldChangedListener.fieldChanged(path, label);
            return true;
          }
        });
        if (codeRef.tag == CodeRef.Tag.CODE) {
          for (Entry<Label, CodeRef> e : codeRef.code.labels.entrySet()) {
            List<Label> path2 = new LinkedList<Label>(path);
            path2.add(e.getKey());
            fillMenu(m, e.getValue(), e.getKey().toString(), space + " ", path2);
          }
        }

      }

    });
    String cs = renderCodeRef(codeRef);
    codeButton.setText(cs.substring(0, Math.min(10, cs.length())));
    return v;
  }

  private static String renderCodeRef(CodeRef cr) {
    return cr.tag == CodeRef.Tag.CODE ? cr.code.toString() : "^";
  }
}
