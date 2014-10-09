package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.CodeUtils.hash;
import static com.pokemon.kore.utils.ListUtils.cons;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.ListUtils.nil;
import static com.pokemon.kore.utils.Null.notNull;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.OptionalUtils.some;
import static com.pokemon.kore.utils.PairUtils.pair;
import static com.pokemon.kore.utils.Unit.unit;

import java.util.HashSet;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.pokemon.kore.R;
import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.CanonicalRelation;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Code2;
import com.pokemon.kore.codes.Code2.Link;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Relation;
import com.pokemon.kore.codes.З2Bytes;
import com.pokemon.kore.utils.CodeUtils;
import com.pokemon.kore.utils.CodeUtils.Resolver;
import com.pokemon.kore.utils.Either3;
import com.pokemon.kore.utils.F;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;
import com.pokemon.kore.utils.Unit;

public class MainActivity extends FragmentActivity {

  private static final String STATE_CODES = "codes";
  private static final String STATE_RECENT_CODES2 = "recent_codes2";
  private static final String STATE_RECENT_CODES = "recent_codes";
  private static final String STATE_RELATIONS = "relations";
  private static final String STATE_RECENT_RELATIONS = "recent_relations";
  private static final String STATE_CODE_LABEL_ALIASES = "code_label_aliases";
  private static final String STATE_CODE_LABEL_ALIASES2 = "code_label_aliases2";
  private static final String STATE_CODE_ALIASES2 = "code_aliases2";
  private static final String STATE_CODE_ALIASES = "code_aliases";
  private static final String STATE_RELATION_ALIASES = "relation_aliases";
  private static final String STATE_CODE_EDITOR = "code_editor";
  private static final String STATE_RELATION_EDITOR = "relation_editor";
  private static final String STATE_RECENT_CODES_VISIBLE =
      "recent_codes_visible";
  private static final String STATE_RECENT_VISIBLE = "recent_visible";
  private static final String STATE_RUN_AREA = "run_area";
  private static final String STATE_RELATION_VIEW_COLORS =
      "relation_view_colors";

  private static RelationViewColors relationViewColors;
  private HashSet<Code2> codes = new HashSet<>();
  private List<Code2> recentCodes2 = nil();
  private List<Code> recentCodes = nil();
  private HashSet<Relation> relations = new HashSet<>();
  private List<Relation> recentRelations = nil();
  private Map<Link, Bijection<Label, String>> codeLabelAliases2 = Map.empty();
  private Map<CanonicalCode, Bijection<Label, String>> codeLabelAliases = Map
      .empty();
  private Bijection<Link, String> codeAliases2 = Bijection.empty();
  private Bijection<CanonicalCode, String> codeAliases = Bijection.empty();
  private Bijection<CanonicalRelation, String> relationAliases = Bijection
      .empty();
  private View mainLayout;
  private ViewGroup codeEditorContainer;
  private ViewGroup relationEditorContainer;
  private Optional<Pair<F<Unit, Bundle>, F<Pair<Code2, Map<З2Bytes, Code2>>, Unit>>> codeEditor =
      nothing();
  private Optional<Pair<F<Unit, Bundle>, F<Relation, Unit>>> relationEditor =
      nothing();
  private boolean recentCodesVisible;
  private boolean recentVisible = true;
  private F<Unit, Unit> addToRunArea;
  private F<Unit, Bundle> getRunAreaState;
  private boolean editingColors;
  private int previousOrientation;
  private Map<З2Bytes, Code2> rootCodes = Map.empty();

  Resolver r = new Resolver() {
    public Optional<Code2> resolve(З2Bytes hash) {
      return rootCodes.get(hash);
    }
  };

  CodeLabelAliasMap2 codeLabelAliasMap2 = new CodeLabelAliasMap2() {
    public boolean setAlias(Link link, Label l, String alias) {
      Optional<Bijection<Label, String>> o = codeLabelAliases2.get(link);
      if (o.isNothing())
        codeLabelAliases2 =
            codeLabelAliases2.put(link,
                Bijection.<Label, String> empty().putX(l, alias).some().x);
      else {
        Optional<Bijection<Label, String>> oo = o.some().x.putX(l, alias);
        if (oo.isNothing())
          return false;
        codeLabelAliases2 = codeLabelAliases2.put(link, oo.some().x);
      }
      return true;
    }

    public void deleteAlias(Link link, Label l) {
      Optional<Bijection<Label, String>> o = codeLabelAliases2.get(link);
      if (!o.isNothing())
        codeLabelAliases2 = codeLabelAliases2.put(link, o.some().x.deleteX(l));
    }

    public Bijection<Label, String> getAliases(Link link) {
      Optional<Bijection<Label, String>> o = codeLabelAliases2.get(link);
      if (o.isNothing())
        return Bijection.empty();
      return o.some().x;
    }

    public void setAliases(Link link, Bijection<Label, String> aliases) {
      codeLabelAliases2 = codeLabelAliases2.put(link, aliases);
    }
  };

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
    relationViewColors = DB.getRelationColors(getBaseContext());
    this.getActionBar().hide();
    setContentView(R.layout.activity_main);
    mainLayout = findViewById(R.id.main_layout);
    codeEditorContainer = (ViewGroup) findViewById(R.id.container_code_editor);
    relationEditorContainer =
        (ViewGroup) findViewById(R.id.container_relation_editor);

