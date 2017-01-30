package com.example.tbrams.markerdemo.db;


import static com.example.tbrams.markerdemo.db.AdTable.COLUMN_ICAO;

public class RPTable {
    public static final String TABLE_NAME = "RepPoints";

    public static final String COLUMN_ID   = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AD = "ad";
    public static final String COLUMN_LAT  = "lat";
    public static final String COLUMN_LON  = "lon";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_ICAO, COLUMN_NAME, COLUMN_LAT, COLUMN_LON};

    public static final String SQL_CREATE =
            "CREATE TABLE  IF NOT EXISTS " + TABLE_NAME + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_ICAO + " TEXT," +
                    COLUMN_LAT + " REAL," +
                    COLUMN_LON + " REAL" +
                    ");";

    public static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
