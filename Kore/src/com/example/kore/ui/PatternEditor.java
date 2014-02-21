package com.example.kore.ui;

import static com.example.kore.ui.PatternUtils.patternAt;
import static com.example.kore.ui.PatternUtils.replacePatternAt;
import static com.example.kore.utils.CodeUtils.directPath;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import com.example.kore.utils.Optional;

public final class PatternEditor {

  public interface Listener {
    public void done(Pattern p);
  }

  final static class S {
    Pattern pattern;
    List<Label> path;
    View nodeEditor;
    F<List<Label>, Void> setPath;
    F<Pattern, Void> initNodeEditor;
  }

  public static View make(final Context context, final Pattern pattern,
      final Code code, final CodeLabelAliasMap codeLabelAliases,
      final Listener listener) {
    notNull(context, pattern, code);
    final Code rootCode = code;
    final S s = new S();
    s.pattern = pattern;
    s.path = nil();
    final View v =
        LayoutInflater.from(context).inflate(R.layout.pattern_editor, null);
    final ViewGroup pathContainer =
        (ViewGroup) v.findViewById(R.id.container_path);

    s.setPath = new F<List<Label>, Void>() {
      public Void f(List<Label> path) {
        pathContainer.removeAllViews();
        pathContainer.addView(PatternPath.make(context,
            new PatternPath.Listener() {
              public void pathSelected(List<Label> p) {
                notNull(p);
                Optional<Pattern> or = patternAt(s.pattern, p);
                if (or.isNothing())
                  throw new RuntimeException("invalid path");
                s.path = p;
                s.setPath.f(p);
                s.initNodeEditor.f(or.some().x);
              }
            }, s.pattern, rootCode, path, codeLabelAliases));
        return null;
      }
    };

    s.initNodeEditor = new F<Pattern, Void>() {
      public Void f(Pattern pattern) {
        s.nodeEditor =
            PatternNodeEditor.make(context, pattern, rootCode,
                directPath(s.path, rootCode), new PatternNodeEditor.Listener() {
                  public void selected(Label l) {
                    Pattern p = patternAt(s.pattern, s.path).some().x;
                    Optional<Pattern> op = p.fields.get(l);
                    if (op.isNothing())
                      throw new RuntimeException("invalid label");
                    s.path = append(l, s.path);
                    s.initNodeEditor.f(op.some().x);
                    s.setPath.f(s.path);
                  }

                  public void replace(Pattern n) {
                    s.pattern = replacePatternAt(s.pattern, s.path, n).some().x;
                    s.initNodeEditor.f(patternAt(s.pattern, s.path).some().x);
                  }

                  public void onDone() {
                    listener.done(s.pattern);
                  }
                }, codeLabelAliases);
        ViewGroup cont = (ViewGroup) v.findViewById(R.id.container_node_editor);
        cont.removeAllViews();
        cont.addView(s.nodeEditor);
        return null;
      }
    };

    s.initNodeEditor.f(pattern);
    s.setPath.f(s.path);
    return v;
  }
}
