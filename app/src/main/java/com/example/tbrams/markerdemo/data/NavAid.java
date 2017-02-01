package com.example.tbrams.markerdemo.data;

import android.content.ContentValues;

import com.example.tbrams.markerdemo.components.Util;
import com.example.tbrams.markerdemo.db.NavAidTable;
import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

import static com.example.tbrams.markerdemo.components.Util.parseLimitations;

public class NavAid {

    public static final int VOR =1;
    public static final int NDB=2;
    public static final int TACAN=3;
    public static final int DME=4;
    public static final int VORDME=5;
    public static final int VORTAC=6;
    public static final int LOCATOR =7;

    private String id;
    private int seq_id;
    private String name;
    private String ident;
    private String freq;
    private int naType;
    private int    max_range;
    private int    max_alt;
    private LatLng position;
    private Double elevation;

    public LatLng getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public NavAid() {
        this.id = UUID.randomUUID().toString();
    }

    public int getSeq_id() { return seq_id; }
    public void setSeq_id(int seq_id) { this.seq_id = seq_id; }

    public NavAid(String name, String ident, int kind, String latlong, String freq, String limitString, Double elevation) {
        position = Util.convertVFG(latlong);

        this.id = UUID.randomUUID().toString();

        this.name = name;
        this.ident=ident;
        this.naType = kind;
        this.freq = freq;

        this.seq_id=-1;

        int[] limits = parseLimitations(limitString);
        max_alt = limits[0];
        max_range=limits[1];

        if (elevation!=null)
           this.elevation = elevation;
        else
            this.elevation=0.0;
    }


    public int getMax_range() {
        return max_range;
    }

    public void setMax_range(int max_range) {
        this.max_range = max_range;
    }

    public int getMax_alt() {
        return max_alt;
    }

    public void setMax_alt(int max_alt) {
        this.max_alt = max_alt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return naType;
    }

    public void setType(int type) {
        this.naType = type;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getFreq() {
        return freq;
    }

    public void setFreq(String freq) {
        this.freq = freq;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }


    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(NavAidTable.COLUMN_ID,   id);
        values.put(NavAidTable.COLUMN_NAME, name);
        values.put(NavAidTable.COLUMN_IDENT, ident);
        values.put(NavAidTable.COLUMN_TYPE, naType);
        values.put(NavAidTable.COLUMN_LAT, position.latitude);
        values.put(NavAidTable.COLUMN_LON, position.longitude);
        values.put(NavAidTable.COLUMN_FREQ, freq);
        values.put(NavAidTable.COLUMN_MAX_ALT, max_alt);
        values.put(NavAidTable.COLUMN_MAX_DIST, max_range);
        values.put(NavAidTable.COLUMN_ELEV, elevation);

        return values;
    }
}



