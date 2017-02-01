package com.example.tbrams.markerdemo.data;


import android.content.ContentValues;

import com.example.tbrams.markerdemo.db.RPTable;
import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

import static com.example.tbrams.markerdemo.components.Util.convertVFG;

public class ReportingPoint {
    private String sid;
    private LatLng position;
    private String name;
    private String aerodrome;

    public ReportingPoint(String ad, String name, String position) {

        // Position comes as a string in this format: "57 01 58N 009 51 10E"

        this.sid = String.valueOf(UUID.randomUUID());
        this.aerodrome=ad;
        this.name = name;
        this.position = convertVFG(position);
    }

    public ReportingPoint(String ad, String name, double lat, double lon) {

        this.sid = String.valueOf(UUID.randomUUID());
        this.aerodrome=ad;
        this.name = name;
        this.position = new LatLng(lat, lon);
    }


    public String getSid() {
        return sid;
    }

    public LatLng getPosition() {
        return position;
    }

    public String getAerodrome() {
        return aerodrome;
    }

    public String getName() {
        return name;
    }


    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(RPTable.COLUMN_ID,   sid);
        values.put(RPTable.COLUMN_NAME, name);
        values.put(RPTable.COLUMN_AD,   aerodrome);
        values.put(RPTable.COLUMN_LAT,  position.latitude);
        values.put(RPTable.COLUMN_LON,  position.longitude);

        return values;
    }
}
