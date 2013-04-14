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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((code == null) ? 0 : code.hashCode());
    result = prime * result + ((path == null) ? 0 : path.hashCode());
    result = prime * result + ((tag == null) ? 0 : tag.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    CodeRef other = (CodeRef) obj;
    if (code == null) {
      if (other.code != null)
        return false;
    } else if (!code.equals(other.code))
      return false;
    if (path == null) {
      if (other.path != null)
        return false;
    } else if (!path.equals(other.path))
      return false;
    if (tag != other.tag)
      return false;
    return true;
  }
}
