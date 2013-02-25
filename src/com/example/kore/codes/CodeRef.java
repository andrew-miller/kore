package com.example.kore.codes;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.example.unsuck.Null;

public final class CodeRef implements Serializable {
  private static final long serialVersionUID = 1L;

  public enum Tag {
    CODE, PATH;
  }

  public final Tag tag;
  public final Code code;
  public final List<Label> path;

  private CodeRef(Tag tag, Code code, List<Label> path) {
    this.tag = tag;
    this.code = code;
    this.path = path;
  }

  public static CodeRef newCode(Code code) {
    Null.notNull(code);
    return new CodeRef(Tag.CODE, code, null);
  }

  public static CodeRef newPath(List<Label> path) {
    Null.notNull(path);
    return new CodeRef(Tag.PATH, null, Collections.unmodifiableList(path));

  }
}
