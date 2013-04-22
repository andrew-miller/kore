package com.example.kore.ui;

import static com.example.kore.utils.Null.notNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
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
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.Boom;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.List;
import com.example.kore.utils.Null;
import com.example.kore.utils.Random;

public class CodeEditor extends Fragment implements
    Field.LabelSelectedListener, Field.FieldChangedListener {
  public static final String ARG_CODE = "code";
  public static final String ARG_ROOT_CODE = "root_code";
  public static final String ARG_LABEL_ALIASES = "label_aliases";
  public static final String ARG_CODE_ALIASES = "code_aliases";

  public static interface CodeEditedListener {
    public void onCodeEdited(Code c);
  }

  public static interface DoneListener {
    public void onDone();
  }

  private Code code;
  private Code rootCode;
  private CodeEditedListener codeEditedListener;
  private Button deleteButton;
  private Label selectedLabel;
  private LinearLayout fields;
  private Button switchCodeOpButton;
  private Map<Label, String> labelAliases;
  private DoneListener doneListener;
  private Map<Code, String> codeAliases;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    codeEditedListener = (CodeEditedListener) activity;
    doneListener = (DoneListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.code_editor, container, false);

    Bundle args = getArguments();
    code = (Code) args.get(ARG_CODE);
    rootCode = (Code) args.get(ARG_ROOT_CODE);
    labelAliases = (Map<Label, String>) args.get(ARG_LABEL_ALIASES);
    codeAliases = (Map<Code, String>) args.get(ARG_CODE_ALIASES);
    Null.notNull(code, rootCode, labelAliases, codeAliases);

    fields = (LinearLayout) v.findViewById(R.id.layout_fields);
    deleteButton = (Button) v.findViewById(R.id.button_delete_field);
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
            Map<Label, CodeOrPath> m =
                new HashMap<Label, CodeOrPath>(code.labels);
            Label l = null;
            do {
              if (l != null)
                Log.w("code editor", "generated duplicate label");
              l = new Label(Random.randomId());
            } while (m.containsKey(l));
            m.put(l, CodeOrPath.newCode(CodeUtils.unit));
            code = new Code(code.tag, m);

            codeEditedListener.onCodeEdited(code);
          }
        });

    ((Button) v.findViewById(R.id.button_done))
        .setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            doneListener.onDone();
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
    FragmentTransaction fragmentTransaction =
        getFragmentManager().beginTransaction();
    for (final Entry<Label, CodeOrPath> e : code.labels.entrySet()) {
      Bundle args = new Bundle();
      args.putBoolean(Field.ARG_SELECTED, e.getKey().equals(selectedLabel));
      args.putSerializable(Field.ARG_LABEL, e.getKey());
      args.putSerializable(Field.ARG_CODE_OR_PATH, e.getValue());
      args.putSerializable(Field.ARG_ROOT_CODE, rootCode);
      args.putSerializable(Field.ARG_LABEL_ALIASES, new HashMap<Label, String>(
          labelAliases));
      args.putSerializable(Field.ARG_CODE_ALIASES, new HashMap<Code, String>(
          codeAliases));
      Field f = new Field();
      f.setArguments(args);
      fragmentTransaction.add(R.id.layout_fields, f);
    }
    fragmentTransaction.commit();
  }

  @Override
  public void labelSelected(final Label l) {
    notNull(l);
    deleteButton.setVisibility(View.VISIBLE);
    selectedLabel = l;
    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>(code.labels);
        m.remove(l);
        code = new Code(code.tag, m);
        codeEditedListener.onCodeEdited(code);
      }
    });
    render();
  }

  @Override
  public void fieldChanged(List<Label> path, Label label) {
    notNull(label);
    path.checkType(Label.class);
    Map<Label, CodeOrPath> m = new HashMap<Label, CodeOrPath>(code.labels);
    m.put(label, CodeOrPath.newPath(path));
    code = new Code(code.tag, m);
    codeEditedListener.onCodeEdited(code);
  }

}
