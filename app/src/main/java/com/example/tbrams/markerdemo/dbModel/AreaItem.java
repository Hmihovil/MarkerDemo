package com.example.tbrams.markerdemo.dbModel;

import android.content.ContentValues;

import com.example.tbrams.markerdemo.components.OpenAirInterpreter;
import com.example.tbrams.markerdemo.db.AreaTable;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AreaItem {
    public static final int CTR = 1;
    public static final int TMA = 2;
    public static final int TIZ = 3;
    public static final int TIA = 4;
    public static final int LTA = 5;

    public static final int RESTRICTED = 10;
    public static final int PROHIBITED = 11;
    public static final int DANGER = 12;
    public static final int SENSITIVE = 13;

    public static final int GLIDER = 20;
    public static final int PARACHUTE=21;

    private String mAreaId;
    private String mAreaName;
    private int mAreaType;
    private String mAreaIdent;
    private String mAreaClass;
    private int mAreaFromAlt;
    private int mAreaToAlt;
    private String mOAC;

    // Not used in database
    private ArrayList<LatLng> mCoordList;


    public AreaItem() {
        this.mAreaId = UUID.randomUUID().toString();
        this.mAreaName = "";
        this.mAreaType = 0;
        this.mAreaFromAlt = 0;
        this.mAreaToAlt = 0;
        this.mAreaClass = "";
        this.mAreaIdent = "";
        this.mOAC="";

        this.mCoordList = new ArrayList<>();

    }


    public AreaItem(String areaId, String areaName, int areaType, String areaIdent, String areaClass, int areaFromAlt, int areaToAlt, String definition) {
        if (areaId != null) {
            this.mAreaId = areaId;
        } else {
            this.mAreaId = UUID.randomUUID().toString();
        }
        this.mAreaName = areaName;
        this.mAreaType = areaType;
        this.mAreaIdent = areaIdent;
        this.mAreaClass = areaClass;
        this.mAreaFromAlt = areaFromAlt;
        this.mAreaToAlt = areaToAlt;
        this.mOAC = definition;

        OpenAirInterpreter interpreter = new OpenAirInterpreter();
        this.mCoordList = interpreter.generatePolygon(definition);

    }

    public String getAreaId() {
        return mAreaId;
    }

    public void setAreaId(String areaId) {
        mAreaId = areaId;
    }

    public String getAreaName() {
        return mAreaName;
    }

    public void setAreaName(String areaName) {
        mAreaName = areaName;
    }

    public int getAreaType() {
        return mAreaType;
    }

    public void setAreaType(int areaType) {
        mAreaType = areaType;
    }

    public String getAreaIdent() {
        return mAreaIdent;
    }

    public void setAreaIdent(String areaIdent) {
        mAreaIdent = areaIdent;
    }

    public String getAreaClass() {
        return mAreaClass;
    }

    public void setAreaClass(String areaClass) {
        mAreaClass = areaClass;
    }

    public int getAreaFromAlt() {
        return mAreaFromAlt;
    }

    public void setAreaFromAlt(int areaFromAlt) {
        mAreaFromAlt = areaFromAlt;
    }

    public int getAreaToAlt() {
        return mAreaToAlt;
    }

    public void setAreaToAlt(int areaToAlt) {
        mAreaToAlt = areaToAlt;
    }

    public String getDefinition() {
        return mOAC;
    }

    public void setDefinition(String OAC) {
        mOAC = OAC;
    }

    // This one is not used in db context - coordinates only exist at runtime
    public ArrayList<LatLng> getCoordList() {
        return mCoordList;
    }


    public ContentValues toContentValues() {
        ContentValues values = new ContentValues(4);

        values.put(AreaTable.COLUMN_ID, mAreaId);
        values.put(AreaTable.COLUMN_NAME, mAreaName);
        values.put(AreaTable.COLUMN_IDENT, mAreaIdent);
        values.put(AreaTable.COLUMN_TYPE, mAreaType);
        values.put(AreaTable.COLUMN_CLASS, mAreaClass);
        values.put(AreaTable.COLUMN_FROM_ALT, mAreaFromAlt);
        values.put(AreaTable.COLUMN_TO_ALT, mAreaToAlt);
        values.put(AreaTable.COLUMN_OAC, mOAC);

        return values;
    }

}
