package com.example.tbrams.markerdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
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

public class TimePagerActivity extends AppCompatActivity {
    private static final String EXTRA_MARKER_ID = "com.example.tbrams.markerdemo.marker_id";
    private ViewPager mViewPager;
    private List<MarkerObject> markerList;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_info_pager);


        int markerIndex = 0;
        Log.d("TBR:","TimePagerActivity/onCreate markerIndex: "+markerIndex);

        mViewPager = (ViewPager) findViewById(R.id.activity_nav_info_pager);
        markerList = MarkerLab.getMarkerLab(this).getMarkers();

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                Marker m=markerList.get(position).getMarker();
                return TimeFragment.newInstance(position);
            }

            @Override
            public int getCount() {
                return markerList.size();
            }
        });


        // Set current markerIndex in ViewPAger
        mViewPager.setCurrentItem(markerIndex);

    }

}
