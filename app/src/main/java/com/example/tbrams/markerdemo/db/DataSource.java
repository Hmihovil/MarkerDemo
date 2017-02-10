package com.example.tbrams.markerdemo.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.tbrams.markerdemo.data.Aerodrome;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.Obstacle;
import com.example.tbrams.markerdemo.data.ReportingPoint;
import com.example.tbrams.markerdemo.dbModel.TripItem;
import com.example.tbrams.markerdemo.dbModel.WpItem;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import static com.example.tbrams.markerdemo.components.Util.getDate;

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
    public SQLiteDatabase openAndGetDb() {
        mDb = mDbOpenHelper.getWritableDatabase();
        return mDb;
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



    public void resetAerodromeTable() {
        mDb.execSQL(AdTable.SQL_DELETE);
        mDb.execSQL(AdTable.SQL_CREATE);
    }

    public void resetRPTable() {
        mDb.execSQL(RPTable.SQL_DELETE);
        mDb.execSQL(RPTable.SQL_CREATE);
    }

    public void resetObstaclesTable() {
        mDb.execSQL(ObstacleTable.SQL_DELETE);
        mDb.execSQL(ObstacleTable.SQL_CREATE);
    }


    public void resetAreaTables() {
        mDb.execSQL(AreaTable.SQL_DELETE);
        mDb.execSQL(CoordTable.SQL_DELETE);
        mDb.execSQL(AreaTable.SQL_CREATE);
        mDb.execSQL(CoordTable.SQL_CREATE);
    }


    public void makeSureWeHaveTables() {
        Log.d(TAG, "makeSureWeHaveTables: entered");

        mDb.execSQL(TripTable.SQL_CREATE);
        mDb.execSQL(WpTable.SQL_CREATE);
        mDb.execSQL(NavAidTable.SQL_CREATE);
        mDb.execSQL(AdTable.SQL_CREATE);
        mDb.execSQL(RPTable.SQL_CREATE);
        mDb.execSQL(ObstacleTable.SQL_CREATE);
        mDb.execSQL(AreaTable.SQL_CREATE);
        mDb.execSQL(CoordTable.SQL_CREATE);


    }


    /**
     * This function will return false if the table is not already created
     */
    boolean isTableExists(SQLiteDatabase db, String tableName)
    {
        if (tableName == null || db == null || !db.isOpen())
        {
            return false;
        }
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[] {"table", tableName});
        if (!cursor.moveToFirst())
        {
            cursor.close();
            return false;
        }
        int count = cursor.getInt(0);
        cursor.close();
        return count > 0;
    }

    //
    //Trip Table specifics
    // --------------------


    /**
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



    /**
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

    /**
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


    /**
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

    /**
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



    /**
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

    /**
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

    /**
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


    /**
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


    /**
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


    /**
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


    /**
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



    /**
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

    /**
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


    /**
     * getAllNavAids
     * Lookup matching records in the database and for each create a matching object and return
     * a list with all of these as the result.
     *
     * @Args: type,     a filter - if this is null all rows are returned from db, otherwise only
     *                  Navigational Aids of a certain type is returned.
     *
     * @Return: A list of trip Navigational Aids
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




    // ================  AdTable ==========================



    /**
     * createAerodrome
     * This is the function responsible for inserting data into the Aerodrome Table. Will be returning
     * the same object in case we need to check if something has been changed.
     *
     * @Args: Aerodrome Object
     *
     * @Return: Aerodrome object
     *
     */
    public Aerodrome createAerodrome(Aerodrome ad) {
        ContentValues values = ad.toContentValues();

        mDb.insert(AdTable.TABLE_NAME, null, values);
        return ad;
    }

    /**
     * seedAerodromeTable
     * Insert all Aerodrome objects into the Aerodrome table in the database
     *
     * @Args: List of Aerodrome objects
     *
     * @Return: none
     *
     */
    public void seedAerodromeTable(List<Aerodrome> adList) {
        if (adList.size()>0) {
            for (Aerodrome ad : adList) {
                try {
                    createAerodrome(ad);
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * getAllAerodromes
     * Lookup matching records in the database and for each create a matching object and return
     * a list with all of these as the result.
     *
     * @Args: type,     a filter - if this is null all rows are returned from db, otherwise only
     *                  Aerodromes of a certain type is returned.
     *
     * @Return: A list of Aerodromes
     *
     */
    public List<Aerodrome> getAllAerodromes(Integer type){
        List<Aerodrome> adList = new ArrayList<>();
        Cursor cursor;

        if (type==null) {
            // Fetch all sorted by Name
            cursor = mDb.query(AdTable.TABLE_NAME, AdTable.ALL_COLUMNS, null,null,null,null, AdTable.COLUMN_NAME);
        } else {
            // Pack the type in the required Array object notation before search for matching types and then sorting by name
            String[] typeArg = {type.toString()};
            cursor = mDb.query(AdTable.TABLE_NAME, AdTable.ALL_COLUMNS, AdTable.COLUMN_TYPE+"=?",typeArg, null, null, AdTable.COLUMN_NAME);
        }


        while (cursor.moveToNext()) {
            Aerodrome  ad = new Aerodrome();

            ad.setName(cursor.getString(cursor.getColumnIndex(AdTable.COLUMN_NAME)));
            ad.setIcaoName(cursor.getString(cursor.getColumnIndex(AdTable.COLUMN_ICAO)));
            ad.setType(cursor.getInt(cursor.getColumnIndex(AdTable.COLUMN_TYPE)));
            double lat = cursor.getDouble(cursor.getColumnIndex(AdTable.COLUMN_LAT));
            double lon = cursor.getDouble(cursor.getColumnIndex(AdTable.COLUMN_LON));
            ad.setPosition(new LatLng(lat, lon));
            ad.setRadio(cursor.getString(cursor.getColumnIndex(AdTable.COLUMN_RADIO)));
            ad.setFreq(cursor.getString(cursor.getColumnIndex(AdTable.COLUMN_FREQ)));
            if (cursor.getInt(cursor.getColumnIndex(AdTable.COLUMN_PPR))==1) {
                ad.setPPR(true);
            } else {
                ad.setPPR(false);
            }
            ad.setPhone(cursor.getString(cursor.getColumnIndex(AdTable.COLUMN_PHONE)));
            ad.setWeb(cursor.getString(cursor.getColumnIndex(AdTable.COLUMN_WEB)));
            ad.setLink(cursor.getString(cursor.getColumnIndex(AdTable.COLUMN_LINK)));
            ad.setActivity(cursor.getString(cursor.getColumnIndex(AdTable.COLUMN_ACTIVITY)));
            ad.setRemarks(cursor.getString(cursor.getColumnIndex(AdTable.COLUMN_REMARKS)));

            adList.add(ad);
        }
        cursor.close();

        return adList;
    }




    // ================  Reporting Points Table ==========================



    /**
     * createReportingPoint
     * This is the function responsible for inserting data into the RPTable. Will be returning
     * the same object in case we need to check if something has been changed.
     *
     * @Args: ReportingPoint Object
     *
     * @Return: ReportingPoing object
     *
     */
    public ReportingPoint createReportingPoing(ReportingPoint rp) {
        ContentValues values = rp.toContentValues();

        mDb.insert(RPTable.TABLE_NAME, null, values);
        return rp;
    }



    /**
     * seedReportingPointTable
     * Insert all ReportingPoing objects into the RP table in the database
     *
     * @Args: List of ReportingPoing objects
     *
     * @Return: none
     *
     */
    public void seedReportingPoingTable(List<ReportingPoint> rpList) {
        if (rpList.size()>0) {
            for (ReportingPoint rp : rpList) {
                try {
                    createReportingPoing(rp);
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * getAllReportingPoints
     * Lookup matching records in the database and for each create a matching object and return
     * a list with all of these as the result.
     *
     * @Args: String,     a filter - if this is null all rows are returned from db, otherwise only
     *                    Reporting Points belonging to a certain Aerodrome are returned.
     *
     * @Return: A list of Reporting Points
     *
     */
    public List<ReportingPoint> getAllReportingPoints(String filter){
        List<ReportingPoint> rpList = new ArrayList<>();
        Cursor cursor;

        if (filter==null) {
            // Fetch all sorted by Name
            cursor = mDb.query(RPTable.TABLE_NAME, RPTable.ALL_COLUMNS, null,null,null,null, RPTable.COLUMN_NAME);
        } else {
            // Pack the type in the required Array object notation before search for matching types and then sorting by name
            String[] adArg = {filter};
            cursor = mDb.query(RPTable.TABLE_NAME, RPTable.ALL_COLUMNS, RPTable.COLUMN_AD+"=?",adArg, null, null, RPTable.COLUMN_NAME);
        }


        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(RPTable.COLUMN_NAME));
            String adname = cursor.getString(cursor.getColumnIndex(RPTable.COLUMN_AD));
            double lat = cursor.getDouble(cursor.getColumnIndex(RPTable.COLUMN_LAT));
            double lon = cursor.getDouble(cursor.getColumnIndex(RPTable.COLUMN_LON));

            ReportingPoint rp = new ReportingPoint(adname, name, lat, lon);

            rpList.add(rp);
        }
        cursor.close();

        return rpList;
    }



    // ================  Obstacles Table ==========================



    /**
     * createObstacle
     * This is the function responsible for inserting data into the ObstacleTable. Will be returning
     * the same object in case we need to check if something has been changed.
     *
     * @Args: Obstacle Object
     *
     * @Return: Obstacle object
     *
     */
    public Obstacle createObstacle(Obstacle obstacle) {
        ContentValues values = obstacle.toContentValues();

        mDb.insert(ObstacleTable.TABLE_NAME, null, values);
        return obstacle;
    }



    /**
     * seedObstacleTable
     * Insert all Obstacle objects into the RP table in the database
     *
     * @Args: List of Obstacle objects
     *
     * @Return: none
     *
     */
    public void seedObstacleTable(List<Obstacle> obstacleList) {
        if (obstacleList.size()>0) {
            for (Obstacle obstacle : obstacleList) {
                try {
                    createObstacle(obstacle);
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    /**
     * getAllObstacles
     * Lookup matching records in the database and for each create a matching object and return
     * a list with all of these as the result.
     *
     * @Args: String,     a filter - if this is null all rows are returned from db, otherwise only
     *                    Obstacles belonging to a certain What type are returned.
     *
     * @Return: A list of Obstacles
     *
     */
    public List<Obstacle> getAllObstacles(String filter){
        List<Obstacle> obstacleList = new ArrayList<>();
        Cursor cursor;

        if (filter==null) {
            // Fetch all sorted by Name
            cursor = mDb.query(ObstacleTable.TABLE_NAME, ObstacleTable.ALL_COLUMNS, null,null,null,null, ObstacleTable.COLUMN_NAME);
        } else {
            // Pack the type in the required Array object notation before search for matching types and then sorting by name
            String[] adArg = {filter};
            cursor = mDb.query(ObstacleTable.TABLE_NAME, ObstacleTable.ALL_COLUMNS, ObstacleTable.COLUMN_WHAT+"=?",adArg, null, null, ObstacleTable.COLUMN_NAME);
        }


        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ObstacleTable.COLUMN_NAME));
            String what = cursor.getString(cursor.getColumnIndex(ObstacleTable.COLUMN_WHAT));
            double lat = cursor.getDouble(cursor.getColumnIndex(ObstacleTable.COLUMN_LAT));
            double lon = cursor.getDouble(cursor.getColumnIndex(ObstacleTable.COLUMN_LON));
            int elevation = cursor.getInt(cursor.getColumnIndex(ObstacleTable.COLUMN_ELEVATION));
            int height = cursor.getInt(cursor.getColumnIndex(ObstacleTable.COLUMN_HEIGHT));

            Obstacle obstacle = new Obstacle(name, what, lat, lon, elevation, height);

            obstacleList.add(obstacle);
        }
        cursor.close();

        return obstacleList;
    }

}
