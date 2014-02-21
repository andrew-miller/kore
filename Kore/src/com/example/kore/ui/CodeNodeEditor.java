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
import com.example.kore.ui.CodeField.CodeSelectedListener;
import com.example.kore.ui.CodeField.FieldReplacedListener;
import com.example.kore.ui.CodeField.LabelAliasChangedListener;
import com.example.kore.ui.CodeField.LabelSelectedListener;
import com.example.kore.utils.Boom;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;

public class CodeNodeEditor extends FrameLayout {
  public static interface Listener {
    void newField();

    void switchCodeOp();

    void deleteField(Label l);

    void codeSelected(Label l);

    void fieldReplaced(CodeOrPath cp, Label l);

    void labelAliasChanged(Label label, String alias);

    void onDone();
  }

  private final Code code;
  private final Code rootCode;
  private final Listener listener;
  private final Button deleteButton;
  private Label selectedLabel;
  private final LinearLayout fields;
  private final Button switchCodeOpButton;
  private final Map<CanonicalCode, String> codeAliases;
  private final List<Code> codes;
  private final List<Label> path;
  private final CodeLabelAliasMap codeLabelAliases;

  public CodeNodeEditor(Context context, Code code, Code rootCode,
      final Listener listener, Map<CanonicalCode, String> codeAliases,
      List<Code> codes, List<Label> path, CodeLabelAliasMap codeLabelAliases) {
    super(context);
    notNull(code, rootCode, codeAliases, codes, path);
    this.code = code;
    this.rootCode = rootCode;
    this.listener = listener;
    this.codeAliases = codeAliases;
    this.codes = codes;
    this.path = path;
    this.codeLabelAliases = codeLabelAliases;
    View v =
        LayoutInflater.from(context).inflate(R.layout.code_node_editor, this,
            true);
    fields = (LinearLayout) v.findViewById(R.id.layout_fields);
    deleteButton = (Button) v.findViewById(R.id.button_delete_field);
    switchCodeOpButton = (Button) v.findViewById(R.id.button_switch_code_op);
    switchCodeOpButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        listener.switchCodeOp();
      }
    });
    v.findViewById(R.id.button_new_field).setOnClickListener(
        new View.OnClickListener() {
          public void onClick(View v) {
            listener.newField();
          }
        });

    v.findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        listener.onDone();
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
    Map<Label, String> las =
        codeLabelAliases.getAliases(new CanonicalCode(rootCode, path));
    for (final Entry<Label, CodeOrPath> e : iter(code.labels.entrySet())) {
      final Label l = e.k;
      LabelSelectedListener lsl = new CodeField.LabelSelectedListener() {
        public void labelSelected() {
          notNull(l);
          deleteButton.setVisibility(View.VISIBLE);
          selectedLabel = l;
          deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              listener.deleteField(l);
            }
          });
          render();
        }
      };

      CodeSelectedListener csl = new CodeField.CodeSelectedListener() {
        public void codeSelected() {
          listener.codeSelected(l);
        }
      };

      FieldReplacedListener frl = new CodeField.FieldReplacedListener() {
        public void fieldReplaced(CodeOrPath cp) {
          listener.fieldReplaced(cp, l);
        }
      };

      LabelAliasChangedListener lacl =
          new CodeField.LabelAliasChangedListener() {
            public void labelAliasChanged(String alias) {
              listener.labelAliasChanged(l, alias);
            }
          };

      fields.addView(new CodeField(getContext(), csl, lsl, frl, lacl, l, e.v,
          rootCode, l.equals(selectedLabel), codeLabelAliases, codeAliases,
          codes, path, las.get(l)));
    }
  }

}
