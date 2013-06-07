package com.example.kore.codes;

import java.io.Serializable;

public final class Label implements Serializable {
  private static final long serialVersionUID = 1L;
  public final String label;

  public Label(String label) {
    this.label = label;
  }

  @Override
  public String toString() {
    return label;
  }

  @Override
  public int hashCode() {
    return label.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Label))
      return false;
    return label.equals(((Label) o).label);
  }
}
