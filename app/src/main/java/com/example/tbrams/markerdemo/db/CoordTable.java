package com.example.tbrams.markerdemo.db;

public class CoordTable {
    public static final String TABLE_NAME = "area_coords";

    public static final String COLUMN_ID = "CoordId";
    public static final String COLUMN_AREA_ID = "AreaId";
    public static final String COLUMN_LAT = "Lat";
    public static final String COLUMN_LON = "Lon";
    public static final String COLUMN_SEQ = "Seq";


    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_AREA_ID, COLUMN_LAT, COLUMN_LON, COLUMN_SEQ};

    public static final String SQL_CREATE =
            "CREATE TABLE  IF NOT EXISTS " + TABLE_NAME + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_AREA_ID + " TEXT," +
                    COLUMN_LAT + " REAL," +
                    COLUMN_LON + " REAL" +
                    ");";

    public static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

}
