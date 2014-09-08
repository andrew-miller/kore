package com.pokemon.kore.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SerializationUtils {
  public static byte[] serialize(Serializable s) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(s);
      oos.flush();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return bos.toByteArray();
  }

  public static Object deserialize(byte[] bs) {
    try {
      ObjectInputStream ois =
          new ObjectInputStream(new ByteArrayInputStream(bs));
      return ois.readObject();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
