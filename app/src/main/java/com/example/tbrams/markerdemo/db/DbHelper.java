package com.example.tbrams.markerdemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbHelper extends SQLiteOpenHelper {
    public static final String  DB_FILE_NAME = "tour.db";
    public static final int     DB_VERSION=1;

    public DbHelper(Context context) {
        super(context, DB_FILE_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TripTable.SQL_CREATE);
        db.execSQL(WpTable.SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(TripTable.SQL_DELETE);
        db.execSQL(WpTable.SQL_DELETE);
        onCreate(db);
    }

}
