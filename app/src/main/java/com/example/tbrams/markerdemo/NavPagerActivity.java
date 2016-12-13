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

        String markerID = getIntent().getStringExtra(EXTRA_MARKER_ID);


        mViewPager = (ViewPager) findViewById(R.id.activity_nav_info_pager);
        markerList = MarkerLab.getMarkerLab(this).getMarkers();

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public Fragment getItem(int position) {
                MarkerObject mo=markerList.get(position);
                return InfoEditFragment.newInstance(mo.getMarker().getId());
            }

            @Override
            public int getCount() {
                return markerList.size();
            }
        });
    }
}
