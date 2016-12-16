package com.example.tbrams.markerdemo.data;


public class GlobalNavValData {
    private static double mMIN_ALT;
    private static double mALT;
    private static double mTAS;
    private static double mWINDfrom;
    private static double mWINDkts;


    public GlobalNavValData(double alt, double tas, String wind) {
        mALT = alt;
        mTAS = tas;
        mMIN_ALT = 1000;   // In Denmark ....
        mWINDfrom=Double.parseDouble(wind.split("/")[0]);
        mWINDkts=Double.parseDouble(wind.split("/")[0]);
    }

    public double getALT() {return mALT;}

    public void setALT(double ALT) {
        mALT = ALT;
    }

    public double getTAS() {return mTAS;}

    public void setTAS(double TAS) {
        mTAS = TAS;
    }

    public double getWINDfrom() {return mWINDfrom;}

    public void setWINDfrom(double WINDfrom) {
        mWINDfrom = WINDfrom;
    }

    public double getWINDkts() {return mWINDkts;}

    public void setWINDkts(double WINDkts) {
        mWINDkts = WINDkts;
    }

    public double getMIN_ALT() {return mMIN_ALT;}

    public void setMIN_ALT(double MIN_ALT) {
        mMIN_ALT = MIN_ALT;
    }


}
