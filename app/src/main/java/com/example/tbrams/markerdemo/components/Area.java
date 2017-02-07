package com.example.tbrams.markerdemo.components;


import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.UUID;

public class Area {

    public static final int CTR=1;
    public static final int TMA=2;

    private String id;
    private String name;
    private String extraName;
    private int category;
    private AreaPolygon polygon;
    private int fromAlt, toAlt;
    private String radio;
    private String frequency;

    public Area(String name, int category, int fromAlt, int toAlt, List<LatLng> vList) {
        this.id= UUID.randomUUID().toString();
        this.name = name;
        this.extraName="";
        this.category = category;
        this.fromAlt = fromAlt;
        this.toAlt = toAlt;

        polygon = new AreaPolygon(this.id, vList);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtraName() {
        return extraName;
    }

    public void setExtraName(String extraName) {
        this.extraName = extraName;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public AreaPolygon getPolygon() {
        return polygon;
    }

    public void setPolygon(AreaPolygon polygon) {
        this.polygon = polygon;
    }

    public int getFromAlt() {
        return fromAlt;
    }

    public void setFromAlt(int fromAlt) {
        this.fromAlt = fromAlt;
    }

    public int getToAlt() {
        return toAlt;
    }

    public void setToAlt(int toAlt) {
        this.toAlt = toAlt;
    }

    public String getRadio() {
        return radio;
    }

    public void setRadio(String radio) {
        this.radio = radio;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
}
