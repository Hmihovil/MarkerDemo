package com.example.tbrams.markerdemo.data;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class MarkerObject implements Parcelable {
    private Integer myIndex;
    private String myText;
    private String mySnippet;
    private Marker myMarker;

    // Navigational parameters that are fed in
    private double mTAS;
    private double mMIN_ALT;
    private double mALT;
    private double mWindStrenght;
    private double mWindDirection;

    // Navigational parameters that are calculated
    private double mDist;
    private double mIAS;
    private double mGS;
    private double mTT;
    private double mWCA;
    private double mTH;
    private double mVAR;
    private double mMH;

    // Timing vars
    private double mTIME;
    private double mETO;
    private double mRETO;
    private double mATO;
    private double mDiff;

    private MagneticModel magModel=new MagneticModel();

    private ArrayList<Pejling> mPejlinger;

    protected MarkerObject(Parcel in) {
        myText = in.readString();
        mySnippet = in.readString();
        mTAS = in.readDouble();
        mMIN_ALT = in.readDouble();
        mALT = in.readDouble();
        mWindStrenght = in.readDouble();
        mWindDirection = in.readDouble();
        mDist = in.readDouble();
        mIAS = in.readDouble();
        mGS = in.readDouble();
        mTT = in.readDouble();
        mWCA = in.readDouble();
        mTH = in.readDouble();
        mVAR = in.readDouble();
        mMH = in.readDouble();
        mTIME = in.readDouble();
        mETO = in.readDouble();
        mRETO = in.readDouble();
        mATO = in.readDouble();
        mDiff = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(myText);
        dest.writeString(mySnippet);
        dest.writeDouble(mTAS);
        dest.writeDouble(mMIN_ALT);
        dest.writeDouble(mALT);
        dest.writeDouble(mWindStrenght);
        dest.writeDouble(mWindDirection);
        dest.writeDouble(mDist);
        dest.writeDouble(mIAS);
        dest.writeDouble(mGS);
        dest.writeDouble(mTT);
        dest.writeDouble(mWCA);
        dest.writeDouble(mTH);
        dest.writeDouble(mVAR);
        dest.writeDouble(mMH);
        dest.writeDouble(mTIME);
        dest.writeDouble(mETO);
        dest.writeDouble(mRETO);
        dest.writeDouble(mATO);
        dest.writeDouble(mDiff);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MarkerObject> CREATOR = new Creator<MarkerObject>() {
        @Override
        public MarkerObject createFromParcel(Parcel in) {
            return new MarkerObject(in);
        }

        @Override
        public MarkerObject[] newArray(int size) {
            return new MarkerObject[size];
        }
    };

    public Marker getMarker() {
        return myMarker;
    }

    public void setMarker(Marker marker) {
        myMarker = marker;
    }


    public String getText() {
        return myText;
    }

    public void setText(String text) {
        myText = text;
        myMarker.setTitle(text);
    }

    public int getIndex() {
        return myIndex;
    }

    public void setIndex(int index) {
        myIndex = index;
    }

    public String getSnippet() {
        return mySnippet;
    }

    public void setSnippet(String text) {
        mySnippet = text;
        myMarker.setSnippet(text);
    }


    // Navigational getters & setters


    public double getTAS() {return mTAS;}
    public double getMIN_ALT() {return mMIN_ALT;}
    public double getALT() {return mALT;}
    public double getWindStrenght() {return mWindStrenght;}
    public double getWindDirection() {return mWindDirection;}

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
    public double getRETO() { return mRETO; }
    public double getATO() { return mATO; }
    public double getDiff() { return mDiff; }

    // These two are done for all way points except for the starting point
    public void setDist(double dist) {mDist= dist/1852.;}
    public void setTT(double tt) {mTT = (tt+360)%360;}

    public void setETO(double ETO) { mETO = ETO; }
    public void setATO(double ATO) {
        mATO = ATO;
        mDiff = mETO-mATO;  // positive means ahead of schedule
    }
    public void setRETO(double RETO) { mRETO = RETO; }

    // Calculate navigational factors based on global nav values
    public void calcIAS(double tas, double alt)
    {
        mALT=alt;
        mIAS = tas/(1+alt/1000.*0.02);
    }

    public void calcGS(double tas, double windDirection, double windStrength) {
        double a = windStrength*Math.sin(windDirection-mTT)/tas;
        double b=  windStrength*Math.cos(windDirection-mTT);

        mGS = Math.sqrt(1-Math.pow(a,2))*(tas-b);
    }

    public void calcWCA(double tas, double windDirection, double windStrength) {
        mTAS = tas;
        mWindDirection=windDirection;
        mWindStrenght=windStrength;

        mWCA = Math.atan(windStrength*Math.sin(windDirection-mTT)/tas);
    }

    public void calcTH() {
        mTH = (mTT+mWCA+360.)%360;
    }
    public void calcVAR() {mVAR = -3.; }
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


    public MarkerObject(Marker marker, String text, String snippet, ArrayList<Pejling> pejlinger){
        myIndex  = null;
        myMarker = null;
        mPejlinger = null;
        mMIN_ALT=1000; // In Denmark...
        mTAS=0;
        mALT=0;
        mWindStrenght=0;
        mWindDirection=0;

        mDist=0;
        mGS=0;
        mTT=0;
        mWCA=0;
        mTH=0;

        magModel.setLocation(marker.getPosition().latitude, marker.getPosition().longitude);
        mVAR=magModel.getDeclination();
        mMH=0;
        mTIME=0;
        mETO=0;

        myText = text;
        mySnippet = snippet;
        myMarker = marker;
        mPejlinger = new ArrayList<>();
        mPejlinger=pejlinger;
    }

    public MarkerObject() {
        myIndex  = null;
        myMarker = null;
        mPejlinger = null;
        mMIN_ALT=1000; // In Denmark...
        mTAS=0;
        mALT=0;
        mWindStrenght=0;
        mWindDirection=0;

        mDist=0;
        mGS=0;
        mTT=0;
        mWCA=0;
        mTH=0;
        mVAR=0;
        mMH=0;
        mTIME=0;
        mETO=0;

        myText = "";
        mySnippet = "";
        myMarker = null;
        mPejlinger = new ArrayList<>();

    }

    public ArrayList<Pejling> getPejlinger() {
        return mPejlinger;
    }

    public void setPejlinger(ArrayList<Pejling> pejlinger) {
        mPejlinger = pejlinger;
    }

}
