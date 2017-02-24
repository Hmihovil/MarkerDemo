package com.example.tbrams.markerdemo.db;

public class AreaTable {
    public static final String TABLE_NAME = "areas";

    public static final String COLUMN_ID = "AreaId";
    public static final String COLUMN_NAME = "AreaName";
    public static final String COLUMN_IDENT = "AreaIdent";
    public static final String COLUMN_CLASS = "AreaClass";
    public static final String COLUMN_TYPE = "AreaType";
    public static final String COLUMN_FROM_ALT = "AreaFrom";
    public static final String COLUMN_TO_ALT = "AreaTo";
    public static final String COLUMN_OAC = "Definition";


    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_NAME, COLUMN_IDENT, COLUMN_TYPE, COLUMN_CLASS, COLUMN_FROM_ALT, COLUMN_TO_ALT, COLUMN_OAC};

    public static final String SQL_CREATE =
            "CREATE TABLE  IF NOT EXISTS " + TABLE_NAME + "(" +
                    COLUMN_ID + " TEXT PRIMARY KEY," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_IDENT + " TEXT," +
                    COLUMN_TYPE + " INT," +
                    COLUMN_CLASS + " TEXT," +
                    COLUMN_FROM_ALT + " INT," +
                    COLUMN_TO_ALT + " INT," +
                    COLUMN_OAC + " TEXT" +
                    ");";

    public static final String SQL_DELETE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;
}
