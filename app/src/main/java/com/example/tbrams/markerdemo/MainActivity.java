package com.example.tbrams.markerdemo;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.tbrams.markerdemo.db.DbAdmin;
import com.example.tbrams.markerdemo.dbModel.TripItem;

import java.util.List;


/*
 * this class is use to display a list of flight plans on file. When in data browse mode
 * it will display way points using DetailActivity and otherwise it will use MarkerDemoActivity
 * to present the plan on a map
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TBR:MainActivity";

    DbAdmin mDbAdmin;
    RecyclerView mRecyclerView;
    TripAdapter mTripAdapter;
    private static Context mContext;
    public static View mBackgroundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;

        // Get a handle to the Database Admin helper and prepare the database
        mDbAdmin = new DbAdmin(this);


        updateTitle();

        setContentView(R.layout.activity_route);

        mBackgroundView = findViewById(R.id.activity_main);

        // Get a reference to the layout for the recyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.rvItems);

        // Display info on trips available in the database
        displayTrips(null);

    }


    /*
     * This is used from DetailActivity to maintain same context as the master list
     */
    public static Context getContext() {
        return mContext;
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
        mDbAdmin.open();
        List<TripItem> mListFromDB = mDbAdmin.getAllTrips(filter);
        mDbAdmin.close();

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
        getMenuInflater().inflate(R.menu.db_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Toggle usage mode between database maintenance and select trip mode
                // In select trip: click on trip -> WP inspection, long click -> select trip
                //                 click on wp -> you selected, long click -> Nothing
                // In db maintenance: Click on trip -> WP inspection, long click -> delete trip
                //                    click on wp -> you selected, long click -> delete wp

                mDbAdmin.toggleMaintenanceMode();
                if (mDbAdmin.isInMaintenanceMode())
                    showGuide();

                updateTitle();
                TripAdapter.updateBackgroundColor();

                return true;

        }
        return super.onOptionsItemSelected(item);
    }




    private void updateTitle() {
        if (mDbAdmin.isInMaintenanceMode())
            setTitle(getString(R.string.title_maintenance));
        else
            setTitle(getString(R.string.title_trip));
    }

    private void showGuide() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
        builder.setTitle("Notice");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.guide_panel, (ViewGroup) this.findViewById(android.R.id.content), false);

        // Set up the input
        TextView guide = (TextView) viewInflated.findViewById(R.id.guide_text);
        guide.setText(R.string.db_admin_guide);
        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        builder.show();

    }

}

