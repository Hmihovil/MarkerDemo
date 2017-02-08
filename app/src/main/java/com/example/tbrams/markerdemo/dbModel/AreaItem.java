package com.example.tbrams.markerdemo.dbModel;

import android.content.ContentValues;

import com.example.tbrams.markerdemo.db.AreaTable;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.UUID;

public class AreaItem {
    public static final int CTR = 1;
    public static final int TMA = 2;
    public static final int TIZ = 3;
    public static final int RESTRICTED = 4;
    public static final int PROHIBITED = 5;
    public static final int DANGER = 6;
    public static final int SENSITIVE = 7;

    private String AreaId;
    private String AreaName;
    private int AreaType;
    private String AreaIdent;
    private String AreaClass;
    private int AreaFromAlt;
    private int AreaToAlt;
    private List<LatLng> coordinates;

    public AreaItem() {
        this.AreaId = UUID.randomUUID().toString();
        this.AreaName = "";
        this.AreaType = 0;
        this.AreaFromAlt = 0;
        this.AreaToAlt = 0;
        this.AreaClass = "";
        this.AreaIdent = "";
    }


    public AreaItem(String areaId, String areaName, int areaType, String areaIdent, String areaClass, int areaFromAlt, int areaToAlt) {
        if (areaId != null) {
            AreaId = areaId;
        } else {
            this.AreaId = UUID.randomUUID().toString();
        }
        AreaName = areaName;
        AreaType = areaType;
        AreaIdent = areaIdent;
        AreaClass = areaClass;
        AreaFromAlt = areaFromAlt;
        AreaToAlt = areaToAlt;
    }

    public String getAreaId() {
        return AreaId;
    }

    public void setAreaId(String areaId) {
        AreaId = areaId;
    }

    public String getAreaName() {
        return AreaName;
    }

    public void setAreaName(String areaName) {
        AreaName = areaName;
    }

    public int getAreaType() {
        return AreaType;
    }

    public void setAreaType(int areaType) {
        AreaType = areaType;
    }

    public String getAreaIdent() {
        return AreaIdent;
    }

    public void setAreaIdent(String areaIdent) {
        AreaIdent = areaIdent;
    }

    public String getAreaClass() {
        return AreaClass;
    }

    public void setAreaClass(String areaClass) {
        AreaClass = areaClass;
    }

    public int getAreaFromAlt() {
        return AreaFromAlt;
    }

    public void setAreaFromAlt(int areaFromAlt) {
        AreaFromAlt = areaFromAlt;
    }

    public int getAreaToAlt() {
        return AreaToAlt;
    }

    public void setAreaToAlt(int areaToAlt) {
        AreaToAlt = areaToAlt;
    }

    public List<LatLng> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<LatLng> coordinates) {
        this.coordinates = coordinates;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues(4);

        values.put(AreaTable.COLUMN_ID, AreaId);
        values.put(AreaTable.COLUMN_NAME, AreaName);
        values.put(AreaTable.COLUMN_IDENT, AreaIdent);
        values.put(AreaTable.COLUMN_TYPE, AreaType);
        values.put(AreaTable.COLUMN_CLASS, AreaClass);
        values.put(AreaTable.COLUMN_FROM_ALT, AreaFromAlt);
        values.put(AreaTable.COLUMN_TO_ALT, AreaToAlt);

        return values;
    }

}