    findViewById(R.id.button_new_code).setOnClickListener(
        $ -> startCodeEditor(CodeUtils.unit2));

    findViewById(R.id.button_new_relation).setOnClickListener($ -> {
      if (recentVisible)
        startRelationEditor(RelationUtils.unit_unit);
      else
        addToRunArea.f(unit());
    });

    Bundle codeEditorState = null;
    Bundle relationEditorState = null;
    Optional<Bundle> runAreaState = nothing();
    if (b != null) {
      codes = (HashSet<Code2>) b.get(STATE_CODES);
      recentCodes2 = (List<Code2>) b.get(STATE_RECENT_CODES2);
      recentCodes = (List<Code>) b.get(STATE_RECENT_CODES);
      relations = (HashSet<Relation>) b.get(STATE_RELATIONS);
      recentRelations = (List<Relation>) b.get(STATE_RECENT_RELATIONS);
      codeLabelAliases =
          (Map<CanonicalCode, Bijection<Label, String>>) b
              .get(STATE_CODE_LABEL_ALIASES);
      codeLabelAliases2 =
          (Map<Link, Bijection<Label, String>>) b
              .get(STATE_CODE_LABEL_ALIASES2);
      codeAliases2 = (Bijection<Link, String>) b.get(STATE_CODE_ALIASES2);
      codeAliases =
          (Bijection<CanonicalCode, String>) b.get(STATE_CODE_ALIASES);
      relationAliases =
          (Bijection<CanonicalRelation, String>) b.get(STATE_RELATION_ALIASES);
      codeEditorState = b.getBundle(STATE_CODE_EDITOR);
      relationEditorState = b.getBundle(STATE_RELATION_EDITOR);
      recentCodesVisible = b.getBoolean(STATE_RECENT_CODES_VISIBLE);
      recentVisible = b.getBoolean(STATE_RECENT_VISIBLE);
      runAreaState = some(b.getBundle(STATE_RUN_AREA));
      relationViewColors =
          (RelationViewColors) b.getSerializable(STATE_RELATION_VIEW_COLORS);
    }

    initRecentCodes();
    initRecentRelations();
    initRunArea(runAreaState);
    switchRecent();

    ToggleButton recentSwitch = (ToggleButton) findViewById(R.id.recent_switch);
    recentSwitch.setOnClickListener($ -> {
      recentCodesVisible = !recentCodesVisible;
      recentVisible = true;
      switchRecent();
    });

    if (codeEditorState != null) {
      F<Pair<Code2, Map<З2Bytes, Code2>>, Unit> doneListener =
          newCodeEditorDoneListener();
      Pair<View, F<Unit, Bundle>> p =
          CodeEditor2.make(this, codeEditorState, codeLabelAliasMap2,
              codeAliases2, recentCodes2, r, doneListener);
      codeEditor = some(pair(p.y, doneListener));
      mainLayout.setVisibility(View.GONE);
      codeEditorContainer.addView(p.x);
      codeEditorContainer.setVisibility(View.VISIBLE);
    }

    if (relationEditorState != null) {
      F<Relation, Unit> doneListener = newRelationEditorDoneListener();
      Pair<View, Pair<F<Unit, Bundle>, F<Unit, Relation>>> p =
          RelationEditor.make(this, recentCodes, codeLabelAliasMap,
              codeAliases, relationAliases, recentRelations,
              relationViewColors, relationEditorState, doneListener);
      relationEditor = some(pair(p.y.x, doneListener));
      mainLayout.setVisibility(View.GONE);
      relationEditorContainer.addView(p.x);
      relationEditorContainer.setVisibility(View.VISIBLE);
    }

