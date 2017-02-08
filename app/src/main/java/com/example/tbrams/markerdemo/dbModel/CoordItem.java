package com.example.tbrams.markerdemo.dbModel;

import android.content.ContentValues;

import com.example.tbrams.markerdemo.db.CoordTable;
import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

public class CoordItem {

    private String coordId;
    private String areaId;
    private LatLng coordPosistion;
    private int coordNumber;

    public CoordItem() {
        coordId = UUID.randomUUID().toString();
        areaId = "";
        coordPosistion = null;
        coordNumber = 0;
    }


    public CoordItem(String coordId, String areaId, LatLng coordPosistion, int coordNumber) {
        if (coordId != null) {
            this.coordId = coordId;
        } else {
            coordId = UUID.randomUUID().toString();
        }
        this.areaId = areaId;
        this.coordPosistion = coordPosistion;
        this.coordNumber = coordNumber;
    }

    public String getCoordId() {
        return coordId;
    }

    public void setCoordId(String coordId) {
        this.coordId = coordId;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public LatLng getCoordPosistion() {
        return coordPosistion;
    }

    public void setCoordPosition(LatLng coordPosistion) {
        this.coordPosistion = coordPosistion;
    }

    public int getCoordNumber() {
        return coordNumber;
    }

    public void setCoordNumber(int coordNumber) {
        this.coordNumber = coordNumber;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues(4);

        values.put(CoordTable.COLUMN_ID, coordId);
        values.put(CoordTable.COLUMN_AREA_ID, areaId);
        values.put(CoordTable.COLUMN_LAT, coordPosistion.latitude);
        values.put(CoordTable.COLUMN_LON, coordPosistion.longitude);
        values.put(CoordTable.COLUMN_SEQ, coordNumber);

        return values;
    }

}
