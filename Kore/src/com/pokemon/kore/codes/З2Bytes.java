package com.pokemon.kore.codes;

public final class З2Bytes {
  private final byte[] bytes;

  public З2Bytes(byte[] bytes) {
    if (bytes.length != 32)
      throw new RuntimeException("wrong size");
    this.bytes = bytes.clone();
  }

  public byte[] getBytes() {
    return bytes.clone();
  }
}
