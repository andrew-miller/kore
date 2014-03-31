package com.example.kore.ui;

import static com.example.kore.utils.Boom.boom;
import static com.example.kore.utils.CodeUtils.followPath;
import static com.example.kore.utils.ListUtils.append;
import static com.example.kore.utils.ListUtils.iter;
import static com.example.kore.utils.OptionalUtils.nothing;
import static com.example.kore.utils.OptionalUtils.some;

import com.example.kore.codes.CanonicalCode;
import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.kore.codes.Pattern;
import com.example.kore.utils.List;
import com.example.kore.utils.Map;
import com.example.kore.utils.Map.Entry;
import com.example.kore.utils.Optional;

public class PatternUtils {
  public static final Pattern emptyPattern =
      new Pattern(Map.<Label, Pattern> empty());

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
    List<Entry<Label, Pattern>> es = pattern.fields.entrySet();
    if (es.isEmpty())
      return start + end;
    CanonicalCode cc = new CanonicalCode(root, path);
    String s;
    {
      Entry<Label, Pattern> e = es.cons().x;
      Optional<String> a = codeLabelAliases.getAliases(cc).get(e.k);
      s =
          start + (a.isNothing() ? e.k : a.some().x) + " "
              + renderPattern(e.v, root, append(e.k, path), codeLabelAliases);
    }
    if (es.cons().tail.isEmpty())
      return s + end;
    for (Entry<Label, Pattern> e : iter(es.cons().tail)) {
      Optional<String> a = codeLabelAliases.getAliases(cc).get(e.k);
      s +=
          "," + (a.isNothing() ? e.k : a.some().x) + " "
              + renderPattern(e.v, root, append(e.k, path), codeLabelAliases);
    }
    return s + end;
  }
}
