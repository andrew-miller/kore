package com.pokemon.kore.utils;

import java.security.SecureRandom;

public class Random {
  private static final SecureRandom sr = new SecureRandom();

  private static char[] m = new char[] { '0', '1', '2', '3', '4', '5', '6',
      '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

  public static String randomId() {
    byte[] bs = new byte[32];
    synchronized (sr) {
      sr.nextBytes(bs);
    }
    String s = "";
    for (byte b : bs) {
      int i = b & 255;
      s += m[i >> 4] + "" + m[i & 0x0f];
    }
    return s;
  }
}
