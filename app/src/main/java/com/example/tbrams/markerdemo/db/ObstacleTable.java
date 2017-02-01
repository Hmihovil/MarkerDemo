package com.example.tbrams.markerdemo.db;


public class ObstacleTable {
    public static final String TABLE_NAME = "Obstacles";

    public static final String COLUMN_ID   = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_WHAT = "what";
    public static final String COLUMN_LAT  = "lat";
    public static final String COLUMN_LON  = "lon";
    public static final String COLUMN_ELEVATION  = "elevation";
    public static final String COLUMN_HEIGHT  = "height";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_WHAT, COLUMN_LAT, COLUMN_LON, COLUMN_ELEVATION, COLUMN_HEIGHT};

    public static final String SQL_CREATE =
            "CREATE TABLE  IF NOT EXISTS " + TABLE_NAME + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_WHAT + " TEXT," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_LAT + " REAL," +
                    COLUMN_LON + " REAL," +
                    COLUMN_ELEVATION + " INT," +
                    COLUMN_HEIGHT + " INT" +
                    ");";

    public static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
