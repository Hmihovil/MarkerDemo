package com.example.tbrams.markerdemo.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.dbModel.TripItem;
import com.example.tbrams.markerdemo.dbModel.WpItem;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataSource {
    private static final String TAG = "TBR:DataSource";


    private Context             mContext;
    private SQLiteDatabase      mDb;
    private SQLiteOpenHelper    mDbOpenHelper;


    /*
     * Generic Database functionality
     * Constructor, open and close methods
     */
    public DataSource(Context context) {
        mContext = context;
        mDbOpenHelper = new DbHelper(context);
        mDb = mDbOpenHelper.getWritableDatabase();
    }



    public void open() {
        mDb = mDbOpenHelper.getWritableDatabase();
    }


    public void close() {
        mDbOpenHelper.close();
    }


    public void resetDB() {
        mDb.execSQL(TripTable.SQL_DELETE);
        mDb.execSQL(TripTable.SQL_CREATE);
        mDb.execSQL(WpTable.SQL_DELETE);
        mDb.execSQL(WpTable.SQL_CREATE);
    }



    public void resetNavAidTable() {
        mDb.execSQL(NavAidTable.SQL_DELETE);
        mDb.execSQL(NavAidTable.SQL_CREATE);
    }



    public void resetAdTable() {
        mDb.execSQL(AdTable.SQL_DELETE);
        mDb.execSQL(AdTable.SQL_CREATE);
    }


    //
    //Trip Table specifics
    // --------------------


    /*
     * createTrip
     * This is the function responsible for inserting data into the Trips Table.  It does that
     * by creating ContentValues for all object attributes and then pass the entire thing to
     * the DB helper insert function.
     *
     * Will be returning the same object in case we need to check if something has been changed.
     *
     * @Args: Trip Object
     *
     * @Return: Trip object
     *
     */

    public TripItem createTrip(TripItem trip) {
        ContentValues values = trip.toContentValues();

        mDb.insert(TripTable.TABLE_NAME, null, values);
        return trip;
    }



    /*
     * deleteTrip
     * This function will erase a trip from the Trip table and
     * also the associated way points from the WP table
     *
     * @params:  TripItem to be deleted
     * @Returns: none
     */
    public void deleteTrip(TripItem trip) {
        // Get a list of all associated WPs
        List<WpItem> allWps = getAllWps(trip.getTripId());

        // delete them one by one and do not waste time on re-calculating trip distance
        for (WpItem wp : allWps) {
            deleteWp(wp, false);
        }

        // Finally delete the trip from the Trip Table
        mDb.delete(TripTable.TABLE_NAME, TripTable.COLUMN_ID + " = ?",
                new String[] { trip.getTripId() });
    }

    /*
     * deleteTrip
     * This function will erase a trip from the Trip table and
     * also the associated way points from the WP table
     *
     * @params:  String  - Trip Id
     * @Returns: none
     */
    public void deleteTrip(String tripId) {
        // Get a list of all associated WPs
        List<WpItem> allWps = getAllWps(tripId);

        // delete them one by one and do not waste time on re-calculating trip distance
        for (WpItem wp : allWps) {
            deleteWp(wp, false);
        }

        // Finally delete the trip from the Trip Table
        mDb.delete(TripTable.TABLE_NAME, TripTable.COLUMN_ID + " = ?",
                new String[] { tripId });
    }


    /*
     * getTripName
     *
     * Lookup and return a String with a single tripname given the tripId as a String
     */
    public String getTripName(String tripId) {
        Cursor cursor = null;
        String tripName = "";
        try{
            cursor = mDb.rawQuery("SELECT tripName FROM Trips WHERE tripId=?", new String[] {tripId});

            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                tripName = cursor.getString(cursor.getColumnIndex("tripName"));
            }

            return tripName;

        } finally {
            cursor.close();
        }
    }

    /*
     * getTripCount
     * Return the number of rows in the Trip Table
     *
     * @Args: None
     *
     * @Return: long
     *
     */
    public long getTripCount() {
        return DatabaseUtils.queryNumEntries(mDb, TripTable.TABLE_NAME);
    }



    /*
     * seedTripTable
     * Insert all Trip objects into the trip table in the database
     *
     * @Args: List of Trip objects
     *
     * @Return: none
     *
     */
    public void seedTripTable(List<TripItem> tripList) {
        if (tripList.size()>0) {
            for (TripItem trip : tripList) {
                try {
                    createTrip(trip);

                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public TripItem addFullTrip(String name, List<WpItem> wps) {
        TripItem trip = new TripItem(null, name, getDate(), null);

        double dist=0.;
        for (WpItem wp : wps) {
            // get distance
            if (wp.getWpDistance()!=null) {
                dist += wp.getWpDistance();
            }
            // update trip index to this trip
            wp.setTripIndex(trip.getTripId());
            // Then write to database
            createWp(wp);
        }
        // Update trip with total distance and write to DB
        trip.setTripDistance(dist);

        return createTrip(trip);
    }

    /*
     * getAllTrips
     * Lookup matching records in the database and for each create a matching object and return
     * a list with all of these as the result.
     *
     * @Args: category, a filter - if this is null all rows are returned from db, otherwise only
     *                  Trips with a date string matching the filter is returned.
     *
     * @Return: A list of trip objects
     *
     */
    public List<TripItem> getAllTrips(String category){
        List<TripItem> trips = new ArrayList<>();
        Cursor cursor;

        if (category==null) {
            cursor = mDb.query(TripTable.TABLE_NAME, TripTable.ALL_COLUMNS, null,null,null,null,TripTable.COLUMN_DATE);
        } else {
            String[] categories = {category};
            cursor = mDb.query(TripTable.TABLE_NAME, TripTable.ALL_COLUMNS, TripTable.COLUMN_DATE+"=?",categories,null,null,TripTable.COLUMN_NAME);
        }


        while (cursor.moveToNext()) {
            TripItem trip = new TripItem();
            trip.setTripId(cursor.getString(
                    cursor.getColumnIndex(TripTable.COLUMN_ID)));
            trip.setTripName(cursor.getString(
                    cursor.getColumnIndex(TripTable.COLUMN_NAME)));
            trip.setTripDate(cursor.getString(
                    cursor.getColumnIndex(TripTable.COLUMN_DATE)));
            trip.setTripDistance(cursor.getDouble(
                    cursor.getColumnIndex(TripTable.COLUMN_DIST)));
            trips.add(trip);
        }
        cursor.close();

        return trips;
    }


    //
    // WP Table specifics
    // --------------------

    /*
     * createWp
     * This is the function responsible for inserting data into the WayPoint Table. Will be returning
     * the same object in case we need to check if something has been changed.
     *
     * @Args: WP Object
     *
     * @Return: WP object
     *
     */
    public WpItem createWp(WpItem wp) {
        ContentValues values = wp.toContentValues();

        mDb.insert(WpTable.TABLE_NAME, null, values);
        return wp;
    }


    /*
     * deleteWp
     * This function will erase a way point from the WP table
     *
     * @params: wp   - the WpItem to be deleted
     * @Returns: none
     */
    public void deleteWp(WpItem wp, boolean updateDistance) {

        // Re-sequence the subsequent Way points
        String sql="UPDATE wps SET wpSequenceNumber=wpSequenceNumber-1"
                  +" WHERE wpSequenceNumber > (SELECT wpSequenceNumber FROM wps WHERE wpId=?)";
        mDb.execSQL(sql, new String[]{wp.getWpId()});

        // Now delete the way point
        mDb.delete(WpTable.TABLE_NAME, WpTable.COLUMN_ID + " = ?", new String[]{wp.getWpId() });

        // And finally update the distance of the associated trip if requested
        if (updateDistance) {

            String query = "SELECT SUM(wpDistance) AS S FROM wps WHERE WpTripId=?";

            Cursor cursor;
            cursor = mDb.rawQuery(query, new String[]{wp.getTripIndex()});

            if (cursor.moveToFirst()) {
                String distString = cursor.getString(cursor.getColumnIndex("S"));
                Double nyDist=null;

                if (distString != null) {
                    nyDist = Double.parseDouble(distString);
                }

                ContentValues value = new ContentValues();
                value.put(TripTable.COLUMN_DIST, nyDist);
                mDb.update(TripTable.TABLE_NAME, value, TripTable.COLUMN_ID + " = ?",
                        new String[]{wp.getTripIndex()});
            } else {
                Log.e(TAG, "cursor.moveToFirst() failed in deleteWp");
            }
            cursor.close();
        }
    }


    /*
      * getWpCount
      * Return the number of rows in the WayPoint Table
      *
      * @Args: None
      *
      * @Return: long
      *
      */
    public long getWpCount() {
        return DatabaseUtils.queryNumEntries(mDb, WpTable.TABLE_NAME);
    }


    /*
     * seedWpTable
     * Insert all Wp objects into the WayPoint table in the database
     *
     * @Args: List of WP objects
     *
     * @Return: none
     *
     */
    public void seedWpTable(List<WpItem> wpList) {
         if (wpList.size()>0) {
            for (WpItem wp : wpList) {
                try {
                    createWp(wp);
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /*
     * getAllWps
     * Lookup matching records in the database and for each create a matching object and return
     * a list with all of these as the result.
     *
     * @Args: id,   a filter - if this is null all rows are returned from db, otherwise only
     *              Way Points with a matching ID is returned.
     *
     * @Return: A list of WP objects
     *
     */
    public List<WpItem> getAllWps(String id){
        List<WpItem> wps = new ArrayList<>();
        Cursor cursor;

        if (id==null) {
            cursor = mDb.query(WpTable.TABLE_NAME, WpTable.ALL_COLUMNS, null,null,null,null, WpTable.COLUMN_SEQUENCE_NUMBER);
        } else {
            String filterQuery = "SELECT  * FROM " + WpTable.TABLE_NAME + " WHERE "+WpTable.COLUMN_TRIP_ID
                                +"=? ORDER BY "+WpTable.COLUMN_SEQUENCE_NUMBER+" ASC";
            Log.d(TAG, "Query: "+filterQuery);
             cursor = mDb.rawQuery(filterQuery, new String[]{id});
        }

        while (cursor.moveToNext()) {
            WpItem wp = new WpItem();
            wp.setWpId(cursor.getString(cursor.getColumnIndex(WpTable.COLUMN_ID)));
            wp.setWpName(cursor.getString(cursor.getColumnIndex(WpTable.COLUMN_NAME)));
            wp.setWpAltitude(cursor.getInt(cursor.getColumnIndex(WpTable.COLUMN_ALT)));
            wp.setWpDistance(cursor.getDouble(cursor.getColumnIndex(WpTable.COLUMN_DIST)));
            wp.setTripIndex(cursor.getString(cursor.getColumnIndex(WpTable.COLUMN_TRIP_ID)));
            wp.setWpSequenceNumber(cursor.getInt(cursor.getColumnIndex(WpTable.COLUMN_SEQUENCE_NUMBER)));
            wp.setWpLat(cursor.getDouble(cursor.getColumnIndex(WpTable.COLUMN_LAT)));
            wp.setWpLon(cursor.getDouble(cursor.getColumnIndex(WpTable.COLUMN_LON)));
            wps.add(wp);
        }
        cursor.close();

        return wps;
    }



    // ================  NavAidsTable ==========================



    /*
     * createNavAid
     * This is the function responsible for inserting data into the NavAid Table. Will be returning
     * the same object in case we need to check if something has been changed.
     *
     * @Args: NavAid Object
     *
     * @Return: NavAid object
     *
     */
    public NavAid createNavAid(NavAid na) {
        ContentValues values = na.toContentValues();

        mDb.insert(NavAidTable.TABLE_NAME, null, values);
        return na;
    }

    /*
     * seedNavAidTable
     * Insert all NavAid objects into the NavAids table in the database
     *
     * @Args: List of NavAid objects
     *
     * @Return: none
     *
     */
    public void seedNavAidTable(List<NavAid> navAidList) {
        if (navAidList.size()>0) {
            for (NavAid na : navAidList) {
                try {
                    createNavAid(na);
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /*
     * getAllNavAids
     * Lookup matching records in the database and for each create a matching object and return
     * a list with all of these as the result.
     *
     * @Args: type,     a filter - if this is null all rows are returned from db, otherwise only
     *                  NavAids of a certain type is returned.
     *
     * @Return: A list of trip NavAids
     *
     */
    public List<NavAid> getAllNavAids(Integer type){
        List<NavAid> navAids = new ArrayList<>();
        Cursor cursor;

        if (type==null) {
            // Fetch all sorted by Name
            cursor = mDb.query(NavAidTable.TABLE_NAME, NavAidTable.ALL_COLUMNS, null,null,null,null,NavAidTable.COLUMN_NAME);
        } else {
            // Pack the type in the required Array object notation before search for matching types and then sorting by name
            String[] typeArg = {type.toString()};
            cursor = mDb.query(NavAidTable.TABLE_NAME, NavAidTable.ALL_COLUMNS, NavAidTable.COLUMN_TYPE+"=?",typeArg, null, null, NavAidTable.COLUMN_NAME);
        }


        while (cursor.moveToNext()) {
            NavAid  navAid = new NavAid();

            navAid.setName(cursor.getString(cursor.getColumnIndex(NavAidTable.COLUMN_NAME)));
            navAid.setIdent(cursor.getString(cursor.getColumnIndex(NavAidTable.COLUMN_IDENT)));
            navAid.setType(cursor.getInt(cursor.getColumnIndex(NavAidTable.COLUMN_TYPE)));
            double lat = cursor.getDouble(cursor.getColumnIndex(NavAidTable.COLUMN_LAT));
            double lon = cursor.getDouble(cursor.getColumnIndex(NavAidTable.COLUMN_LON));
            navAid.setPosition(new LatLng(lat, lon));
            navAid.setFreq(cursor.getString(cursor.getColumnIndex(NavAidTable.COLUMN_FREQ)));
            navAid.setMax_alt(cursor.getInt(cursor.getColumnIndex(NavAidTable.COLUMN_MAX_ALT)));
            navAid.setMax_range(cursor.getInt(cursor.getColumnIndex(NavAidTable.COLUMN_MAX_DIST)));
            navAid.setElevation(cursor.getDouble(cursor.getColumnIndex(NavAidTable.COLUMN_ELEV)));

            navAids.add(navAid);
        }
        cursor.close();

        return navAids;
    }




    /*
     * get date string
     *
     */
    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
