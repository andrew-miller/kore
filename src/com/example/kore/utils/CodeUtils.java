package com.example.kore.utils;

import com.example.kore.codes.Code;
import com.example.kore.codes.CodeRef;
import com.example.kore.codes.Label;
import com.example.kore.codes.LabelOrd;
import com.example.unsuck.Boom;

import fj.F2;
import fj.P2;
import fj.data.List;
import fj.data.Option;
import fj.data.TreeMap;

public final class CodeUtils {

  public final static Code unit = Code.newProduct(TreeMap
      .<Label, CodeRef> empty((LabelOrd.ord())));

  public static String renderCode(CodeRef cr,
      TreeMap<Label, String> labelAliases, int depth) {
    if (depth < 0)
      throw new RuntimeException("negative depth");
    if (depth == 0)
      return "...";
    if (cr.tag == CodeRef.Tag.PATH)
      return "^";
    Code c = cr.code;
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
    for (P2<Label, CodeRef> p : c.labels) {
      if (result.equals(""))
        result = "'";
      else
        result += ", '";
      Option<String> ola = labelAliases.get(p._1());
      String ls = ola.isNone() ? p._1().label : ola.some();
      result += (ls + " " + renderCode(p._2(), labelAliases, depth - 1));
    }
    return start + result + end;
  }

  public static Code followPath(List<Label> p, Code c) {
    return p.foldLeft(new F2<Code, Label, Code>() {
      @Override
      public Code f(Code c, Label l) {
        Option<CodeRef> ocr = c.labels.get(l);
        if (ocr.isNone())
          return null;
        CodeRef cr = ocr.some();
        if (cr.tag != CodeRef.Tag.CODE)
          return null;
        return cr.code;
      }
    }, c);
  }
}
