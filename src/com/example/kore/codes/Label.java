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
}
