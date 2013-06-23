package com.example.kore.codes;

import static com.example.kore.utils.Null.notNull;

import java.io.Serializable;

import com.example.kore.utils.List;

public final class CodeOrPath implements Serializable {
  public enum Tag {
    CODE, PATH;
  }

  public final Tag tag;
  public final Code code;
  public final List<Label> path;

  @Override
  public String toString() {
    return "CodeOrPath [tag=" + tag + ", code=" + code + ", path=" + path + "]";
  }

  private CodeOrPath(Tag tag, Code code, List<Label> path) {
    this.tag = tag;
    this.code = code;
    this.path = path;
  }

  public static CodeOrPath newCode(Code code) {
    notNull(code);
    return new CodeOrPath(Tag.CODE, code, null);
  }

  public static CodeOrPath newPath(List<Label> path) {
    notNull(path);
    return new CodeOrPath(Tag.PATH, null, path);
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
    CodeOrPath other = (CodeOrPath) obj;
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
