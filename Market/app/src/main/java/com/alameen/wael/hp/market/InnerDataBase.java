package com.alameen.wael.hp.market;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class InnerDataBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "sales.db";
    private static final String TABLE_NAME = "sales_table";
    Context context;

    InnerDataBase(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE "+TABLE_NAME+" (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, trader TEXT, price TEXT, num TEXT, color TEXT, " +
                "size TEXT, latitude TEXT, longitude TEXT, address TEXT, phone TEXT, cost TEXT, date TEXT)";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    boolean insert(String name, String trader, String price, String number, String color, String size, String lat, String longi, String address, String phone,
                String cost, String currentDate) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("trader", trader);
        contentValues.put("price", price);
        contentValues.put("num", number);
        contentValues.put("color", color);
        contentValues.put("size", size);
        contentValues.put("latitude", lat);
        contentValues.put("longitude", longi);
        contentValues.put("address", address);
        contentValues.put("phone", phone);
        contentValues.put("cost", cost);
        contentValues.put("date", currentDate);

        long res = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        return res != -1;
    }

    Cursor showData() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT name, price, num FROM "+TABLE_NAME;
        return sqLiteDatabase.rawQuery(query, null);
    }

    Cursor showAllData() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "SELECT name, trader, price, num, color, size, latitude, longitude, address, phone, cost, date FROM "+TABLE_NAME;
        return sqLiteDatabase.rawQuery(query, null);
    }

    void deleteRows() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        String query = "DELETE FROM "+TABLE_NAME;
        sqLiteDatabase.execSQL(query);
    }
}
