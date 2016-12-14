package com.example.tbrams.markerdemo.data;

/* A structure like class needed to sort the list of available VORs
 * for each marker as well as to store in a shortlist for each marker
 */
public class Pejling implements Comparable<Pejling> {
    int markerIndex;
    double distance;
    double heading;

    public double getHeading() {
        return heading;
    }

    public double getDistance() {
        return distance;
    }

    public int getMarkerIndex() {
        return markerIndex;
    }


    public Pejling(int mindex, double dist, double h) {
        markerIndex=mindex;
        distance=dist;
        heading=h;
    }


    @Override
    public int compareTo(Pejling o) {
        // compareTo should return < 0 if this is supposed to be
        // less than other, > 0 if this is supposed to be greater than
        // other and 0 if they are supposed to be equal
        return (this.distance>o.distance? 1: (this.distance==o.distance?0: -1));
    }
}
