package com.pokemon.kore.ui;

import static com.pokemon.kore.utils.PairUtils.pair;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pokemon.kore.codes.Relation.Tag;
import com.pokemon.kore.utils.Map;
import com.pokemon.kore.utils.Pair;

public class DB {
  private static class FuckYou extends SQLiteOpenHelper {
    public FuckYou(Context context) {
      super(context, "db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE relation_colors (it BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
    }
  }

  public static void
      saveRelationColors(Context context, RelationViewColors rvc) {
    SQLiteDatabase db = new FuckYou(context).getWritableDatabase();
    try {
      db.beginTransaction();
      try {
        db.delete("relation_colors", null, null);
        ContentValues cv = new ContentValues();
        cv.put("it", SerializationUtils.serialize(rvc));
        if (db.insert("relation_colors", null, cv) == -1)
          throw new RuntimeException("insert failed");
        db.setTransactionSuccessful();
      } finally {
        db.endTransaction();
      }
    } finally {
      db.close();
    }
  }

  public static RelationViewColors getRelationColors(Context context) {
    SQLiteDatabase db = new FuckYou(context).getReadableDatabase();
    try {
      Cursor c = db.rawQuery("SELECT it FROM relation_colors", null);
      try {
        if (c.getCount() == 0) {
          RelationColors relationColors =
              new RelationColors(Map.<Tag, Pair<Integer, Integer>> empty()
                  .put(Tag.ABSTRACTION, pair(0xFFAA00AA, 0xFFFF00FF))
                  .put(Tag.COMPOSITION, pair(0xFF0000AA, 0xFF0000FF))
                  .put(Tag.LABEL, pair(0xFF00AAAA, 0xFF00FFFF))
                  .put(Tag.PRODUCT, pair(0xFF00AAAA, 0xFF00FFFF))
                  .put(Tag.PROJECTION, pair(0xFFAA0000, 0xFFFF0000))
                  .put(Tag.UNION, pair(0xFF00AA00, 0xFF00FF00)));
          return new RelationViewColors(relationColors, 0xFFAAAAFF, 0xFFCCCCCC,
              pair(0xFF000000, 0xFF444444));
        }
        c.moveToFirst();
        return (RelationViewColors) SerializationUtils
            .deserialize(c.getBlob(0));
      } finally {
        c.close();
      }
    } finally {
      db.close();
    }
  }
}
