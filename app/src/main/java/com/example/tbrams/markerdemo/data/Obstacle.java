package com.example.tbrams.markerdemo.data;


import android.content.ContentValues;

import com.example.tbrams.markerdemo.components.Util;
import com.example.tbrams.markerdemo.db.ObstacleTable;
import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

public class Obstacle {
    private String sid;
    private LatLng position;
    private String name;
    private String what;
    private int elevation;
    private int height;


    public Obstacle(String name, String what, String location, int elev, int height) {

        this.sid = String.valueOf(UUID.randomUUID());
        this.what=what;
        this.name = name;
        this.position = Util.convertVFG(location);
        this.elevation=elev;
        this.height=height;
    }

    public Obstacle(String name, String what, double lat, double lon, int elevation, int height) {

        this.sid = String.valueOf(UUID.randomUUID());
        this.what=what;
        this.name = name;
        this.position = new LatLng(lat, lon);
        this.elevation=elevation;
        this.height=height;
    }


    public String getId() {
        return sid;
    }

    public LatLng getPosition() {
        return position;
    }

    public String getWhat() {
        return what;
    }

    public String getName() {
        return name;
    }

    public int getElevation() { return elevation; }

    public int getHeight() { return height; }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(ObstacleTable.COLUMN_ID,   sid);
        values.put(ObstacleTable.COLUMN_NAME, name);
        values.put(ObstacleTable.COLUMN_WHAT, what);
        values.put(ObstacleTable.COLUMN_LAT,  position.latitude);
        values.put(ObstacleTable.COLUMN_LON,  position.longitude);
        values.put(ObstacleTable.COLUMN_ELEVATION, elevation);
        values.put(ObstacleTable.COLUMN_HEIGHT, height);

        return values;
    }
}
