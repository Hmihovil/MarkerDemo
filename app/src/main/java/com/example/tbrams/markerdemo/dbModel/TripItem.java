package com.example.tbrams.markerdemo.dbModel;


import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.tbrams.markerdemo.db.TripTable;

import java.util.UUID;

public class TripItem implements Parcelable {
    private String tripId;
    private String tripName;
    private String tripDate;
    private Double tripDistance;


    public TripItem() {
    }

    public TripItem(String tripId, String tripName, String tripDate, Double tripDistance) {

        if (tripId == null) {
            this.tripId= UUID.randomUUID().toString();
        } else {
            this.tripId=tripId;
        }
        this.tripName = tripName;
        this.tripDate = tripDate;
        this.tripDistance = tripDistance;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getTripDate() {
        return tripDate;
    }

    public void setTripDate(String tripDate) {
        this.tripDate = tripDate;
    }

    public Double getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(Double tripDistance) {
        this.tripDistance = tripDistance;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues(4);

        values.put(TripTable.COLUMN_ID,   tripId);
        values.put(TripTable.COLUMN_NAME, tripName);
        values.put(TripTable.COLUMN_DIST, tripDistance);
        values.put(TripTable.COLUMN_DATE, tripDate);

        return values;
    }

    @Override
    public String toString() {
        return "TripItem{" +
                "tripId='"        + tripId + '\'' +
                ", tripName='"    + tripName + '\'' +
                ", tripDate='"    + tripDate + '\'' +
                ", tripDistance=" + tripDistance +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.tripId);
        dest.writeString(this.tripName);
        dest.writeString(this.tripDate);
        dest.writeValue(this.tripDistance);
    }

    protected TripItem(Parcel in) {
        this.tripId = in.readString();
        this.tripName = in.readString();
        this.tripDate = in.readString();
        this.tripDistance = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Creator<TripItem> CREATOR = new Creator<TripItem>() {
        @Override
        public TripItem createFromParcel(Parcel source) {
            return new TripItem(source);
        }

        @Override
        public TripItem[] newArray(int size) {
            return new TripItem[size];
        }
    };
}
