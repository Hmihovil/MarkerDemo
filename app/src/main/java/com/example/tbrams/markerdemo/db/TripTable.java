package com.example.tbrams.markerdemo.db;

public class TripTable {
    public static final String TABLE_NAME = "trips";

    public static final String COLUMN_ID   = "tripId";
    public static final String COLUMN_NAME = "tripName";
    public static final String COLUMN_DIST = "tripDistance";
    public static final String COLUMN_DATE = "tripDate";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_DIST, COLUMN_DATE};

    public static final String SQL_CREATE =
            "CREATE TABLE  IF NOT EXISTS " + TABLE_NAME + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_DIST + " REAL," +
                    COLUMN_DATE + " TEXT" + ");";

    public static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
