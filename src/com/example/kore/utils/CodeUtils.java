package com.example.kore.utils;

import java.util.HashMap;
import java.util.Map;

import com.example.kore.codes.Code;
import com.example.kore.codes.Label;
import com.example.unsuck.Boom;

public class CodeUtils {

  public final static Code unit = Code.newProduct(new HashMap<Label, Code>());

  public static Map<Label, Code> getLabels(Code code) {
    switch (code.tag) {
    case UNION:
      return code.getUnion().labels;
    case PRODUCT:
      return code.getProduct().labels;
    default:
      throw Boom.boom();
    }
  }

  public static Code replaceLabels(Code c, Map<Label, Code> m) {
    switch (c.tag) {
    case PRODUCT:
      return Code.newProduct(m);
    case UNION:
      return Code.newUnion(m);
    default:
      throw Boom.boom();
    }
  }

}
