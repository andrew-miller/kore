package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.ui.Field.CodeSelectedListener;
import com.example.kore.ui.Field.FieldReplacedListener;
import com.example.kore.ui.Field.LabelAliasChangedListener;
import com.example.kore.ui.Field.LabelSelectedListener;
import com.example.kore.utils.Boom;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.Optional;
import com.example.kore.utils.OptionalUtils;

public class NodeEditor extends FrameLayout {
  public static interface NodeEditorListener {
    void newField();

    void switchCodeOp();

    void deleteField(Label l);

    void codeSelected(Label l);

    void fieldReplaced(CodeOrPath cp, Label l);

    void labelAliasChanged(Label label, String alias);
  }

  public static interface DoneListener {
    public void onDone();
  }

  private final Code code;
  private final Code rootCode;
  private final NodeEditorListener nodeEditorListener;
  private final Button deleteButton;
  private Label selectedLabel;
  private final LinearLayout fields;
  private final Button switchCodeOpButton;
  private final Map<CanonicalCode, String> codeAliases;
  private final List<Code> codes;
  private final List<Label> path;
  private final Map<CanonicalCode, Map<Label, String>> codeLabelAliases;

  public NodeEditor(Context context, Code code, Code rootCode,
      final NodeEditorListener nodeEditorListener,
      final DoneListener doneListener, Map<CanonicalCode, String> codeAliases,
      List<Code> codes, List<Label> path,
      Map<CanonicalCode, Map<Label, String>> codeLabelAliases) {
    super(context);
    notNull(code, rootCode, codeAliases, codes, path);
    this.code = code;
    this.rootCode = rootCode;
    this.nodeEditorListener = nodeEditorListener;
    this.codeAliases = codeAliases;
    this.codes = codes;
    this.path = path;
    this.codeLabelAliases = codeLabelAliases;
    View v =
        LayoutInflater.from(context).inflate(R.layout.node_editor, this, true);
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
    Optional<Map<Label, String>> las =
        codeLabelAliases.get(new CanonicalCode(rootCode, path));
    for (final Entry<Label, CodeOrPath> e : iter(code.labels.entrySet())) {
      final Label l = e.k;
      LabelSelectedListener lsl = new Field.LabelSelectedListener() {
        @Override
        public void labelSelected() {
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
      };

      CodeSelectedListener csl = new Field.CodeSelectedListener() {
        @Override
        public void codeSelected() {
          nodeEditorListener.codeSelected(l);
        }
      };

      FieldReplacedListener frl = new Field.FieldReplacedListener() {
        @Override
        public void fieldReplaced(CodeOrPath cp) {
          nodeEditorListener.fieldReplaced(cp, l);
        }
      };

      LabelAliasChangedListener lacl = new Field.LabelAliasChangedListener() {
        @Override
        public void labelAliasChanged(String alias) {
          nodeEditorListener.labelAliasChanged(l, alias);
        }
      };

      fields.addView(new Field(getContext(), csl, lsl, frl, lacl, l, e.v,
          rootCode, l.equals(selectedLabel), codeLabelAliases, codeAliases,
          codes, path, las.isNothing() ? OptionalUtils.<String> nothing() : las
              .some().x.get(l)));
    }
  }

}
