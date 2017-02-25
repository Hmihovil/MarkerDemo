package com.example.tbrams.markerdemo.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.tbrams.markerdemo.dbModel.AreaItem;
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

        ContentValues values = areaItem.toContentValues();
        SQLiteDatabase dB;
        dB = openAndGetDb();

        dB.insert(AreaTable.TABLE_NAME, null, values);
        close();

        return areaItem;
    }




    /**
     * This function will erase an area from the Area table.
     *
     * @params: area AreaItem to be deleted
     * @Returns: none
     */
    public void deleteArea(AreaItem areaItem) {

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
     * @param oac       Area definition in OpenAir format
     * @return An AreaItem object
     */
    public AreaItem addFullArea(String name, String ident, int areaType, String areaClass, int fromAlt, int toAlt, String oac) {
        AreaItem areaItem = new AreaItem(null, name, areaType, ident, areaClass, fromAlt, toAlt, oac);
        return createArea(areaItem);
    }



    /**
     * Lookup matching records in the database and return a list of fully populated objects.
     * For each matching area create an object create an object and populate it with database
     * values.
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

                String aId = cursor.getString(cursor.getColumnIndex(AreaTable.COLUMN_ID));
                String aName = cursor.getString(cursor.getColumnIndex(AreaTable.COLUMN_NAME));
                int aType = cursor.getInt(cursor.getColumnIndex(AreaTable.COLUMN_TYPE));
                String aIdent = cursor.getString(cursor.getColumnIndex(AreaTable.COLUMN_IDENT));
                String aClass = cursor.getString(cursor.getColumnIndex(AreaTable.COLUMN_CLASS));
                int aFrom = cursor.getInt(cursor.getColumnIndex(AreaTable.COLUMN_FROM_ALT));
                int aTo = cursor.getInt(cursor.getColumnIndex(AreaTable.COLUMN_TO_ALT));
                String aDefinition = cursor.getString(cursor.getColumnIndex(AreaTable.COLUMN_OAC));

                AreaItem areaItem = new AreaItem(aId, aName, aType, aIdent, aClass, aFrom, aTo, aDefinition);
                areaList.add(areaItem);
            }
        }
        cursor.close();
        close();

        return areaList;
    }

}