package com.pokemon.kore.ui;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import android.os.Bundle;
import android.os.Parcel;
import android.util.Base64;

// from https://gist.github.com/aprock/2037883
public class BundleUtils {
  public static String serializeBundle(final Bundle bundle) {
    Parcel p = Parcel.obtain();
    p.writeBundle(bundle);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      GZIPOutputStream zos =
          new GZIPOutputStream(new BufferedOutputStream(bos));
      zos.write(p.marshall());
      zos.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    String base64 = Base64.encodeToString(bos.toByteArray(), 0);
    p.recycle();
    return base64;
  }

  public static Bundle deserializeBundle(final String base64) {
    Parcel p = Parcel.obtain();
    try {
      ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      GZIPInputStream zis =
          new GZIPInputStream(
              new ByteArrayInputStream(Base64.decode(base64, 0)));
      int len = 0;
      while ((len = zis.read(buffer)) != -1) {
        byteBuffer.write(buffer, 0, len);
      }
      zis.close();
      p.unmarshall(byteBuffer.toByteArray(), 0, byteBuffer.size());
      p.setDataPosition(0);
      Bundle b = p.readBundle();
      p.recycle();
      return b;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
