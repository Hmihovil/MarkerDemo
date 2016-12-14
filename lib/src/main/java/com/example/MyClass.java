package com.example;

import java.util.ArrayList;
import java.util.Collections;

public class MyClass {
    private static ArrayList<Pejling> plist= new ArrayList<>();
    public static void main(String args[]) {

        plist.add(new Pejling(1, 23.44));
        plist.add(new Pejling(2, 33.44));
        plist.add(new Pejling(3, 13.44));
        System.out.println("dumping plist ");
        dumpPlist();

        Collections.sort(plist);
        System.out.println("After sorting: ");
        dumpPlist();

    }


    static void dumpPlist() {
        for (int i=0;i<plist.size();i++){
            System.out.format("plist[%d], marker:%d, dist:%f", i, plist.get(i).markerID, plist.get(i).distance);
            System.out.println();
        }
    }
}

class Pejling implements Comparable<Pejling> {
    int markerID;
    double distance;
    public Pejling(int mid, double dist) {
        markerID=mid;
        distance=dist;
    }

    @Override
    public int compareTo(Pejling o) {
        // compareTo should return < 0 if this is supposed to be
        // less than other, > 0 if this is supposed to be greater than
        // other and 0 if they are supposed to be equal
        return (this.distance>o.distance? 1: (this.distance==o.distance?0: -1));
    }
}
