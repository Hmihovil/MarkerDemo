package com.example.tbrams.markerdemo.db;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.tbrams.markerdemo.data.Aerodrome;
import com.example.tbrams.markerdemo.data.ExtraMarkers;
import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.data.ReportingPoint;
import com.example.tbrams.markerdemo.dbModel.JSONHelper;
import com.example.tbrams.markerdemo.dbModel.SampleDataProvider;
import com.example.tbrams.markerdemo.dbModel.TripItem;
import com.example.tbrams.markerdemo.dbModel.WpItem;

import java.util.List;

/**
 * An attempt to contain all Database Admin facilities to this class, instead of having them
 * all over the place.
 *
 */
public class DbAdmin extends DataSource {
    private static final int REQUEST_PERMISSION_WRITE = 101;
    private static final String TAG = "TBR:DbAdmin";
    private static boolean mDbMaintenance=false;

    List<String> mTripSampleList = SampleDataProvider.sTrips;
    List<List<WpItem>> mWpSampleList = SampleDataProvider.sWpListsForTrips;

    private ExtraMarkers sExtraMarkers;

    private boolean mPermissionGranted;
    private Context mContext;

    public DbAdmin(Context context) {
        super(context);

        mContext = context;
        mPermissionGranted = false;
    }



    public void populateDatabase() {
        // Delete and recreate both tables
        super.open();
        super.resetDB();

        // For each trip we have in the Sample provider, get all related WP's as well and add it all to the DB
        for (int i = 0; i < mTripSampleList.size(); i++) {
            String tripName = mTripSampleList.get(i);
            List<WpItem> wpList = mWpSampleList.get(i);

            super.addFullTrip(tripName, wpList);
        }

       super.close();

        // Get hold of ExtraMarker storage and fetch the NavAid samples we will use for resetting
        sExtraMarkers = ExtraMarkers.get(mContext);
        List<NavAid> navAidsSampleList = sExtraMarkers.getSampleNavAidList();

        // Then update the NavAid table
        updateNavAidsFromMaster(navAidsSampleList);

        // Fetch the Aerodrome samples we will use for resetting
        List<Aerodrome> adSampleList = sExtraMarkers.getSampleAerodromeList();

        // Then update the Aerodromes table
        updateAerodromesFromMaster(adSampleList, true);

    }



    /**
     * Will reset the Reporting Points table and then populate it from the list provided
     * as argument to the function.
     *
     * After updating the database table, the singleton Storage will be reflecting the database
     * as well.
     */
    public void updateReportingPointsFromMaster(List<ReportingPoint> rpList, boolean purgeDatabase) {
        // Delete and recreate table
        super.open();

        if (purgeDatabase) {
            // Delete and recreate table
            super.resetRPTable();
        }

        // Copy all Reporting Points from the list to the Database
        for (ReportingPoint rp : rpList) {
            super.createReportingPoing(rp);
        }

        super.close();

        if (sExtraMarkers==null) {
            sExtraMarkers=ExtraMarkers.get(mContext);
        }
        sExtraMarkers.setReportingPointList(rpList);

    }



    /**
     * Will reset the NavAids table and then populate it with navaids from the list provided
     * as argument to the function.
     *
     * After updating the database table, the singleton Storage will be reflecting the database
     * as well.
     */
    public void updateNavAidsFromMaster(List<NavAid> navAidsList) {
        // Delete and recreate table
        super.open();
        super.resetNavAidTable();

        // Copy all navigational aids form the sample list to the Database
        for (NavAid na : navAidsList) {
            super.createNavAid(na);
        }

        super.close();

        if (sExtraMarkers==null) {
            sExtraMarkers=ExtraMarkers.get(mContext);
        }
        sExtraMarkers.setNavAidList(navAidsList);

    }


    /**
     * Will reset the Aerodromes table and then populate it with aerodromes from the list provided
     * as argument to the function.
     *
     * After updating the database table, the singleton Storage will be reflecting the database
     * as well.
     */
    public void updateAerodromesFromMaster(List<Aerodrome> adList, boolean purgeDatabase) {

        super.open();

        if (purgeDatabase) {
            // Delete and recreate table
            super.resetAerodromeTable();
        }

        // Copy all navigational aids form the sample list to the Database
        for (Aerodrome ad : adList) {
            super.createAerodrome(ad);
        }

        super.close();

        if (sExtraMarkers==null) {
            sExtraMarkers=ExtraMarkers.get(mContext);
        }
        sExtraMarkers.setAerodromeList(adList);

    }




    public void importJSON() {
        // Need storage access permission for import
        if (!mPermissionGranted) {
            mPermissionGranted = checkPermissions();
        }

        if (mPermissionGranted) {
            // First flush the tables
            super.open();
            super.resetDB();

            // then get the items from the imported trips and save in DB
            List<TripItem> tripItems = JSONHelper.importTripsFromJSON();
            super.seedTripTable(tripItems);
            Log.i(TAG, "Imported JSON Trip Data written to DB");

            List<WpItem> wpItems = JSONHelper.importWpsFromJSON();
            super.seedWpTable(wpItems);
            Log.i(TAG, "Imported JSON WP Data written to DB");

            List<NavAid> naList = JSONHelper.importNavAidsFromJSON();
            super.seedNavAidTable(naList);
            Log.i(TAG, "Imported JSON NavAid Data written to DB");

            super.close();
        }
    }


    public void exportJSON(){

        // Need storage access permission for export
        if (!mPermissionGranted) {
            mPermissionGranted = checkPermissions();
        }

        if (mPermissionGranted) {
            super.open();

            // Get all WPs from database
            List<WpItem> wps = super.getAllWps(null);
            Log.d(TAG, "exportJSON: wps.size(): "+wps.size());

            // Export the WPs using JSONHelper
            if (JSONHelper.exportWpsToJSON(wps)) {
                Log.i(TAG, "WP data exported in JSON format");
            } else {
                Log.e(TAG, "WP data export failed");
            }


            // Get all Trips from database
            List<TripItem> mListFromDB = super.getAllTrips(null);
            Log.d(TAG, "exportJSON: mListFromDB.size():"+mListFromDB.size());

            // Export the Trips using JSONHelper
            if (JSONHelper.exportTripsToJSON(mListFromDB)) {
                Toast.makeText(mContext, "Trip data Exported in JSON format", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "Trips data export failed");
            }

            // Get all Nav Aids from database
            List<NavAid> mNavAidsListFromDB = super.getAllNavAids(null);

            // Export the NavAids using JSONHelper
            if (JSONHelper.exportNavAidsToJSON(mNavAidsListFromDB)) {
                Toast.makeText(mContext, "NavAid data Exported in JSON format", Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "NavAid data export failed");
            }
            super.close();
        }
    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }


    // Initiate request for permissions.
    private boolean checkPermissions() {
        Log.d(TAG, "CheckPermissions");

        if (!isExternalStorageReadable() || !isExternalStorageWritable()) {
            Toast.makeText(mContext, "This app only works on devices with usable external storage",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE);

            return false;
        } else {
            return true;
        }
    }


    // Handle permissions result
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionGranted = true;
                    Log.d(TAG, "onRequestPermissionsResult: External storage permission granted");
                } else {
                    Toast.makeText(mContext, "You must grant permission!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    public void toggleMaintenanceMode() {
        mDbMaintenance = !mDbMaintenance;
    }

    public boolean isInMaintenanceMode() {
        return mDbMaintenance;
    }


}
