package com.example.kore.ui;

import java.util.LinkedList;
import java.util.List;

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
import com.example.kore.codes.Label;
import com.example.unsuck.Boom;
import com.example.unsuck.Null;

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
    path = (ViewGroup) v.findViewById(R.id.path);
    return v;
  }

  public void setPath(Code code, List<Label> path) {
    Null.notNull(code);
    path = new LinkedList<Label>(path);
    FragmentActivity a = getActivity();
    this.path.removeAllViews();

    LinkedList<Label> subpath = new LinkedList<Label>();
    while (true) {
      Button b = new Button(a);
      switch (code.node.tag) {
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
      final LinkedList<Label> subpathS = new LinkedList<Label>(subpath);
      b.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          subpathSelectedListener.onCodeInPathSelected(subpathS);
        }
      });
      this.path.addView(b);
      if (path.size() == 0) {
        break;
      } else {
        Label l = path.get(0);
        path = path.subList(1, path.size());
        subpath.add(l);
        Code codeRef = code.edges.get(l);
        code = codeRef;
        Button b2 = new Button(a);
        b2.setBackgroundColor((int) Long.parseLong(l.label.substring(0, 8), 16));
        b2.setWidth(0);
        b2.setHeight(LayoutParams.MATCH_PARENT);
        this.path.addView(b2);
      }
    }
  }
}
