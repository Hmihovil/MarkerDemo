package com.example.tbrams.markerdemo.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class MarkerLab {
    private static MarkerLab sMarkerLab;
    private List<MarkerObject> mMarkerlist;

    private MarkerLab(Context context) {
        mMarkerlist = new ArrayList<>();
    }

    public List<MarkerObject> getMarkers() {
        return mMarkerlist;
    }

    public static MarkerLab getMarkerLab(Context context){
        if (sMarkerLab==null) {
            sMarkerLab = new MarkerLab(context);
        }
        return sMarkerLab;
    }
}
