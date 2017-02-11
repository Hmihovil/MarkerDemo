package com.example.tbrams.markerdemo.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.tbrams.markerdemo.dbModel.AreaItem;
import com.example.tbrams.markerdemo.dbModel.CoordItem;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class DataSourceArea extends DataSource {
    private static final String TAG = "TBR:DataSourceArea";


    private Context mContext;
    private SQLiteDatabase mDb;


    public DataSourceArea(Context context) {
        super(context);

        mContext = context;
    }


    /**
     * createArea
     * This is the function responsible for inserting data into the Area Table.  It does that
     * by creating ContentValues for all object attributes and then pass the entire thing to
     * the DB helper insert function.
     * <p>
     * Will be returning the same object in case we need to check if something has been changed.
     *
     * @Args: area Object
     * @Return: area object
     */

    public static AreaItem createArea(AreaItem areaItem) {

        // First make sure we get all the coordinates saved to the database
        for (CoordItem coordItem: areaItem.getCoordItemList()) {
            createCoord(coordItem);
        }

        // Then add the AreaItem to the database as well
        ContentValues values = areaItem.toContentValues();
        SQLiteDatabase dB;
        dB = openAndGetDb();

        dB.insert(AreaTable.TABLE_NAME, null, values);
        close();
        return areaItem;
    }




    /**
     * This function will erase an area from the Area table. And
     * also the associated coordinates from the Coords table
     *
     * @params: area AreaItem to be deleted
     * @Returns: none
     */
    public void deleteArea(AreaItem areaItem) {
        // Get a list of all associated coords
        List<CoordItem> coordItemList = getAllCoords(areaItem.getAreaId());

        // delete them one by one and do not waste time on re-calculating trip distance
        for (CoordItem coordItem : coordItemList) {
            deleteCoord(coordItem);
        }

        // Finally delete the AreaItem from the Area Table
        SQLiteDatabase dB = openAndGetDb();
        dB.delete(AreaTable.TABLE_NAME, AreaTable.COLUMN_ID + " = ?",
                new String[]{areaItem.getAreaId()});
        close();
    }


    /**
     * Insert all Area objects into the area table in the database.
     *
     * @param areaItemList : List of AreaItem objects
     * @Return none
     */
    public void seedAreaTable(List<AreaItem> areaItemList) {
        if (areaItemList.size() > 0) {
            for (AreaItem areaItem : areaItemList) {
                try {
                    createArea(areaItem);

                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /**
     * A super constructor populating both the Area Table and the Coords table with all points.
     *
     * @param name      Area Name
     * @param ident     Area Ident
     * @param areaType  Area Type
     * @param areaClass Airspace class
     * @param fromAlt   Lowest altitude
     * @param toAlt     Highest Altitude
     * @param coordList A list of LatLng objects - one for each point on the polygon of the area
     * @return An AreaItem object
     */
    public AreaItem addFullArea(String name, String ident, int areaType, String areaClass, int fromAlt, int toAlt, List<LatLng> coordList) {
        AreaItem areaItem = new AreaItem(null, name, areaType, ident, areaClass, fromAlt, toAlt);
        for (int i = 0; i < coordList.size(); i++) {

            LatLng location = coordList.get(i);

            // Create a new object and write it do the database
            CoordItem coordItem = new CoordItem(null, areaItem.getAreaId(), location, i);
            createCoord(coordItem);
        }

        return createArea(areaItem);
    }



    /**
     * Lookup matching records in the database and return a list of fully populated objects.
     * For each matching area create an object create an object and populate it with database
     * values. Finally find all polygon coordinates from the database and add that to the object
     * before returning a list with all of these as the result.
     *
     * @param areaType Integer, a filter - if this is null all rows are returned from db, otherwise only
     *                 areas with a areaType matching the filter is returned.
     * @Return A list of AreaItem objects
     */
    public static List<AreaItem> getAllAreas(Integer areaType) {
        List<AreaItem> areaList = new ArrayList<>();
        Cursor cursor;

        SQLiteDatabase dB = openAndGetDb();
        if (areaType == null) {
            cursor = dB.query(AreaTable.TABLE_NAME, AreaTable.ALL_COLUMNS, null, null, null, null, AreaTable.COLUMN_NAME);
        } else {
            String[] areaTypes = {areaType.toString()};
            cursor = dB.query(AreaTable.TABLE_NAME, AreaTable.ALL_COLUMNS, AreaTable.COLUMN_TYPE + "=?", areaTypes, null, null, AreaTable.COLUMN_NAME);
        }

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                AreaItem areaItem = new AreaItem();
                areaItem.setAreaId(cursor.getString(cursor.getColumnIndex(AreaTable.COLUMN_ID)));
                areaItem.setAreaName(cursor.getString(cursor.getColumnIndex(AreaTable.COLUMN_NAME)));
                areaItem.setAreaClass(cursor.getString(cursor.getColumnIndex(AreaTable.COLUMN_CLASS)));
                areaItem.setAreaIdent(cursor.getString(cursor.getColumnIndex(AreaTable.COLUMN_IDENT)));
                areaItem.setAreaType(cursor.getInt(cursor.getColumnIndex(AreaTable.COLUMN_TYPE)));
                areaItem.setAreaFromAlt(cursor.getInt(cursor.getColumnIndex(AreaTable.COLUMN_FROM_ALT)));
                areaItem.setAreaToAlt(cursor.getInt(cursor.getColumnIndex(AreaTable.COLUMN_TO_ALT)));
                areaList.add(areaItem);
            }
        }
        cursor.close();
        close();

        // Populate the polygon for each of these areas by looping through the Coords table
        List<CoordItem> coordList = new ArrayList<>();
        for (AreaItem areaItem : areaList) {
            for (CoordItem coordItem : getAllCoords(areaItem.getAreaId())) {
                coordList.add(coordItem);
            }
            areaItem.setCoordItemList(coordList);
            coordList = new ArrayList<>();
        }

        return areaList;
    }


    //
    // Coord Table specifics
    // --------------------

    /**
     * This is the function responsible for inserting data into the coord Table.
     * Will be returning the same object in case we need to check if something has been changed.
     *
     * @param coordItem CoordItem Object
     * @Return CoordItem object
     */
    public static CoordItem createCoord(CoordItem coordItem) {
        ContentValues values = coordItem.toContentValues();

        SQLiteDatabase dB = openAndGetDb();
        dB.insert(CoordTable.TABLE_NAME, null, values);
        close();
        return coordItem;
    }


    /**
     * This function will erase a coordinate from the Coords table.
     *
     * @param coordItem The CoordItem to be deleted
     * @Return none
     */
    public void deleteCoord(CoordItem coordItem) {

        SQLiteDatabase dB = openAndGetDb();
        // Re-sequence the subsequent Coordinates
        String sql = "UPDATE " + CoordTable.TABLE_NAME + " SET " + CoordTable.COLUMN_SEQ + "=" + CoordTable.COLUMN_SEQ + "-1"
                + " WHERE " + CoordTable.COLUMN_SEQ + " > (SELECT " + CoordTable.COLUMN_SEQ + " FROM " + CoordTable.TABLE_NAME + " WHERE " +
                CoordTable.COLUMN_ID + "=?)";
        dB.execSQL(sql, new String[]{coordItem.getCoordId()});

        // Now delete the way point
        dB.delete(CoordTable.TABLE_NAME, CoordTable.COLUMN_ID + " = ?", new String[]{coordItem.getCoordId()});
        close();
    }


    /**
     * Return the number of rows in the Coord Table
     *
     * @Return long
     */
    public long getCoordCount() {
        long numberEntries;
        SQLiteDatabase dB = openAndGetDb();
        numberEntries = DatabaseUtils.queryNumEntries(dB, CoordTable.TABLE_NAME);
        close();

        return numberEntries;
    }


    /**
     * Insert all CoordItem objects into the Coord table in the database.
     *
     * @param coordList of CoordItems
     * @Return none
     */
    public void seedCoordTable(List<CoordItem> coordList) {
        if (coordList.size() > 0) {
            for (CoordItem coordItem : coordList) {
                try {
                    createCoord(coordItem);
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /**
     * Lookup matching records in the database and for each create a matching object and return
     * a list with all of these as the result.
     *
     * @param area_id, String, a filter - if this is null all rows are returned from db, otherwise only
     *                 CoordItems with a matching area ID are returned.
     * @Return A list of CoordItem objects
     */
    public static List<CoordItem> getAllCoords(String area_id) {
        List<CoordItem> coordItemList = new ArrayList<>();
        Cursor cursor;

        SQLiteDatabase dB = openAndGetDb();

        if (area_id == null) {
            cursor = dB.query(CoordTable.TABLE_NAME, CoordTable.ALL_COLUMNS, null, null, null, null, null);
        } else {
            String filterQuery = "SELECT  * FROM " + CoordTable.TABLE_NAME + " WHERE " + CoordTable.COLUMN_AREA_ID
                    + "=? ORDER BY " + CoordTable.COLUMN_SEQ + " ASC";
            Log.d(TAG, "Query: " + filterQuery);
            cursor = dB.rawQuery(filterQuery, new String[]{area_id});
        }

        while (cursor.moveToNext()) {
            CoordItem coordItem = new CoordItem();

            coordItem.setCoordId(cursor.getString(cursor.getColumnIndex(CoordTable.COLUMN_ID)));
            coordItem.setAreaId(cursor.getString(cursor.getColumnIndex(CoordTable.COLUMN_AREA_ID)));
            coordItem.setCoordNumber(cursor.getInt(cursor.getColumnIndex(CoordTable.COLUMN_SEQ)));
            coordItem.setCoordPosition(
                    new LatLng(cursor.getDouble(cursor.getColumnIndex(CoordTable.COLUMN_LAT)),
                            cursor.getDouble(cursor.getColumnIndex(CoordTable.COLUMN_LON))));
            coordItemList.add(coordItem);
        }
        cursor.close();
        close();

        return coordItemList;
    }

}