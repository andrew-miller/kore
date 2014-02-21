package com.example.kore.ui;

import static com.example.kore.ui.PatternUtils.renderPattern;
import static com.example.kore.utils.CodeUtils.codeAt;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.CodeOrPath.Tag;
import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.utils.List;

public class PatternPath {
  public static interface Listener {
    public void pathSelected(List<Label> p);
  }

  public static View make(Context context,
      final Listener subpathSelectedListener, Pattern pattern, Code code,
      List<Label> path, CodeLabelAliasMap codeLabelAliases) {
    notNull(context, subpathSelectedListener, pattern, code, path);
    Code rootCode = code;
    View v = LayoutInflater.from(context).inflate(R.layout.path, null);
    ViewGroup pathVG = (ViewGroup) v.findViewById(R.id.layout_path);

    List<Label> subpath = nil();
    while (true) {
      Button b = new Button(context);
      b.setText(renderPattern(pattern, rootCode, subpath, codeLabelAliases));
      b.setWidth(0);
      b.setHeight(LayoutParams.MATCH_PARENT);
      final List<Label> subpathS = subpath;
      b.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          subpathSelectedListener.pathSelected(subpathS);
        }
      });
      pathVG.addView(b);
      if (path.isEmpty())
        break;
      Label l = path.cons().x;
      path = path.cons().tail;
      subpath = append(l, subpath);
      CodeOrPath codeOrPath = code.labels.get(l).some().x;
      if (codeOrPath.tag == Tag.PATH)
        code = codeAt(codeOrPath.path, rootCode).some().x;
      else
        code = codeOrPath.code;
      pattern = pattern.fields.get(l).some().x;
      Button b2 = new Button(context);
      b2.setBackgroundColor((int) Long.parseLong(l.label.substring(0, 8), 16));
      b2.setWidth(0);
      b2.setHeight(LayoutParams.MATCH_PARENT);
      pathVG.addView(b2);
    }
    return v;
  }
}
