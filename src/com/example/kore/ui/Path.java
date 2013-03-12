package com.example.kore.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeRef;
import com.example.kore.codes.Label;
import com.example.unsuck.Boom;
import com.example.unsuck.Null;

import fj.F2;
import fj.data.List;

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

  private void addCode(Code c, final List<Label> p) {
    Button b = new Button(getActivity());
    switch (c.tag) {
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
    b.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        subpathSelectedListener.onCodeInPathSelected(p);
      }
    });
    Path.this.path.addView(b);
  }

  private void addLabel(Label l) {
    Button b = new Button(getActivity());
    b.setBackgroundColor((int) Long.parseLong(l.label.substring(0, 8), 16));
    b.setWidth(0);
    b.setHeight(LayoutParams.MATCH_PARENT);
    Path.this.path.addView(b);
  }

  public void setPath(Code code, List<Label> path) {
    Null.notNull(code, path);
    this.path.removeAllViews();
    addCode(code, List.<Label> nil());
    path.inits().tail().foldLeft(new F2<Code, List<Label>, Code>() {
      @Override
      public Code f(Code c, final List<Label> p) {
        Label l = p.last();
        addLabel(l);
        CodeRef codeRef = c.labels.get(l);
        if (codeRef.tag != CodeRef.Tag.CODE)
          throw new RuntimeException("path exits the spanning tree");
        addCode(codeRef.code, p);
        return codeRef.code;
      }
    }, code);

  }
}
