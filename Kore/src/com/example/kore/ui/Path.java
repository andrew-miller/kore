package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.kore.utils.Boom;
import com.example.kore.utils.List;

public class Path extends Fragment {
  private ViewGroup path;
  private SubpathSelectedListener subpathSelectedListener;

  public static interface SubpathSelectedListener {
    public void onCodeInPathSelected(List<Label> subpath);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    subpathSelectedListener = (SubpathSelectedListener) activity;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View v = inflater.inflate(R.layout.path, container, false);
    path = (ViewGroup) v.findViewById(R.id.layout_path);
    return v;
  }

  public void setPath(Code code, List<Label> path) {
    notNull(code, path);
    FragmentActivity a = getActivity();
    this.path.removeAllViews();

    List<Label> subpath = nil();
    while (true) {
      Button b = new Button(a);
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
        @Override
        public void onClick(View v) {
          subpathSelectedListener.onCodeInPathSelected(subpathS);
        }
      });
      this.path.addView(b);
      if (path.isEmpty())
        break;
      Label l = path.cons().x;
      path = path.cons().tail;
      subpath = append(l, subpath);
      CodeOrPath codeOrPath = code.labels.get(l);
      if (codeOrPath.tag != CodeOrPath.Tag.CODE) {
        throw new RuntimeException("path exits the spanning tree");
      }
      code = codeOrPath.code;
      Button b2 = new Button(a);
      b2.setBackgroundColor((int) Long.parseLong(l.label.substring(0, 8), 16));
      b2.setWidth(0);
      b2.setHeight(LayoutParams.MATCH_PARENT);
      this.path.addView(b2);
    }
  }
}
