package com.example.kore.codes;

import java.util.HashMap;
import java.util.Map;

public class Program {
  public final Map<String, Blob> relMap;
  public final Map<String, Code> codeMap;
  public String main;
  public final Map<String, String> aliasMap;

  public Program() {
    relMap = new HashMap<String, Blob>();
    codeMap = new HashMap<String, Code>();
    aliasMap = new HashMap<String, String>();
  }

  public String[] toStrings() {
    int index = 0;
    String[] result = new String[codeMap.size() + relMap.size()];
    for (String c : codeMap.keySet()) {
      result[index++] = "<code> " + c + " <is> " + codeMap.get(c).toString()
          + ";";
    }
    for (String r : relMap.keySet()) {
      Blob b = relMap.get(r);
      result[index++] = "( " + b.in.toString() + " -> " + b.out.toString()
          + " ) " + r + " <is> " + b.toString() + ";";
    }
    return result;
  }

}
