package com.pokemon.kore;

import junit.framework.TestCase;
import android.util.Log;

public class StackTest extends TestCase {
  private static int f(int x) {
    try {
      return f(x + 1);
    } catch (StackOverflowError e) {
      return x;
    }
  }

  public static void testStack() {
    int x = f(0);
    Log.i("stack size test", "" + x);
  }
}
