package com.example.tbrams.markerdemo.dbModel;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

public class AreaCoordItem extends AreaItem {

    private ArrayList<LatLng> mCoordList;

    public AreaCoordItem() {
        mCoordList = new ArrayList<>();
    }

    public ArrayList<LatLng> getCoordList() {
        return mCoordList;
    }

    public void setCoordList(ArrayList<LatLng> coordList) {
        mCoordList = coordList;
    }
}
