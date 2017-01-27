package com.example.tbrams.markerdemo.db;


public class NavAidTable {
    public static final String TABLE_NAME = "navaids";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_IDENT   = "ident";
    public static final String COLUMN_TYPE = "natype";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LON = "lon";
    public static final String COLUMN_FREQ = "freq";
    public static final String COLUMN_ELEV = "elev";
    public static final String COLUMN_MAX_ALT = "maxalt";
    public static final String COLUMN_MAX_DIST = "maxdist";


    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_IDENT, COLUMN_TYPE,
            COLUMN_LAT, COLUMN_LON, COLUMN_FREQ, COLUMN_MAX_ALT, COLUMN_MAX_DIST, COLUMN_ELEV};

    public static final String SQL_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_IDENT + " TEXT," +
                    COLUMN_TYPE + " INT," +
                    COLUMN_LAT + " REAL," +
                    COLUMN_LON + " REAL," +
                    COLUMN_FREQ + " TEXT," +
                    COLUMN_MAX_ALT + " INT," +
                    COLUMN_MAX_DIST + " INT," +
                    COLUMN_ELEV + " REAL" +
                    ");";

    public static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
