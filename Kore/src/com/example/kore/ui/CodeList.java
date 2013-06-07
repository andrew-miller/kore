package com.example.kore.ui;

import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.ListUtils.nil;
import static com.example.kore.utils.Null.notNull;

import java.util.HashMap;
import java.util.Map.Entry;

import com.example.kore.R;
import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.utils.CodeUtils;
import com.example.kore.utils.F;
import com.example.kore.utils.List;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class CodeList extends Fragment {
  public static final String ARG_CODES = "codes";
  public static final String ARG_CODE_LABEL_ALIASES = "code_label_aliases";
  public static final String ARG_CODE_ALIASES = "code_aliases";

  public static interface CodeSelectListener {
    public void onCodeSelected(Code c);
  }

  public static interface CodeAliasChangedListener {
    public void codeAliasChanged(Code code, List<Label> path, String alias);
  }

  private CodeSelectListener codeSelectListener;
  private LinearLayout codeListLayout;
  private List<Code> codes;
  private HashMap<CanonicalCode, HashMap<Label, String>> codeLabelAliases;
  private CodeAliasChangedListener codeAliasChangedListener;
  private HashMap<CanonicalCode, String> codeAliases;

  @Override
  public void onAttach(Activity a) {
    super.onAttach(a);
    codeSelectListener = (CodeSelectListener) a;
    codeAliasChangedListener = (CodeAliasChangedListener) a;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.code_list, container, false);

    Bundle args = getArguments();
    codes = (List<Code>) args.getSerializable(ARG_CODES);
    notNull(codes);
    {
      HashMap<CanonicalCode, HashMap<Label, String>> m =
          new HashMap<CanonicalCode, HashMap<Label, String>>();
      HashMap<CanonicalCode, HashMap<Label, String>> cla =
          (HashMap<CanonicalCode, HashMap<Label, String>>) args
              .getSerializable(ARG_CODE_LABEL_ALIASES);
      notNull(cla);
      for (Entry<CanonicalCode, HashMap<Label, String>> e : cla.entrySet()) {
        notNull(e.getKey(), e.getValue());
        m.put(e.getKey(), new HashMap<Label, String>(e.getValue()));
      }
      codeLabelAliases = m;
    }
    codeAliases =
        new HashMap<CanonicalCode, String>(
            (HashMap<CanonicalCode, String>) args
                .getSerializable(ARG_CODE_ALIASES));
    notNull(codeAliases);
    codeListLayout = (LinearLayout) v.findViewById(R.id.layout_code_list);

    final FragmentActivity a = getActivity();
    for (final Code code : iter(codes)) {
      final FrameLayout fl = new FrameLayout(a);
      Button b = new Button(a);
      String codeName =
          codeAliases.get(new CanonicalCode(code, nil(Label.class)));
      final String strCode =
          codeName == null ? CodeUtils.renderCode(code, nil(Label.class),
              codeLabelAliases, codeAliases, 1) : codeName;
      b.setText(strCode);
      b.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          codeSelectListener.onCodeSelected(code);
        }
      });
      fl.addView(b);

      b.setOnLongClickListener(new OnLongClickListener() {
        @Override
        public boolean onLongClick(final View v) {
          UIUtils.replaceWithTextEntry(fl, v, a, strCode,
              new F<String, Void>() {
                @Override
                public Void f(String s) {
                  codeAliasChangedListener.codeAliasChanged(code,
                      nil(Label.class), s);
                  return null;
                }
              });
          return true;
        }
      });

      codeListLayout.addView(fl);
    }

    return v;
  }

}
