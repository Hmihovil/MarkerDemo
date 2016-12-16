package com.example.tbrams.markerdemo.data;


import android.util.Log;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class MarkerObject {
    private Integer myIndex;
    private String myText;
    private String mySnippet;
    private Marker myMarker;

    // Navigational parameters that are calculated
    private double mDist;
    private double mIAS;
    private double mGS;
    private double mTT;
    private double mWCA;
    private double mTH;
    private double mVAR;
    private double mMH;
    private double mTIME;
    private double mETO;



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


    // Navigational getters & setters
    public double getDist() {return mDist;}
    public double getIAS() {return mIAS;}
    public double getGS() {return mGS;}
    public double getTT() {return mTT;}
    public double getWCA() {return mWCA;}
    public double getTH() {return mTH;}
    public double getVAR() {return mVAR;}
    public double getMH() {return mMH;}
    public double getTIME() {return mTIME;}
    public double getETO() {return mETO;}

    // These two are done for all way points except for the starting point
    public void setDist(double dist) {mDist= dist/1852.; Log.d("TBR:", "mDist updated: "+mDist);}
    public void setTT(double tt) {mTT = (tt+360)%360;Log.d("TBR:", "mTT updated: "+mTT);}


    // Calculate navigational factors based on global nav values
    public void calcIAS(double tas, double alt)
    {
        mIAS = tas/(1+alt/1000.*0.02);
    }

    public void calcGS(double tas, double windDirection, double windStrength) {
        double a = windStrength*Math.sin(windDirection-mTT)/tas;
        double b=  windStrength*Math.cos(windDirection-mTT);

        mGS = Math.sqrt(1-Math.pow(a,2))*(tas-b);
    }

    public void calcWCA(double tas, double windDirection, double windStrength) {

        mWCA = Math.atan(windStrength*Math.sin(windDirection-mTT)/tas);
    }



    public void calcTH() {
        mTH = (mTT+mWCA+360.)%360;
    }


    public void calcVAR() {
        mVAR = -3.;
    }


    public void calcMH() {
        mMH = mTH-mVAR;
    }

    public void addStartTIME() {
        mTIME=mTIME+2.;
    }
    public void calcTIME() {
        mTIME = (mDist/mGS)*60.;
    }


    public void calcETO() {
        mETO = 0;
    }


    public MarkerObject() {
        myIndex  = null;
        myText = null;
        myMarker = null;
        mPejlinger = null;

        mDist=0;
        mGS=0;
        mTT=0;
        mWCA=0;
        mTH=0;
        mVAR=0;
        mMH=0;
        mTIME=0;
        mETO=0;
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
