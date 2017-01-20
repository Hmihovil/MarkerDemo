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

public class DetailPagerActivity extends AppCompatActivity {

    private static final String EXTRA_MARKER_ID = "com.example.tbrams.markerdemo.marker_id";
    private ViewPager mViewPager;
    private List<MarkerObject> markerList;
    MarkerLab markerLab = MarkerLab.getMarkerLab(this);

    public static Intent newIntent(Context packageContext, int markerIndex) {
        Intent intent = new Intent(packageContext, DetailPagerActivity.class);
        intent.putExtra(EXTRA_MARKER_ID, markerIndex);
        return intent;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_info_pager);

        this.setTitle(markerLab.getTripName());

        // Get markerIndex from arguments
        final int markerIndex = (int) getIntent().getSerializableExtra(EXTRA_MARKER_ID);
        Log.d("TBR:","DetailPagerActivity/onCreate - intent/markerIndex: "+markerIndex);

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
                return DetailInfoFragment.newInstance(position);
            }

            @Override
            public int getCount() {
                return markerList.size()-1;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                // Generate title based on item position
                return markerList.get(position+1).getText();
            }
        });


        // Set current markerIndex in ViewPAger
        mViewPager.setCurrentItem(markerIndex);

    }
}
