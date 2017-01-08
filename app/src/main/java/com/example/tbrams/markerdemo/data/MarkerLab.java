package com.example.tbrams.markerdemo.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class MarkerLab {
    private static MarkerLab sMarkerLab;
    private List<MarkerObject> mMarkerlist;
    private String             mTripName;


    private MarkerLab(Context context) {
        mMarkerlist = new ArrayList<>();
        mTripName   = new String();
    }

    public static MarkerLab getMarkerLab(Context context){
        if (sMarkerLab==null) {
            sMarkerLab = new MarkerLab(context);
        }
        return sMarkerLab;
    }

    public List<MarkerObject> getMarkers() {
        return mMarkerlist;
    }
    public String getTripName() { return mTripName; }
    public void setTripName(String name) { this.mTripName = name; }


}
