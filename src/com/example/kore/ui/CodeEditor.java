package com.example.kore.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.Random;
import com.example.unsuck.Boom;
import com.example.unsuck.Null;

public class CodeEditor extends Fragment implements Field.LabelSelectedListener {
  public static final String ARG_CODE = "code";

  public static interface CodeEditedListener {
    public void onCodeEdited(Code c);
  }

  private Code code;
  private CodeEditedListener codeEditedListener;
  private Button deleteButton;
  private Label selectedLabel;
  private LinearLayout fields;
  private Button switchCodeOpButton;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    codeEditedListener = (CodeEditedListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.code_editor, container, false);

    Bundle args = getArguments();
    code = (Code) args.get(ARG_CODE);
    Null.notNull(code);

    fields = (LinearLayout) v.findViewById(R.id.fields);
    deleteButton = ((Button) v.findViewById(R.id.button_delete_field));
    switchCodeOpButton = (Button) v.findViewById(R.id.button_switch_code_op);

    switchCodeOpButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        switchCodeOp();
      }
    });

    ((Button) v.findViewById(R.id.button_new_field))
        .setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Map<Label, Code> m = new HashMap<Label, Code>(code.labels);
            while (m.put(new Label(Random.randomId()), CodeUtils.unit) != null)
              ;
            code = new Code(code.tag, m);

            codeEditedListener.onCodeEdited(code);
            render();
          }
        });
    return v;
  }

  private void switchCodeOp() {
    switch (code.tag) {
    case PRODUCT:
      code = Code.newUnion(code.labels);
      break;
    case UNION:
      code = Code.newProduct(code.labels);
      break;
    default:
      throw Boom.boom();
    }
    codeEditedListener.onCodeEdited(code);
    render();
  }

  @Override
  public void onStart() {
    super.onStart();
    render();
  }

  private void render() {
    switch (code.tag) {
    case PRODUCT:
      switchCodeOpButton.setText("{}");
      break;
    case UNION:
      switchCodeOpButton.setText("[]");
      break;
    default:
      throw Boom.boom();
    }
    fields.removeAllViews();
    FragmentTransaction fragmentTransaction = getFragmentManager()
        .beginTransaction();
    for (final Entry<Label, Code> e : code.labels.entrySet()) {
      Bundle args = new Bundle();
      args.putBoolean(Field.ARG_SELECTED, e.getKey().equals(selectedLabel));
      args.putSerializable(Field.ARG_LABEL, e.getKey());
      args.putSerializable(Field.ARG_CODE, e.getValue());
      Field f = new Field();
      f.setArguments(args);
      fragmentTransaction.add(R.id.fields, f);
    }
    fragmentTransaction.commit();
  }

  public Code getCode() {
    return code;
  }

  @Override
  public void labelSelected(final Label l) {
    deleteButton.setVisibility(View.VISIBLE);
    selectedLabel = l;
    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Map<Label, Code> m = new HashMap<Label, Code>(code.labels);
        m.remove(l);
        code = new Code(code.tag, m);
        codeEditedListener.onCodeEdited(code);
        render();
      }
    });
    render();
  }

}
