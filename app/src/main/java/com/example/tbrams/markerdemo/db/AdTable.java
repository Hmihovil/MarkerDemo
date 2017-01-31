package com.example.tbrams.markerdemo.db;


public class AdTable {
    public static final String TABLE_NAME = "aerodromes";

    public static final String COLUMN_ID   = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_ICAO = "icao";
    public static final String COLUMN_TYPE = "adtype";
    public static final String COLUMN_LAT  = "lat";
    public static final String COLUMN_LON  = "lon";
    public static final String COLUMN_RADIO= "radio";
    public static final String COLUMN_FREQ = "freq";
    public static final String COLUMN_PPR  = "ppr";
    public static final String COLUMN_WEB  = "web";
    public static final String COLUMN_PHONE= "phone";
    public static final String COLUMN_ACTIVITY="adactivity";
    public static final String COLUMN_REMARKS = "remarks";
    public static final String COLUMN_LINK = "link";



    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_ICAO, COLUMN_TYPE,
            COLUMN_LAT, COLUMN_LON, COLUMN_RADIO, COLUMN_FREQ, COLUMN_PPR, COLUMN_WEB, COLUMN_PHONE, COLUMN_ACTIVITY, COLUMN_REMARKS, COLUMN_LINK};

    public static final String SQL_CREATE =
            "CREATE TABLE  IF NOT EXISTS " + TABLE_NAME + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_ICAO + " TEXT," +
                    COLUMN_TYPE + " INT," +
                    COLUMN_LAT + " REAL," +
                    COLUMN_LON + " REAL," +
                    COLUMN_RADIO + " TEXT," +
                    COLUMN_FREQ + " TEXT," +
                    COLUMN_PPR + " INT," +
                    COLUMN_WEB + " TEXT," +
                    COLUMN_PHONE + " TEXT," +
                    COLUMN_LINK + " TEXT," +
                    COLUMN_ACTIVITY + " TEXT," +
                    COLUMN_REMARKS + " TEXT" +
                    ");";

    public static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
