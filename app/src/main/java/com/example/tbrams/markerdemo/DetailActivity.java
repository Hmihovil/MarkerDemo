package com.example.tbrams.markerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.db.DataSource;
import com.example.tbrams.markerdemo.dbModel.WpItem;

import java.util.List;

import static com.example.tbrams.markerdemo.MarkerDemoActivity.getCurrentTripId;

/*
 * This class is used to display a list of Waypoints in data browse mode
 */

@SuppressWarnings("FieldCanBeLocal")
public class DetailActivity extends AppCompatActivity {

    DataSource   mDataSource;
    List<WpItem> mListFromDB;
    RecyclerView mRecyclerView;
    WpAdapter    mWpAdapter;
    String       mId;
    MarkerLab markerLab;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        markerLab= MarkerLab.getMarkerLab(getApplicationContext());
        if (MainActivity.isThisDbMaintenance()) {
            setTitle(R.string.title_maintenance);
        } else {
            setTitle(markerLab.getTripName());
        }

        // Get trip id from intent extra storage
        mId = getIntent().getExtras().getString(TripAdapter.TRIP_KEY);
        if (mId == null) {
            Log.d("TBR:", "DetailActivity received a null id from extras");
        }

        Log.d("TBR:", "DetailActivity, received TRIP_KEY:"+mId);

        // Get a reference to the recyclerView and make sure we have defined
        // a LayoutManager ... otherwise it will crash
        mRecyclerView = (RecyclerView) findViewById(R.id.rvItems);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        displayWps();
    }


    private void displayWps() {

        // Get the WP data ready for display
        mDataSource = new DataSource(this);
        mDataSource.open();
        mListFromDB = mDataSource.getAllWps(mId);

        // If the Database has been recently updated, in some cases the ID will no longer
        // produce results. In that case we have reserved a new ID in the OnActionResult
        // handler, that we can use
        if (mListFromDB.size()==0) {
            mListFromDB = mDataSource.getAllWps(getCurrentTripId());
        }

        mDataSource.close();

        if (mWpAdapter==null) {
            // create an adapter and initiate it with the available data
            mWpAdapter = new WpAdapter(MainActivity.getContext(), mListFromDB);
            mRecyclerView.setAdapter(mWpAdapter);
        } else {
            // It is already on screen, we just need to refresh
            mWpAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDataSource.close();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWpAdapter = null;
        displayWps();
    }


}