package com.alameen.wael.hp.market;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class CounterBadgeDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "counter.db";
    private static final String TABLE_NAME = "count_table";

    CounterBadgeDatabase(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE "+TABLE_NAME+" (id INTEGER PRIMARY KEY AUTOINCREMENT, counter INTEGER)";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS"+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    boolean update(int counter) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("counter", counter);
        long res = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        return res != -1;
    }

    Cursor showCounterData() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT counter FROM "+TABLE_NAME;
        return sqLiteDatabase.rawQuery(query, null);
    }
}
