package com.example.tbrams.markerdemo.data;


import android.content.ContentValues;

import com.example.tbrams.markerdemo.db.RPTable;
import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

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



    // Convert format used in VFG Denmark ADC to internal location form
    // For example "57 05 34.04N 009 50 56.99E"

    // Also supports the format "57 05 34 04N 009 50 56 99E" sometimes used for private aerodromes

    private LatLng convertVFG(String input) {
        String[] parts;
        parts = input.split(" ");
        parts[2] = removeLastChar(parts[2]);
        parts[5] = removeLastChar(parts[5]);

        Double lat = Double.parseDouble(parts[0])+Double.parseDouble(parts[1])/60+Double.parseDouble(parts[2])/3600;
        Double lon = Double.parseDouble(parts[3])+Double.parseDouble(parts[4])/60+Double.parseDouble(parts[5])/3600;

        return new LatLng(lat, lon);
    }

    private static String removeLastChar(String str) {
        return str.substring(0,str.length()-1);
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
