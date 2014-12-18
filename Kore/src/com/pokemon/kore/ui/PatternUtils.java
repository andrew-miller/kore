package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.Boom.boom;
import static com.pokemon.kore.utils.CodeUtils.child;
import static com.pokemon.kore.utils.CodeUtils.followPath;
import static com.pokemon.kore.utils.CodeUtils.hashLink;
import static com.pokemon.kore.utils.ListUtils.append;
import static com.pokemon.kore.utils.ListUtils.iter;
import static com.pokemon.kore.utils.OptionalUtils.nothing;
import static com.pokemon.kore.utils.OptionalUtils.some;

import com.pokemon.kore.codes.CanonicalCode;
import com.pokemon.kore.codes.Code;
import com.pokemon.kore.codes.Label;
import com.pokemon.kore.codes.Pattern;
import com.pokemon.kore.utils.ICode;
import com.pokemon.kore.utils.List;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Optional;
import com.pokemon.kore.utils.Pair;

public class PatternUtils {
  public static final Pattern emptyPattern = new Pattern(Map.empty());

  public static Optional<Pattern> patternAt(Pattern pattern, List<Label> path) {
    if (path.isEmpty())
      return some(pattern);
    Optional<Pattern> op = pattern.fields.get(path.cons().x);
    if (op.isNothing())
      return nothing();
    return patternAt(op.some().x, path.cons().tail);
  }

  public static Optional<Pattern> replacePatternAt(Pattern pattern,
      List<Label> path, Pattern newPattern) {
    if (path.isEmpty())
      return some(newPattern);
    Optional<Pattern> op = pattern.fields.get(path.cons().x);
    if (op.isNothing())
      return nothing();
    Optional<Pattern> r =
        replacePatternAt(op.some().x, path.cons().tail, newPattern);
    if (r.isNothing())
      return nothing();
    return some(new Pattern(pattern.fields.put(path.cons().x, r.some().x)));
  }

  public static String renderPattern(Pattern pattern, ICode c,
      CodeLabelAliasMap2 codeLabelAliases) {
    String start;
    String end;
    switch (c.tag()) {
    case PRODUCT:
      start = "{";
      end = "}";
      break;
    case UNION:
      start = "[";
      end = "]";
      break;
    default:
      throw boom();
    }
    List<Pair<Label, Pattern>> es = pattern.fields.entrySet();
    if (es.isEmpty())
      return start + end;
    String s;
    {
      Pair<Label, Pattern> e = es.cons().x;
      Optional<String> a =
          codeLabelAliases.getAliases(hashLink(c.link())).xy.get(e.x);
      s =
          start + (a.isNothing() ? e.x : a.some().x) + " "
              + renderPattern(e.y, child(c, e.x), codeLabelAliases);
    }
    if (es.cons().tail.isEmpty())
      return s + end;
    for (Pair<Label, Pattern> e : iter(es.cons().tail)) {
      Optional<String> a =
          codeLabelAliases.getAliases(hashLink(c.link())).xy.get(e.x);
      s +=
          "," + (a.isNothing() ? e.x : a.some().x) + " "
              + renderPattern(e.y, child(c, e.x), codeLabelAliases);
    }
    return s + end;
  }

  public static String renderPattern(Pattern pattern, Code root,
      List<Label> path, CodeLabelAliasMap codeLabelAliases) {
    String start;
    String end;
    Code c = followPath(path, root).some().x;
    switch (c.tag) {
    case PRODUCT:
      start = "{";
      end = "}";
      break;
    case UNION:
      start = "[";
      end = "]";
      break;
    default:
      throw boom();
    }
    List<Pair<Label, Pattern>> es = pattern.fields.entrySet();
    if (es.isEmpty())
      return start + end;
    CanonicalCode cc = new CanonicalCode(root, path);
    String s;
    {
      Pair<Label, Pattern> e = es.cons().x;
      Optional<String> a = codeLabelAliases.getAliases(cc).xy.get(e.x);
      s =
          start + (a.isNothing() ? e.x : a.some().x) + " "
              + renderPattern(e.y, root, append(e.x, path), codeLabelAliases);
    }
    if (es.cons().tail.isEmpty())
      return s + end;
    for (Pair<Label, Pattern> e : iter(es.cons().tail)) {
      Optional<String> a = codeLabelAliases.getAliases(cc).xy.get(e.x);
      s +=
          "," + (a.isNothing() ? e.x : a.some().x) + " "
              + renderPattern(e.y, root, append(e.x, path), codeLabelAliases);
    }
    return s + end;
  }
}
