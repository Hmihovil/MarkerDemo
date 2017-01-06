package com.example.tbrams.markerdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.tbrams.markerdemo.db.DataSource;
import com.example.tbrams.markerdemo.dbModel.JSONHelper;
import com.example.tbrams.markerdemo.dbModel.SampleDataProvider;
import com.example.tbrams.markerdemo.dbModel.TripItem;
import com.example.tbrams.markerdemo.dbModel.WpItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_WRITE = 101;

    List<String>       mTripSampleList = SampleDataProvider.sTrips;
    List<List<WpItem>> mWpSampleList   = SampleDataProvider.sWpListsForTrips;

    DataSource      mDataSource;
    List<TripItem>  mListFromDB;
    RecyclerView mRecyclerView;
    TripAdapter     mTripAdapter;
    private boolean mPermissionGranted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Need this for import/export
        if (!mPermissionGranted) {
            mPermissionGranted=checkPermissions();
        }


        // Get a handle to the database helper and prepare the database
        mDataSource = new DataSource(this);
        mDataSource.open();

        // Get a reference to the layout for the recyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.rvItems);

        // Display info on trips available in the database
        displayTrips(null);

    }


    private void populateDatabase() {
        // Delete and recreate both tables
        mDataSource.resetDB();

        for (int i = 0; i < mTripSampleList.size(); i++) {
                String tripName = mTripSampleList.get(i);
                List<WpItem> wpList = mWpSampleList.get(i);

                mDataSource.addFullTrip(tripName, wpList);
            }
    }



    /*
     * displayTrips
     * Lookup trip data in the database considering the filter. Create a list of
     * matching object for each row returned and feed that to the TripAdapter that will
     * be used for the List.
     *
     * @args:   filter
     * @return: none
     */
    private void displayTrips(String filter) {
        mListFromDB  = mDataSource.getAllTrips(filter);
        mTripAdapter = new TripAdapter(this, mListFromDB);

        mRecyclerView.setAdapter(mTripAdapter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mDataSource.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDataSource.open();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.db_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Show the settings screen - not in this version though...
                return true;

            case R.id.action_import:
                // First flush the tables
                mDataSource.resetDB();

                // then get the items from the imported trips and save in DB
                List<TripItem> tripItems = JSONHelper.importTripsFromJSON(this);
                mDataSource.seedTripTable(tripItems);
                Log.i("TBR", "Restored Trip Data written to DB");

                // Update list display
                displayTrips(null);

                List<WpItem> wpItems = JSONHelper.importWpsFromJSON(this);
                mDataSource.seedWpTable(wpItems);
                Log.i("TBR", "Restored Wp Data written to DB");

                return true;

            case R.id.action_export:

                // Get all WPs from database for export
                List<WpItem> wps = mDataSource.getAllWps(null);
                if (JSONHelper.exportWpsToJSON(this, wps)) {
                    Log.i("TBR", "WP data exported");
                } else {
                    Log.e("TBR", "WP data export failed");
                }


                if (JSONHelper.exportTripsToJSON(this, mListFromDB)) {
                    Toast.makeText(this, "Database Exported", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("TBR", "Trips data export failed");
                }
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
        Log.d("TBR", "CheckPermissions");

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


}

