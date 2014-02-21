package com.example.kore.ui;

import static com.example.kore.ui.PatternUtils.replacePatternAt;
import static com.example.kore.utils.CodeUtils.codeAt;
import static com.example.kore.utils.ListUtils.cons;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.utils.List;
import com.example.kore.utils.ListUtils;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;

public class PatternNodeEditor {

  public interface Listener {
    void selected(Label l);

    void replace(Pattern p);

    void onDone();
  }

  public static View make(final Context context, final Pattern pattern,
      Code rootCode, List<Label> path, final Listener listener,
      CodeLabelAliasMap codeLabelAliases) {
    notNull(context, pattern, rootCode, path, listener);
    final Code code = codeAt(path, rootCode).some().x;
    View v =
        LayoutInflater.from(context)
            .inflate(R.layout.pattern_node_editor, null);
    LinearLayout fields = (LinearLayout) v.findViewById(R.id.layout_fields);

    v.findViewById(R.id.button_done).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        listener.onDone();
      }
    });

    for (final Entry<Label, Pattern> e : iter(pattern.fields.entrySet())) {
      View pf =
          PatternField.make(context, e.k, e.v, rootCode, path,
              codeLabelAliases, new PatternField.Listener() {
                public void selected() {
                  listener.selected(e.k);
                }

                public void replace(Label l) {
                  if (code.tag != Code.Tag.UNION)
                    throw new RuntimeException(
                        "can only replace label of union");
                  listener.replace(new Pattern(Map.<Label, Pattern> empty()
                      .put(l, e.v)));
                }

                public void replace(Pattern p) {
                  listener.replace(replacePatternAt(pattern,
                      cons(e.k, ListUtils.<Label> nil()), p).some().x);
                }
              });
      fields.addView(pf);
    }
    return v;
  }
}
