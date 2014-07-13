package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.ListUtils.cons;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Null.notNull;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.OptionalUtils.some;
import static com.pokemon.kore.utils.PairUtils.pair;
import static com.pokemon.kore.utils.Unit.unit;

import java.util.HashSet;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.pokemon.kore.R;
import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.CanonicalRelation;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.codes.Relation.Tag;
import com.pokemon.kore.utils.CodeUtils;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public class MainActivity extends FragmentActivity {

  private static final String STATE_CODES = "codes";
  private static final String STATE_RECENT_CODES = "recent_codes";
  private static final String STATE_RELATIONS = "relations";
  private static final String STATE_RECENT_RELATIONS = "recent_relations";
  private static final String STATE_CODE_LABEL_ALIASES = "code_label_aliases";
  private static final String STATE_CODE_ALIASES = "code_aliases";
  private static final String STATE_RELATION_ALIASES = "relation_aliases";
  private static final String STATE_CODE_EDITOR = "code_editor";
  private static final String STATE_RELATION_EDITOR = "relation_editor";
  private static final String STATE_RECENT_CODES_VISIBLE =
      "recent_codes_visible";
  private static final String STATE_RECENT_VISIBLE = "recent_visible";
  private static final String STATE_RUN_AREA = "run_area";

  private final static RelationColors relationColors = new RelationColors(Map
      .<Tag, Pair<Integer, Integer>> empty()
      .put(Tag.ABSTRACTION, pair(0xFFAA00AA, 0xFFFF00FF))
      .put(Tag.COMPOSITION, pair(0xFF0000AA, 0xFF0000FF))
      .put(Tag.LABEL, pair(0xFF00AAAA, 0xFF00FFFF))
      .put(Tag.PRODUCT, pair(0xFF00AAAA, 0xFF00FFFF))
      .put(Tag.PROJECTION, pair(0xFFAA0000, 0xFFFF0000))
      .put(Tag.UNION, pair(0xFF00AA00, 0xFF00FF00)));
  private final static RelationViewColors relationViewColors =
      new RelationViewColors(relationColors, 0xFFCCCCCC, pair(0xFF000000,
          0xFF444444));

  private HashSet<Code> codes = new HashSet<>();
  private List<Code> recentCodes = nil();
  private HashSet<Relation> relations = new HashSet<>();
  private List<Relation> recentRelations = nil();
  private Map<CanonicalCode, Bijection<Label, String>> codeLabelAliases = Map
      .empty();
  private Bijection<CanonicalCode, String> codeAliases = Bijection.empty();
  private Bijection<CanonicalRelation, String> relationAliases = Bijection
      .empty();
  private View mainLayout;
  private ViewGroup codeEditorContainer;
  private ViewGroup relationEditorContainer;
  private Optional<Pair<F<Unit, Bundle>, F<Code, Unit>>> codeEditor = nothing();
  private Optional<Pair<F<Unit, Bundle>, F<Relation, Unit>>> relationEditor =
      nothing();
  private boolean recentCodesVisible;
  private boolean recentVisible = true;
  private F<Unit, Unit> addToRunArea;
  private F<Unit, Bundle> getRunAreaState;

  CodeLabelAliasMap codeLabelAliasMap = new CodeLabelAliasMap() {
    public boolean setAlias(CanonicalCode c, Label l, String alias) {
      Optional<Bijection<Label, String>> o = codeLabelAliases.get(c);
      if (o.isNothing())
        codeLabelAliases =
            codeLabelAliases.put(c,
                Bijection.<Label, String> empty().putX(l, alias).some().x);
      else {
        Optional<Bijection<Label, String>> oo = o.some().x.putX(l, alias);
        if (oo.isNothing())
          return false;
        codeLabelAliases = codeLabelAliases.put(c, oo.some().x);
      }
      return true;
    }

    public void deleteAlias(CanonicalCode c, Label l) {
      Optional<Bijection<Label, String>> o = codeLabelAliases.get(c);
      if (!o.isNothing())
        codeLabelAliases = codeLabelAliases.put(c, o.some().x.deleteX(l));
    }

    public Bijection<Label, String> getAliases(CanonicalCode c) {
      Optional<Bijection<Label, String>> o = codeLabelAliases.get(c);
      if (o.isNothing())
        return Bijection.empty();
      return o.some().x;
    }

    public void setAliases(CanonicalCode c, Bijection<Label, String> aliases) {
      codeLabelAliases = codeLabelAliases.put(c, aliases);
    }
  };

  @Override
  protected void onCreate(Bundle b) {
    super.onCreate(b);

    this.getActionBar().hide();
    setContentView(R.layout.activity_main);
    mainLayout = findViewById(R.id.main_layout);
    codeEditorContainer = (ViewGroup) findViewById(R.id.container_code_editor);
    relationEditorContainer =
        (ViewGroup) findViewById(R.id.container_relation_editor);

    findViewById(R.id.button_new_code).setOnClickListener(
        new OnClickListener() {
          public void onClick(View v) {
            startCodeEditor(CodeUtils.unit);
          }
        });

    findViewById(R.id.button_new_relation).setOnClickListener(
        new OnClickListener() {
          public void onClick(View v) {
            if (recentVisible)
              startRelationEditor(RelationUtils.unit_unit);
            else
              addToRunArea.f(unit());
          }
        });

    Bundle codeEditorState = null;
    Bundle relationEditorState = null;
    Optional<Bundle> runAreaState = nothing();
    if (b != null) {
      codes = (HashSet<Code>) b.get(STATE_CODES);
      recentCodes = (List<Code>) b.get(STATE_RECENT_CODES);
      relations = (HashSet<Relation>) b.get(STATE_RELATIONS);
      recentRelations = (List<Relation>) b.get(STATE_RECENT_RELATIONS);
      codeLabelAliases =
          (Map<CanonicalCode, Bijection<Label, String>>) b
              .get(STATE_CODE_LABEL_ALIASES);
      codeAliases =
          (Bijection<CanonicalCode, String>) b.get(STATE_CODE_ALIASES);
      relationAliases =
          (Bijection<CanonicalRelation, String>) b.get(STATE_RELATION_ALIASES);
      codeEditorState = b.getBundle(STATE_CODE_EDITOR);
      relationEditorState = b.getBundle(STATE_RELATION_EDITOR);
      recentCodesVisible = b.getBoolean(STATE_RECENT_CODES_VISIBLE);
      recentVisible = b.getBoolean(STATE_RECENT_VISIBLE);
      runAreaState = some(b.getBundle(STATE_RUN_AREA));
    }

    initRecentCodes();
    initRecentRelations();
    initRunArea(runAreaState);
    switchRecent();

    ToggleButton recentSwitch = (ToggleButton) findViewById(R.id.recent_switch);
    recentSwitch.setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        recentCodesVisible = !recentCodesVisible;
        recentVisible = true;
        switchRecent();
      }
    });

    if (codeEditorState != null) {
      F<Code, Unit> doneListener = newCodeEditorDoneListener();
      Pair<View, F<Unit, Bundle>> p =
          CodeEditor.make(this, codeEditorState, codeLabelAliasMap,
              codeAliases, recentCodes, doneListener);
      codeEditor = some(pair(p.y, doneListener));
      mainLayout.setVisibility(View.GONE);
      codeEditorContainer.addView(p.x);
      codeEditorContainer.setVisibility(View.VISIBLE);
    }

    if (relationEditorState != null) {
      F<Relation, Unit> doneListener = newRelationEditorDoneListener();
      Pair<View, F<Unit, Bundle>> p =
          RelationEditor.make(this, recentCodes, codeLabelAliasMap,
              codeAliases, relationAliases, recentRelations,
              relationViewColors, relationEditorState, doneListener);
      relationEditor = some(pair(p.y, doneListener));
      mainLayout.setVisibility(View.GONE);
      relationEditorContainer.addView(p.x);
      relationEditorContainer.setVisibility(View.VISIBLE);
    }

    findViewById(R.id.button_run).setOnClickListener(new OnClickListener() {
      public void onClick(View _) {
        recentVisible = false;
        switchRecent();
      }
    });
  }

  @Override
  public void onSaveInstanceState(Bundle b) {
    super.onSaveInstanceState(b);
    b.putSerializable(STATE_CODES, codes);
    b.putSerializable(STATE_RECENT_CODES, recentCodes);
    b.putSerializable(STATE_RELATIONS, relations);
    b.putSerializable(STATE_RECENT_RELATIONS, recentRelations);
    b.putSerializable(STATE_CODE_LABEL_ALIASES, codeLabelAliases);
    b.putSerializable(STATE_CODE_ALIASES, codeAliases);
    b.putSerializable(STATE_RELATION_ALIASES, relationAliases);
    if (!codeEditor.isNothing())
      b.putBundle(STATE_CODE_EDITOR, codeEditor.some().x.x.f(unit()));
    if (!relationEditor.isNothing())
      b.putBundle(STATE_RELATION_EDITOR, relationEditor.some().x.x.f(unit()));
    b.putBoolean(STATE_RECENT_CODES_VISIBLE, recentCodesVisible);
    b.putBoolean(STATE_RECENT_VISIBLE, recentVisible);
    b.putBundle(STATE_RUN_AREA, getRunAreaState.f(unit()));
  }

  private void switchRecent() {
    if (recentVisible) {
      findViewById(R.id.container_run).setVisibility(View.GONE);
      if (recentCodesVisible) {
        findViewById(R.id.container_recent_relations).setVisibility(View.GONE);
        findViewById(R.id.container_recent_codes).setVisibility(View.VISIBLE);
      } else {
        findViewById(R.id.container_recent_codes).setVisibility(View.GONE);
        findViewById(R.id.container_recent_relations).setVisibility(
            View.VISIBLE);
      }
    } else {
      findViewById(R.id.container_recent_relations).setVisibility(View.GONE);
      findViewById(R.id.container_recent_codes).setVisibility(View.GONE);
      findViewById(R.id.container_run).setVisibility(View.VISIBLE);
    }
  }

  private void initRunArea(Optional<Bundle> ob) {
    Pair<Pair<View, F<Unit, Unit>>, F<Unit, Bundle>> rA;
    if (ob.isNothing())
      rA =
          RunArea.make(this, recentCodes, codeLabelAliasMap, codeAliases,
              relationAliases, recentRelations, relationViewColors);
    else
      rA =
          RunArea
              .make(ob.some().x, this, recentCodes, codeLabelAliasMap,
                  codeAliases, relationAliases, recentRelations,
                  relationViewColors);
    ((ViewGroup) findViewById(R.id.container_run)).removeAllViews();
    ((ViewGroup) findViewById(R.id.container_run)).addView(rA.x.x);
    addToRunArea = rA.x.y;
    getRunAreaState = rA.y;
  }

  private void initRecentCodes() {
    CodeList.Listener cll = new CodeList.Listener() {
      public void select(Code c) {
        notNull(c);
        startCodeEditor(c);
      }

      public boolean changeAlias(Code code, List<Label> path, String alias) {
        notNull(code, alias);
        if (!codeEditor.isNothing())
          throw new RuntimeException(
              "code list tried to change alias while code editor was open");
        Optional<Bijection<CanonicalCode, String>> o =
            codeAliases.putX(new CanonicalCode(code, path), alias);
        if (o.isNothing())
          return false;
        codeAliases = o.some().x;
        initRecentCodes();
        return true;
      }
    };
    View cl =
        CodeList.make(this, cll, recentCodes, codeLabelAliasMap, codeAliases);
    ViewGroup v = (ViewGroup) findViewById(R.id.container_recent_codes);
    v.removeAllViews();
    v.addView(cl);
  }

  private void initRecentRelations() {
    RelationList.Listener rll = new RelationList.Listener() {
      public void select(Relation r) {
        notNull(r);
        startRelationEditor(r);
      }

      public boolean changeAlias(Relation relation,
          List<Either3<Label, Integer, Unit>> path, String alias) {
        notNull(relation, alias);
        if (!relationEditor.isNothing())
          throw new RuntimeException(
              "relation list tried to change alias while relation editor was open");
        Optional<Bijection<CanonicalRelation, String>> o =
            relationAliases.putX(new CanonicalRelation(relation, path), alias);
        if (o.isNothing())
          return false;
        relationAliases = o.some().x;
        initRecentRelations();
        return true;
      }
    };
    View rl =
        RelationList.make(this, rll, recentRelations, codeLabelAliasMap,
            relationAliases, relationViewColors);
    ViewGroup v = (ViewGroup) findViewById(R.id.container_recent_relations);
    v.removeAllViews();
    v.addView(rl);
  }

  private void startCodeEditor(Code c) {
    /*
     * Workaround android behavior (can't tell if bug or feature): Without this,
     * a user could create multiple superimposed CodeEditors. He could do this
     * by pressing on two codes in the recent code list at the same time, or by
     * quickly pressing the "new code" button multiple times.
     */
    if (!(codeEditor.isNothing() & relationEditor.isNothing()))
      return;
    F<Code, Unit> doneListener = newCodeEditorDoneListener();
    Pair<View, F<Unit, Bundle>> p =
        CodeEditor.make(this, c, codeLabelAliasMap, codeAliases, recentCodes,
            doneListener);
    codeEditor = some(pair(p.y, doneListener));
    mainLayout.setVisibility(View.GONE);
    codeEditorContainer.addView(p.x);
    codeEditorContainer.setVisibility(View.VISIBLE);
  }

  private void startRelationEditor(Relation r) {
    // same workaround in startCodeEditor
    if (!(relationEditor.isNothing() & codeEditor.isNothing()))
      return;
    F<Relation, Unit> doneListener = newRelationEditorDoneListener();
    Pair<View, F<Unit, Bundle>> p =
        RelationEditor.make(this, r, recentCodes, codeLabelAliasMap,
            codeAliases, relationAliases, recentRelations, relationViewColors,
            doneListener);
    relationEditor = some(pair(p.y, doneListener));
    mainLayout.setVisibility(View.GONE);
    relationEditorContainer.addView(p.x);
    relationEditorContainer.setVisibility(View.VISIBLE);
  }

  private F<Code, Unit> newCodeEditorDoneListener() {
    return new F<Code, Unit>() {
      public Unit f(Code c) {
        if (codeEditor.isNothing() || this != codeEditor.some().x.y)
          throw new RuntimeException(
              "got \"done editing\" event from non-current code editor");
        notNull(c);
        codeEditorContainer.removeAllViews();
        codeEditorContainer.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        if (!codes.contains(c))
          recentCodes = cons(c, recentCodes);
        codes.add(c);
        initRecentCodes();
        codeEditor = nothing();
        initRecentRelations();
        initRunArea(some(getRunAreaState.f(unit())));
        return unit();
      }
    };
  }

  private F<Relation, Unit> newRelationEditorDoneListener() {
    return new F<Relation, Unit>() {
      public Unit f(Relation r) {
        if (relationEditor.isNothing() || this != relationEditor.some().x.y)
          throw new RuntimeException(
              "got \"done editing\" event from non-current relation editor");
        notNull(r);
        relationEditorContainer.removeAllViews();
        relationEditorContainer.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        if (!relations.contains(r))
          recentRelations = cons(r, recentRelations);
        relations.add(r);
        initRecentRelations();
        relationEditor = nothing();
        initRecentCodes();
        initRunArea(some(getRunAreaState.f(unit())));
        return unit();
      }
    };
  }

}