package com.example.kore.ui;

import static com.example.kore.utils.Null.notNull;
import static com.example.kore.utils.OptionalUtils.fromObject;
import static com.example.kore.utils.OptionalUtils.nothing;

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
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.Boom;
import com.example.kore.utils.List;
import com.example.kore.utils.MapUtils;

public class NodeEditor extends Fragment implements Field.LabelSelectedListener {
  public static final String ARG_CODE = "code";
  public static final String ARG_ROOT_CODE = "root_code";
  public static final String ARG_CODE_LABEL_ALIASES = "code_label_aliases";
  public static final String ARG_CODE_ALIASES = "code_aliases";
  public static final String ARG_CODES = "codes";
  public static final String ARG_PATH = "path";

  public static interface NodeEditorListener {
    void newField();

    void switchCodeOp();

    void deleteField(Label l);
  }

  public static interface DoneListener {
    public void onDone();
  }

  private Code code;
  private Code rootCode;
  private NodeEditorListener nodeEditorListener;
  private Button deleteButton;
  private Label selectedLabel;
  private LinearLayout fields;
  private Button switchCodeOpButton;
  private DoneListener doneListener;
  private Map<CanonicalCode, String> codeAliases;
  private List<Code> codes;
  private List<Label> path;
  private HashMap<CanonicalCode, HashMap<Label, String>> codeLabelAliases;

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    nodeEditorListener = (NodeEditorListener) activity;
    doneListener = (DoneListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.code_editor, container, false);

    Bundle args = getArguments();
    code = (Code) args.get(ARG_CODE);
    rootCode = (Code) args.get(ARG_ROOT_CODE);
    codeLabelAliases =
        MapUtils
            .cloneNestedMap((HashMap<CanonicalCode, HashMap<Label, String>>) args
                .get(ARG_CODE_LABEL_ALIASES));
    codeAliases = (Map<CanonicalCode, String>) args.get(ARG_CODE_ALIASES);
    codes = (List<Code>) args.get(ARG_CODES);
    codes.checkType(Code.class);
    path = (List<Label>) args.get(ARG_PATH);
    path.checkType(Label.class);
    notNull(code, rootCode, codeAliases);

    fields = (LinearLayout) v.findViewById(R.id.layout_fields);
    deleteButton = (Button) v.findViewById(R.id.button_delete_field);
    switchCodeOpButton = (Button) v.findViewById(R.id.button_switch_code_op);

    switchCodeOpButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        nodeEditorListener.switchCodeOp();
      }
    });

    ((Button) v.findViewById(R.id.button_new_field))
        .setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            nodeEditorListener.newField();
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
    HashMap<Label, String> las =
        codeLabelAliases.get(new CanonicalCode(rootCode, path));
    for (final Entry<Label, CodeOrPath> e : code.labels.entrySet()) {
      Bundle args = new Bundle();
      args.putBoolean(Field.ARG_SELECTED, e.getKey().equals(selectedLabel));
      args.putSerializable(Field.ARG_LABEL, e.getKey());
      args.putSerializable(Field.ARG_CODE_OR_PATH, e.getValue());
      args.putSerializable(Field.ARG_ROOT_CODE, rootCode);
      args.putSerializable(Field.ARG_CODE_LABEL_ALIASES,
          MapUtils.cloneNestedMap(codeLabelAliases));
      args.putSerializable(Field.ARG_CODE_ALIASES,
          new HashMap<CanonicalCode, String>(codeAliases));
      args.putSerializable(Field.ARG_CODES, codes);
      args.putSerializable(
          Field.ARG_LABEL_ALIAS,
          las == null ? nothing(String.class) : fromObject(las.get(e.getKey()),
              String.class));
      args.putSerializable(Field.ARG_PATH, path);
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
        nodeEditorListener.deleteField(l);
      }
    });
    render();
  }

}
