package com.example.tbrams.markerdemo.db;

public class WpTable {

    public static final String TABLE_NAME = "wps";

    public static final String COLUMN_ID   = "wpId";
    public static final String COLUMN_NAME = "wpName";
    public static final String COLUMN_LAT = "wpLat";
    public static final String COLUMN_LON = "wpLon";
    public static final String COLUMN_DIST = "wpDistance";
    public static final String COLUMN_ALT = "wpAltitude";
    public static final String COLUMN_TRIP_ID = "wpTripId";
    public static final String COLUMN_SEQUENCE_NUMBER = "wpSequenceNumber";


    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_LAT, COLUMN_LON, COLUMN_DIST, COLUMN_ALT, COLUMN_TRIP_ID, COLUMN_SEQUENCE_NUMBER};

    public static final String SQL_CREATE =
            "CREATE TABLE  IF NOT EXISTS " + TABLE_NAME + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_LAT  + " REAL," +
                    COLUMN_LON  + " REAL," +
                    COLUMN_DIST + " REAL," +
                    COLUMN_ALT  + " INTEGER," +
                    COLUMN_TRIP_ID + " TEXT," +
                    COLUMN_SEQUENCE_NUMBER + " INTEGER" + ");";

    public static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
