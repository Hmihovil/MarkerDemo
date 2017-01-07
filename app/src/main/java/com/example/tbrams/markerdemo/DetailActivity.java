package com.example.tbrams.markerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.example.tbrams.markerdemo.db.DataSource;
import com.example.tbrams.markerdemo.dbModel.WpItem;

import java.util.List;


@SuppressWarnings("FieldCanBeLocal")
public class DetailActivity extends AppCompatActivity {

    private TextView tvName;

    DataSource   mDataSource;
    List<WpItem> mListFromDB;
    RecyclerView mRecyclerView;
    WpAdapter    mWpAdapter;
    String       mId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get trip id from intent extra storage
        mId = getIntent().getExtras().getString(TripAdapter.TRIP_KEY);
        if (mId == null) {
            Log.d("TBR:", "DetailActivity received a null id from extras");
        }

        // Get a reference to the recyclerView and make sure we have defined
        // a LayoutManager ... otherwise it will crash
        mRecyclerView = (RecyclerView) findViewById(R.id.rvItems);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        displayWps(mId);
    }


    private void displayWps(String id) {

        // Get the WP data ready for display
        mDataSource = new DataSource(this);
        mDataSource.open();
        mListFromDB = mDataSource.getAllWps(id);

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
        displayWps(mId);
    }


}