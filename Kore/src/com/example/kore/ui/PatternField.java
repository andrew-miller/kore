package com.example.kore.ui;

import static com.example.kore.ui.PatternUtils.renderPattern;
import static com.example.kore.utils.CodeUtils.codeAt;
import static com.example.kore.utils.CodeUtils.directPath;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Code.Tag;
import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Optional;

public final class PatternField {

  public interface Listener {
    void selected();

    void replace(Pattern p);

    void replace(Label l);
  }

  /** <tt>rootCode</tt> with <tt>path</tt> is the code that this field is in */
  public static View make(final Context context, final Label label,
      final Pattern pattern, final Code rootCode, List<Label> path,
      final CodeLabelAliasMap codeLabelAliases, final Listener listener) {
    notNull(context, label, pattern, path, listener);
    View v = LayoutInflater.from(context).inflate(R.layout.pattern_field, null);
    Button labelButton = (Button) v.findViewById(R.id.button_label);
    final Code code = codeAt(path, rootCode).some().x;
    final CanonicalCode cc = new CanonicalCode(rootCode, path);
    Optional<String> a = codeLabelAliases.getAliases(cc).get(label);
    labelButton.setText(a.isNothing() ? label.toString() : a.some().x);
    Button patternButton = (Button) v.findViewById(R.id.button_pattern);
    patternButton.setText(renderPattern(pattern, rootCode, append(label, path),
        codeLabelAliases));
    patternButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        listener.selected();
      }
    });
    PatternMenu.make(patternButton, context, rootCode,
        directPath(append(label, path), rootCode), codeLabelAliases,
        new PatternMenu.Listener() {
          public void select(Pattern p) {
            listener.replace(p);
          }
        });

    if (code.tag == Tag.UNION)
      labelButton.setOnLongClickListener(new OnLongClickListener() {
        public boolean onLongClick(View v) {
          LabelMenu.make(context, v, code, codeLabelAliases, cc,
              new F<Label, Void>() {
                public Void f(Label l) {
                  listener.replace(l);
                  return null;
                }
              });
          return true;
        }
      });
    return v;
  }
}
