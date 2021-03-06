package com.example.tbrams.markerdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.data.MarkerObject;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import static com.example.tbrams.markerdemo.InfoEditFragment.EXTRA_MARKER_ID;


public class NavPagerActivity extends AppCompatActivity {

    public static final int ACTION_UPDATE = 1;
    private static final String TAG = "TBR:";

    private ViewPager mViewPager;
    private List<MarkerObject> markerList;
    private static boolean mSomethingUpdated =false;



    public static Intent newIntent(Context packageContext, int markerIndex) {
        Intent intent = new Intent(packageContext, NavPagerActivity.class);
        intent.putExtra(EXTRA_MARKER_ID, markerIndex);
        return intent;
    }


    public static void setSomethingUpdated(boolean flag) {
        Log.d(TAG,"NavpagerActivity, setSomethingUpdated called");
        mSomethingUpdated = flag;
    }

    /*
     * Helper function used to send results back to the main activity where it can
     * be processed in the onActivityResult method
     */
    private void sendResult(int resultCode, int markerIndex) {
        Log.d("TBR", "NavPagerActivity, SedResult called");

        Intent intent = new Intent();
        intent.putExtra(EXTRA_MARKER_ID, markerIndex);
        setResult(resultCode, intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TBR", "NavLagerActivity, onActivityResult called");
    }


    @Override
    public void onBackPressed() {

        Log.d(TAG,"NavPagerActivity, onBackPressed called");

        int count = getFragmentManager().getBackStackEntryCount();
        Log.d(TAG,"NavPagerActivity, BackStackEntryCount: "+count);

        if (count == 0) {
            if (mSomethingUpdated) sendResult(ACTION_UPDATE, 0);
            super.onBackPressed();

        } else {
            Log.d(TAG, "popping Backstack");
            getFragmentManager().popBackStack();
        }

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout with SlidingTabs and a PageViewer
        setContentView(R.layout.nav_info_pager);

        // Get markerIndex from arguments
        int markerIndex = (int) getIntent().getSerializableExtra(EXTRA_MARKER_ID);

        Log.d(TAG,"NavPagerActivity/onCreate - received MarkerIndex via intent: "+markerIndex);

        mViewPager = (ViewPager) findViewById(R.id.activity_nav_info_pager);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);


        markerList = MarkerLab.getMarkerLab(this).getMarkers();

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                Marker m=markerList.get(position).getMarker();
                return InfoEditFragment.newInstance(position);
            }

            @Override
            public int getCount() {
                return markerList.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                // Generate title based on item position, and we do not want zero as first ;-)
                return String.format("#%d", position+1);
            }
        });


        // Set current markerIndex in ViewPager
        mViewPager.setCurrentItem(markerIndex);

    }
}
