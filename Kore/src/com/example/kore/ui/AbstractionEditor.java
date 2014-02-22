package com.example.kore.ui;

import static com.example.kore.ui.PatternUtils.renderPattern;
import static com.example.kore.ui.RelationUtils.renderRelation;
import static com.example.kore.utils.Null.notNull;
import static com.example.kore.utils.OptionalUtils.some;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.codes.Relation;
import com.example.kore.utils.ListUtils;

public class AbstractionEditor extends FrameLayout {

  public interface Listener {
    void relationSelected();

    void patternChanged(Pattern p);
  }

  public AbstractionEditor(final Context context, final Relation.Abstraction a,
      final CodeLabelAliasMap codeLabelAliases, final Listener listener) {
    super(context);
    notNull(a, listener);
    final LinearLayout ll = new LinearLayout(context);
    Button b = new Button(context);
    b.setText(renderPattern(a.pattern, a.i, ListUtils.<Label> nil(),
        codeLabelAliases));
    b.setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        View v =
            PatternEditor.make(context, a.pattern, a.i, codeLabelAliases,
                new PatternEditor.Listener() {
                  public void done(Pattern p) {
                    listener.patternChanged(p);
                  }
                });
        ll.removeAllViews();
        ll.addView(v);
      }
    });
    ChoosePatternMenu.make(b, context, a.i, ListUtils.<Label> nil(),
        codeLabelAliases, new ChoosePatternMenu.Listener() {
          public void select(Pattern p) {
            listener.patternChanged(p);
          }
        });
    ll.addView(b);
    b = new Button(context);
    b.setText(renderRelation(some(a.i), a.r, codeLabelAliases));
    b.setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        listener.relationSelected();
      }
    });
    ll.addView(b);
    addView(ll);
  }

}
