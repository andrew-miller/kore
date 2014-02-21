package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.Boom;
import com.example.kore.utils.List;

public class CodePath extends FrameLayout {
  public static interface SubpathSelectedListener {
    public void pathSelected(List<Label> subpath);
  }

  public CodePath(Context context,
      final SubpathSelectedListener subpathSelectedListener, Code code,
      List<Label> path) {
    super(context);
    notNull(subpathSelectedListener, code, path);
    View v = LayoutInflater.from(context).inflate(R.layout.path, this, true);
    ViewGroup pathVG = (ViewGroup) v.findViewById(R.id.layout_path);

    List<Label> subpath = nil();
    while (true) {
      Button b = new Button(context);
      switch (code.tag) {
      case PRODUCT:
        b.setText("{...}");
        break;
      case UNION:
        b.setText("[...]");
        break;
      default:
        throw Boom.boom();
      }
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
      if (codeOrPath.tag != CodeOrPath.Tag.CODE)
        throw new RuntimeException("path exits the spanning tree");
      code = codeOrPath.code;
      Button b2 = new Button(context);
      b2.setBackgroundColor((int) Long.parseLong(l.label.substring(0, 8), 16));
      b2.setWidth(0);
      b2.setHeight(LayoutParams.MATCH_PARENT);
      pathVG.addView(b2);
    }
  }
}
