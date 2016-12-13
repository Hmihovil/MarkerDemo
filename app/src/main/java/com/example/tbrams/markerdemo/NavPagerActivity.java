package com.example.tbrams.markerdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;

import java.util.List;


public class NavPagerActivity extends FragmentActivity {

    private static final String EXTRA_MARKER_ID = "com.example.tbrams.markerdemo.marker_id";
    private ViewPager mViewPager;
    private List<MarkerObject> markerList;


    public static Intent newIntent(Context packageContext, String markerID) {
        Intent intent = new Intent(packageContext, NavPagerActivity.class);
        intent.putExtra(EXTRA_MARKER_ID, markerID);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_info_pager);

        String markerID = (String) getIntent().getSerializableExtra(EXTRA_MARKER_ID);
        Log.d("TBR:","NavPagerActivity/onCreate - intent/markerID: "+markerID);

        mViewPager = (ViewPager) findViewById(R.id.activity_nav_info_pager);
        markerList = MarkerLab.getMarkerLab(this).getMarkers();

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                Marker m=markerList.get(position).getMarker();
                return InfoEditFragment.newInstance(m.getId());
            }

            @Override
            public int getCount() {
                return markerList.size();
            }
        });

        // Set current ID in ViewPAger
        for (int i = 0; i < markerList.size(); i++) {
            if (markerList.get(i).getMarker().getId().equals(markerID)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
}
