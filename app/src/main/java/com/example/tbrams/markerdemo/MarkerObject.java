package com.example.tbrams.markerdemo;


import com.google.android.gms.maps.model.Marker;

public class MarkerObject {
    private Integer myIndex;
    private String myText;
    private String mySnippet;
    private Marker myMarker;

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
    }

    public MarkerObject(Marker marker, String text, String snippet){
        myText = text;
        mySnippet = snippet;
        myMarker = marker;
    }

}
