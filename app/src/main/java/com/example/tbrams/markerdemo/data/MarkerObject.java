package com.example.tbrams.markerdemo.data;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.UUID;

public class MarkerObject implements Parcelable {
    private Integer myIndex;    // Internal index
    private String myId;        // WpId
    private String myText;      // WP Name
    private String myNote;      // WP Notes
    private Marker myMarker;    // Copy of marker object

    // Navigational parameters that are set by user
    private double mTAS;
    private double mMIN_ALT;
    private double mALT;
    private double mWindStr;
    private double mWindDir;

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
    private double mATO;
    private double mDiff;

    private ArrayList<Pejling> mPejlinger;

    protected MarkerObject(Parcel in) {
        myText = in.readString();
        myNote = in.readString();
        mTAS = in.readDouble();
        mMIN_ALT = in.readDouble();
        mALT = in.readDouble();
        mWindStr = in.readDouble();
        mWindDir = in.readDouble();
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
        mATO = in.readDouble();
        mDiff = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(myText);
        dest.writeString(myNote);
        dest.writeDouble(mTAS);
        dest.writeDouble(mMIN_ALT);
        dest.writeDouble(mALT);
        dest.writeDouble(mWindStr);
        dest.writeDouble(mWindDir);
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

    public String getNote() {
        return myNote;
    }
    public void setNote(String text) {
        myNote = text;
        myMarker.setSnippet(text);
    }


    // Navigational getters & setters


    public String getMyId() {
        return myId;
    }
    public void setMyId(String myId) {
        // If no id is given, create a unique ID string
        if (myId==null) {
            this.myId = UUID.randomUUID().toString();
        } else {
            this.myId = myId;
        }
    }

    public double getTAS() {return mTAS;}
    public double getMIN_ALT() {return mMIN_ALT;}
    public double getALT() {return mALT;}
    public double getWindStr() {return mWindStr;}
    public double getWindDir() {return mWindDir;}

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
    public double getATO() { return mATO; }
    public double getDiff() { return mDiff; }

    // These two are done for all way points except for the starting point
    public void setDist(double dist) {mDist= dist/1852.;}    // Convert to nautical miles
    public void setTT(double tt) {mTT = (tt+360)%360;}       // keen between 0 to 360 degrees

    public void setETO(double ETO) { mETO = ETO; }
    public void setATO(double ATO) {
        mATO  = ATO;
        mDiff = mETO-mATO;  // positive means ahead of schedule
    }



    // Calculate navigational factors based on global nav values
    public void calcIAS(double tas, double alt)
    {
        mALT=alt;
        mIAS = tas/(1+alt/1000.*0.02);
    }

    public void calcGS(double tas, double windDirection, double windStrength) {
        double a = windStrength*Math.sin(Math.toRadians(windDirection-mTT))/tas;
        double b=  windStrength*Math.cos(Math.toRadians(windDirection-mTT));

        mGS = Math.sqrt(1-Math.pow(a,2))*(tas-b);
    }

    public void calcWCA(double tas, double windDirection, double windStrength) {
        mTAS = tas;
        mWindDir =windDirection;
        mWindStr =windStrength;

        mWCA = Math.toDegrees(Math.atan(windStrength*Math.sin(Math.toRadians(windDirection-mTT))/tas));
    }

    public void calcTH() {
        mTH = (mTT+mWCA+360.)%360;
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

    public void setALT(double ALT) { mALT = ALT; }

    public MarkerObject(Marker marker, String text, String snippet, ArrayList<Pejling> pejlinger){
        setMyId(null);
        myIndex  = null;
        myMarker = null;
        mPejlinger = null;
        mMIN_ALT=1000; // In Denmark...
        mTAS=0;
        mALT=0;
        mWindStr =0;
        mWindDir =0;

        mDist=0;
        mGS=0;
        mTT=0;
        mWCA=0;
        mTH=0;

        // Find Magnetic declination
        MagneticModel magModel = new MagneticModel();
        magModel.setLocation(marker.getPosition().latitude, marker.getPosition().longitude);
        mVAR= magModel.getDeclination();
        mMH=0;
        mTIME=0;
        mETO=0;

        myText = text;
        myNote = snippet;
        myMarker = marker;
        mPejlinger = new ArrayList<>();
        mPejlinger=pejlinger;
    }

    public MarkerObject() {
        setMyId(null);
        myIndex  = null;
        myMarker = null;
        mPejlinger = null;
        mMIN_ALT=1000; // In Denmark...
        mTAS=0;
        mALT=0;
        mWindStr =0;
        mWindDir =0;

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
        myNote = "";
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
