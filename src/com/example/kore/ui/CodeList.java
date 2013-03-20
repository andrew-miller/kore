package com.example.kore.ui;

import java.util.LinkedList;

import com.example.kore.R;
import com.example.kore.codes.Code;
import com.example.kore.codes.CodeRef;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.unsuck.Null;

import fj.data.TreeMap;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class CodeList extends Fragment {
  public static final String ARG_CODES = "codes";
  public static final String ARG_CODE_LABEL_ALIASES = "code_label_aliases";

  public static interface CodeSelectListener {
    public void onCodeSelected(Code c);
  }

  private CodeSelectListener codeSelectListener;
  private LinearLayout codeListLayout;
  private LinkedList<Code> codes;
  private TreeMap<Code, TreeMap<Label, String>> codeLabelAliases;

  @Override
  public void onAttach(Activity a) {
    super.onAttach(a);
    codeSelectListener = (CodeSelectListener) a;
  }

  @SuppressWarnings("unchecked")
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.code_list, container, false);

    Bundle args = getArguments();
    codes = (LinkedList<Code>) args.getSerializable(ARG_CODES);
    codeLabelAliases =
        (TreeMap<Code, TreeMap<Label, String>>) args
            .getSerializable(ARG_CODE_LABEL_ALIASES);
    Null.notNull(codes, codeLabelAliases);
    codeListLayout = (LinearLayout) v.findViewById(R.id.layout_code_list);

    FragmentActivity a = getActivity();
    for (final Code c : codes) {
      Button b = new Button(a);
      b.setText(CodeUtils.renderCode(CodeRef.newCode(c), codeLabelAliases
          .get(c).some(), 1));
      b.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          codeSelectListener.onCodeSelected(c);
        }
      });
      codeListLayout.addView(b);
    }

    return v;
  }
}
