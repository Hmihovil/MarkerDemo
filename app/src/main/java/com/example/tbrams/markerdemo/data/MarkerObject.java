package com.example.tbrams.markerdemo.data;


import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class MarkerObject {
    private Integer myIndex;
    private String myText;
    private String mySnippet;
    private Marker myMarker;


    private ArrayList<Pejling> mPejlinger;

    public Marker getMarker() {
        return myMarker;
    }

    public void setMarker(Marker myMarker) {
        this.myMarker = myMarker;
    }


    public String getText() {
        return myText;
    }

    public void setText(String text) {
        this.myText = text;
    }

    public int getIndex() {
        return myIndex;
    }

    public void setIndex(int index) {
        this.myIndex = index;
    }

    public String getSnippet() {
        return mySnippet;
    }

    public void setSnippet(String text) {
        this.mySnippet = text;
    }

    public MarkerObject() {
        myIndex  = null;
        myText = null;
        myMarker = null;
        mPejlinger = null;
    }

    public MarkerObject(Marker marker, String text, String snippet, ArrayList<Pejling> pejlinger){
        myText = text;
        mySnippet = snippet;
        myMarker = marker;
        mPejlinger = new ArrayList<>();
        mPejlinger=pejlinger;
    }


    public ArrayList<Pejling> getPejlinger() {
        return mPejlinger;
    }

    public void setPejlinger(ArrayList<Pejling> pejlinger) {
        mPejlinger = pejlinger;
    }

}