    findViewById(R.id.button_run).setOnClickListener($ -> {
      recentVisible = false;
      switchRecent();
    });
  }

  @Override
  public void onSaveInstanceState(Bundle b) {
    super.onSaveInstanceState(b);
    b.putSerializable(STATE_CODES, codes);
    b.putSerializable(STATE_RECENT_CODES2, recentCodes2);
    b.putSerializable(STATE_RECENT_CODES, recentCodes);
    b.putSerializable(STATE_RELATIONS, relations);
    b.putSerializable(STATE_RECENT_RELATIONS, recentRelations);
    b.putSerializable(STATE_CODE_LABEL_ALIASES, codeLabelAliases);
    b.putSerializable(STATE_CODE_LABEL_ALIASES2, codeLabelAliases2);
    b.putSerializable(STATE_CODE_ALIASES2, codeAliases2);
    b.putSerializable(STATE_CODE_ALIASES, codeAliases);
    b.putSerializable(STATE_RELATION_ALIASES, relationAliases);
    if (!codeEditor.isNothing())
      b.putBundle(STATE_CODE_EDITOR, codeEditor.some().x.x.f(unit()));
    if (!relationEditor.isNothing())
      b.putBundle(STATE_RELATION_EDITOR, relationEditor.some().x.x.f(unit()));
    b.putBoolean(STATE_RECENT_CODES_VISIBLE, recentCodesVisible);
    b.putBoolean(STATE_RECENT_VISIBLE, recentVisible);
    b.putBundle(STATE_RUN_AREA, getRunAreaState.f(unit()));
    b.putSerializable(STATE_RELATION_VIEW_COLORS, relationViewColors);
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

  @Override
  public void onBackPressed() {
    if (editingColors) {
      setRequestedOrientation(previousOrientation);
      recreate();
    } else
      super.onBackPressed();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu m) {
    m.add("colors").setOnMenuItemClickListener($ -> {
      if (editingColors)
        return false;
      editingColors = true;
      codeEditorContainer.removeAllViews();
      codeEditorContainer.setVisibility(View.GONE);
      relationEditorContainer.removeAllViews();
      relationEditorContainer.setVisibility(View.GONE);
      mainLayout.setVisibility(View.GONE);
      previousOrientation = getRequestedOrientation();
      // XXX doing this when in landscape switches to portrait, but then the
      // activity is recreated and since the state for the color chooser isn't
      // saved, the color chooser is lost
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        ViewGroup c = (ViewGroup) findViewById(R.id.container_colors);
        c.setVisibility(View.VISIBLE);
        c.addView(Colors.make(getBaseContext(), relationViewColors, rvc -> {
          DB.saveRelationColors(getBaseContext(), rvc);
          relationViewColors = rvc;
          setRequestedOrientation(previousOrientation);
          recreate();
          return unit();
        }));
        return false;
      });
    return true;
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
    CodeList2.Listener cll = new CodeList2.Listener() {

      public void select(Code2 c) {
        notNull(c);
        startCodeEditor(c);
      }

      public boolean changeAlias(Code2 code, List<Label> path, String alias) {
        notNull(code, alias);
        if (!codeEditor.isNothing())
          throw new RuntimeException(
              "code list tried to change alias while code editor was open");
        Optional<Bijection<Link, String>> o =
            codeAliases2.putX(new Link(hash(code), path), alias);
        if (o.isNothing())
          return false;
        codeAliases2 = o.some().x;
        initRecentCodes();
        initRunArea(some(getRunAreaState.f(unit())));
        return true;
      }
    };
    View cl =
        CodeList2.make(this, cll, recentCodes2, codeLabelAliasMap2,
            codeAliases2, r);
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
        initRunArea(some(getRunAreaState.f(unit())));
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

  private void startCodeEditor(Code2 c) {
    /*
     * Workaround android behavior (can't tell if bug or feature): Without this,
     * a user could create multiple superimposed CodeEditors. He could do this
     * by pressing on two codes in the recent code list at the same time, or by
     * quickly pressing the "new code" button multiple times.
     */
    if (!(codeEditor.isNothing() & relationEditor.isNothing()))
      return;
    F<Pair<Code2, Map<З2Bytes, Code2>>, Unit> doneListener =
        newCodeEditorDoneListener();
    Pair<View, F<Unit, Bundle>> p =
        CodeEditor2.make(this, c, codeLabelAliasMap2, codeAliases2,
            recentCodes2, r, doneListener);
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
    Pair<View, Pair<F<Unit, Bundle>, F<Unit, Relation>>> p =
        RelationEditor.make(this, r, recentCodes, codeLabelAliasMap,
            codeAliases, relationAliases, recentRelations, relationViewColors,
            doneListener);
    relationEditor = some(pair(p.y.x, doneListener));
    mainLayout.setVisibility(View.GONE);
    relationEditorContainer.addView(p.x);
    relationEditorContainer.setVisibility(View.VISIBLE);
  }

  private F<Pair<Code2, Map<З2Bytes, Code2>>, Unit> newCodeEditorDoneListener() {
    return new F<Pair<Code2, Map<З2Bytes, Code2>>, Unit>() {
      public Unit f(Pair<Code2, Map<З2Bytes, Code2>> p) {
        if (codeEditor.isNothing() || this != codeEditor.some().x.y)
          throw new RuntimeException(
              "got \"done editing\" event from non-current code editor");
        notNull(p);
        codeEditorContainer.removeAllViews();
        codeEditorContainer.setVisibility(View.GONE);
        mainLayout.setVisibility(View.VISIBLE);
        if (!codes.contains(p.x))
          recentCodes2 = cons(p.x, recentCodes2);
        codes.add(p.x);
        for (Pair<З2Bytes, Code2> e : iter(p.y.entrySet()))
          rootCodes = rootCodes.put(e.x, e.y);
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
