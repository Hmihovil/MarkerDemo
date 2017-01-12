package com.example.tbrams.markerdemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.tbrams.markerdemo.data.NavAid;
import com.example.tbrams.markerdemo.db.DataSource;
import com.example.tbrams.markerdemo.dbModel.JSONHelper;
import com.example.tbrams.markerdemo.dbModel.SampleDataProvider;
import com.example.tbrams.markerdemo.dbModel.TripItem;
import com.example.tbrams.markerdemo.dbModel.WpItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_WRITE = 101;
    private static final String TAG = "TBR:MA";

    List<String>       mTripSampleList = SampleDataProvider.sTrips;
    List<List<WpItem>> mWpSampleList   = SampleDataProvider.sWpListsForTrips;
    List<NavAid> mNavAidListsSample = SampleDataProvider.sNavAidList;

    DataSource      mDataSource;
    List<TripItem>  mListFromDB;
    List<NavAid>    mNavAidsListFromDB;
    RecyclerView    mRecyclerView;
    TripAdapter     mTripAdapter;
    private boolean mPermissionGranted;
    private static boolean mDbMaintenance=false;
    private Menu    mMenuHandle;
    private static Context mContext;
    public static View mBackgroundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        updateTitle();

        setContentView(R.layout.activity_main);

        mBackgroundView = findViewById(R.id.activity_main);

        // Need this for import/export
        if (!mPermissionGranted) {
            mPermissionGranted=checkPermissions();
        }


        // Get a handle to the database helper and prepare the database
        mDataSource = new DataSource(this);

        // Get a reference to the layout for the recyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.rvItems);

        // Display info on trips available in the database
        displayTrips(null);

    }


    public static Context getContext() {
        return mContext;
    }

    private void populateDatabase() {
        // Delete and recreate both tables
        mDataSource.open();
        mDataSource.resetDB();

        // For each trip we have in the Sample provider, get all related WP's as well and add it all to the DB
        for (int i = 0; i < mTripSampleList.size(); i++) {
                String tripName = mTripSampleList.get(i);
                List<WpItem> wpList = mWpSampleList.get(i);

                mDataSource.addFullTrip(tripName, wpList);
        }

        // Copy all navaids form the dataProvider to the Database
        for (NavAid na : mNavAidListsSample) {
            mDataSource.createNavAid(na);
        }

        mDataSource.close();
    }



    /*
     * displayTrips
     * Lookup trip data in the database considering the filter. Create a list of
     * matching object for each row returned and feed that to the TripAdapter that will
     * be used for the List.
     *
     * Also for data maintenance purposes, get a fresh list of NavAids from the database at this point
     *
     * @args:   filter
     * @return: none
     */
    private void displayTrips(String filter) {
        mDataSource.open();
        mListFromDB  = mDataSource.getAllTrips(filter);
        mNavAidsListFromDB = mDataSource.getAllNavAids(null);
        mDataSource.close();

        mTripAdapter = new TripAdapter(this, mListFromDB);
        mRecyclerView.setAdapter(mTripAdapter);
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTripAdapter = null;
        displayTrips(null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenuHandle = menu;
        getMenuInflater().inflate(R.menu.db_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Toggle usage mode between dabtabase maintenance and select trip mode
                // In select trip: click on trip -> WP inspection, long click -> select trip
                //                 click on wp -> you selected, long click -> Nothing
                // In db maintenance: Click on trip -> WP inspection, long click -> delete trip
                //                    click on wp -> you selected, long click -> delete wp

                mDbMaintenance = !mDbMaintenance;
                updateTitle();
                TripAdapter.updateBackgroundColor();

                return true;

            case R.id.action_import:
                // First flush the tables
                mDataSource.open();
                mDataSource.resetDB();

                // then get the items from the imported trips and save in DB
                List<TripItem> tripItems = JSONHelper.importTripsFromJSON(this);
                mDataSource.seedTripTable(tripItems);
                Log.i(TAG, "Restored JSON Trip Data written to DB");

                // Update list display - show them all
                displayTrips(null);

                // need to re-open datasource, because displayTrips closes it
                mDataSource.open();
                List<WpItem> wpItems = JSONHelper.importWpsFromJSON(this);
                mDataSource.seedWpTable(wpItems);
                Log.i(TAG, "Restored JSON WP Data written to DB");

                List<NavAid> naList = JSONHelper.importNavAidsFromJSON(this);
                mDataSource.seedNavAidTable(naList);
                Log.i(TAG, "Restored JSON NavAid Data written to DB");

                mDataSource.close();

                return true;

            case R.id.action_export:

                // Get all WPs from database for export
                mDataSource.open();
                List<WpItem> wps = mDataSource.getAllWps(null);
                if (JSONHelper.exportWpsToJSON(this, wps)) {
                    Log.i(TAG, "WP data exported in JSONB format");
                } else {
                    Log.e(TAG, "WP data export failed");
                }


                if (JSONHelper.exportTripsToJSON(this, mListFromDB)) {
                    Toast.makeText(this, "Trip data Exported in JSONB format", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Trips data export failed");
                }


                if (JSONHelper.exportNavAidsToJSON(this, mNavAidsListFromDB)) {
                    Toast.makeText(this, "NavAid data Exported in JSONB format", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "NavAid data export failed");
                }
                mDataSource.close();

                return true;


            case R.id.action_reset:
                // Clean DB and load test data
                populateDatabase();

                // Update list display
                displayTrips(null);

                return true;

        }
        return super.onOptionsItemSelected(item);
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
            Toast.makeText(this, "This app only works on devices with usable external storage",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE);

            return false;
        } else {
            return true;
        }
    }

    // Handle permissions result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionGranted = true;
                    Toast.makeText(this, "External storage permission granted",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You must grant permission!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    public static boolean isThisDbMaintenance() {
        return mDbMaintenance;
    }


    private void updateTitle() {
        if (isThisDbMaintenance())
            setTitle(getString(R.string.title_maintenance));
        else
            setTitle(getString(R.string.title_trip));
    }



}

