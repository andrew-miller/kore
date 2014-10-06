package com.pokemon.kore.codes;

import java.io.Serializable;
import java.util.Arrays;

public final class З2Bytes implements Serializable {
  private final byte[] bytes;

  public З2Bytes(byte[] bytes) {
    if (bytes.length != 32)
      throw new RuntimeException("wrong size");
    this.bytes = bytes.clone();
  }

  public byte[] getBytes() {
    return bytes.clone();
  }

  @Override
  public String toString() {
    return "З2Bytes [bytes=" + Arrays.toString(bytes) + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(bytes);
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
    З2Bytes other = (З2Bytes) obj;
    if (!Arrays.equals(bytes, other.bytes))
      return false;
    return true;
  }

}
