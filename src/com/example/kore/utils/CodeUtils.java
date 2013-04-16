package com.example.kore.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.kore.codes.Code;
import com.example.kore.codes.CodeOrPath;
import com.example.kore.codes.Label;
import com.example.unsuck.Boom;

public final class CodeUtils {

  public final static Code unit = Code
      .newProduct(new HashMap<Label, CodeOrPath>());

  public static String renderCode(CodeOrPath cr, Map<Label, String> labelAliases,
      Map<Code, String> codeAliases, int depth) {
    if (depth < 0)
      throw new RuntimeException("negative depth");
    if (depth == 0)
      return "...";
    if (cr.tag == CodeOrPath.Tag.PATH)
      return "^";
    Code c = cr.code;
    String codeAlias = codeAliases.get(c);
    if (codeAlias != null)
      return codeAlias;
    String start;
    String end;
    switch (c.tag) {
    case UNION:
      start = "[";
      end = "]";
      break;
    case PRODUCT:
      start = "{";
      end = "}";
      break;
    default:
      throw Boom.boom();
    }
    String result = "";
    for (Label l : c.labels.keySet()) {
      if (result.equals(""))
        result = "'";
      else
        result += ", '";
      String la = labelAliases.get(l);
      String ls = la == null ? l.label : la;
      result +=
          (ls + " " + renderCode(c.labels.get(l), labelAliases, codeAliases,
              depth - 1));
    }
    return start + result + end;
  }

  public static Code followPath(List<Label> path, Code c) {
    for (Label l : path) {
      CodeOrPath cr = c.labels.get(l);
      if (cr == null)
        return null;
      if (cr.tag != CodeOrPath.Tag.CODE)
        return null;
      c = cr.code;
    }
    return c;
  }
}
