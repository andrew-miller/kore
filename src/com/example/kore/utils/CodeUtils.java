package com.example.kore.utils;

import java.util.HashMap;
import java.util.Map;

import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.unsuck.Boom;

public final class CodeUtils {

  /**
   * Unit Type, i.e., {}
   */
  public final static Code unit = Code.newProduct(new HashMap<Label, Code>());

  /* what does it do ? */
  public static String renderCode(Code cr, Map<Label, String> labelAliases,
      Integer depth) {
    if (depth < 0)
      throw new RuntimeException("negative depth");
    if (depth == 0)
      return "...";
    if (depth != null)
      depth--;
    // if (cr.tag == CodeRef.Tag.PATH)
    // return "^";
    // Code c = cr.code;
    Code c = cr;
    String start;
    String end;
    switch (c.node.tag) {
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
    for (Label l : c.edges.keySet()) {
      if (result.equals(""))
        result = "'";
      else
        result += ", '";
      String la = labelAliases.get(l);
      String ls = la == null ? l.label : la;
      result += ls + " " + renderCode(c.edges.get(l), labelAliases, depth);
    }
    return start + result + end;
  }

}
