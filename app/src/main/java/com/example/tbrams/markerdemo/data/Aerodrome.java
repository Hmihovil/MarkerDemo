package com.example.tbrams.markerdemo.data;


import android.content.ContentValues;

import com.example.tbrams.markerdemo.db.AdTable;
import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

import static com.example.tbrams.markerdemo.components.Util.convertVFG;

public class Aerodrome {

    public static final int PUBLIC=1;
    public static final int PRIVATE=2;
    public static final int GLIDER=3;
    public static final int HELI=4;
    public static final int RECREATIONAL=5;

    private String id;
    private String icaoName;
    private String name;
    private int    adType;
    private LatLng position;
    private String radio;
    private String freq;
    private String web;
    private String phone;
    private boolean PPR;
    private String activity;
    private String remarks;
    private String link;


    public Aerodrome() {
        this.id = UUID.randomUUID().toString();
    }

    public Aerodrome(String name, String fullname, String position, int adType, String radio, String freq, boolean ppr, String link) {
        this.id = UUID.randomUUID().toString();
        this.icaoName = name;
        this.name = fullname;
        this.position = convertVFG(position);
        this.adType=adType;
        this.radio=radio;
        this.freq=freq;
        this.PPR=ppr;
        this.link=link;

        this.web=null;
        this.phone=null;
        this.remarks=null;
        this.activity=null;
    }


// Special constructor for recreational airfields
    public Aerodrome(String fullname, String name, String position, int adType, String activity, String remarks) {
        this.id = UUID.randomUUID().toString();
        this.icaoName = name;
        this.name = fullname;
        this.position = convertVFG(position);
        this.adType=adType;
        this.activity= activity;
        this.remarks = remarks;

        this.radio=null;
        this.freq=null;
        this.PPR=false;
        this.link=null;

        this.web=null;
        this.phone=null;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getIcaoName() {
        return icaoName;
    }

    public void setIcaoName(String icaoName) {
        this.icaoName = icaoName;
    }

    public int getType() { return adType; }
    public void setType(int adType) { this.adType = adType; }

    public String getRadio() { return radio; }
    public void setRadio(String radio) { this.radio = radio; }

    public String getFreq() { return freq; }
    public void setFreq(String freq) { this.freq = freq; }

    public String getWeb() { return web; }
    public void setWeb(String web) { this.web = web; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isPPR() { return PPR; }
    public void setPPR(boolean PPR) { this.PPR = PPR; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(AdTable.COLUMN_ID,   id);
        values.put(AdTable.COLUMN_NAME, name);
        values.put(AdTable.COLUMN_ICAO, icaoName);
        values.put(AdTable.COLUMN_TYPE, adType);
        values.put(AdTable.COLUMN_LAT, position.latitude);
        values.put(AdTable.COLUMN_LON, position.longitude);
        values.put(AdTable.COLUMN_RADIO, radio);
        values.put(AdTable.COLUMN_FREQ, freq);
        values.put(AdTable.COLUMN_WEB, web);
        values.put(AdTable.COLUMN_PHONE, phone);
        values.put(AdTable.COLUMN_PPR, PPR);
        values.put(AdTable.COLUMN_ACTIVITY, activity);
        values.put(AdTable.COLUMN_REMARKS, remarks);

        return values;
    }
}
