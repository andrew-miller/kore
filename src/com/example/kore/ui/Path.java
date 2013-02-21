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

  public void setPath(Code root, List<Label> p) {
    Null.notNull(p);
    final LinkedList<Label> path = new LinkedList<Label>(p);
    final FragmentActivity a = getActivity();
    this.path.removeAllViews();

    Button rB = new Button(a);
    rB.setWidth(0);
    rB.setHeight(LayoutParams.MATCH_PARENT);
    this.path.addView(rB);
    rB.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        subpathSelectedListener.onCodeInPathSelected(new LinkedList<Label>());
      }
    });
    switch (root.tag) {
    case PRODUCT:
      rB.setText("{...}");
      break;
    case UNION:
      rB.setText("[...]");
      break;
    default:
      throw Boom.boom();
    }

    LinkedList<Label> subpath = new LinkedList<Label>();
    Code code = root;
    for (Label l : path) {
      code = code.labels.get(l);
      Button b1 = new Button(a);
      b1.setBackgroundColor((int) Long.parseLong(l.label.substring(0, 8), 16));
      b1.setWidth(0);
      b1.setHeight(LayoutParams.MATCH_PARENT);
      this.path.addView(b1);
      Button b2 = new Button(a);
      switch (code.tag) {
      case PRODUCT:
        b2.setText("{...}");
        break;
      case UNION:
        b2.setText("[...]");
        break;
      default:
        throw Boom.boom();
      }
      b2.setWidth(0);
      b2.setHeight(LayoutParams.MATCH_PARENT);
      subpath.add(l);
      final LinkedList<Label> subpathS = new LinkedList<Label>(subpath);
      b2.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          subpathSelectedListener.onCodeInPathSelected(subpathS);
        }
      });
      this.path.addView(b2);
    }
  }
}
