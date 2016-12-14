package com.example.tbrams.markerdemo.data;

import com.google.android.gms.maps.model.LatLng;

public class NavAid {
    private String name;
    private LatLng position;

    public LatLng getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }


    public NavAid(String n, String lat, String lng) {
        // Convert the Lat and Lng strings to double values and use them to establish a position

        position = new LatLng(convert(lat), convert(lng));
        name = n;
    }


    /*
     * convert position from degrees, minutes and seconds format as found
     * in the NavAid list under VFG Denmark to decimal format used in Google Maps.
     *
     * Accepts input format like these:
     *    "570613N"
     *   "0095944E"
     */
    private static double convert(String ll) {
        double result=0;
        double dd,mm,ss;

        if (ll.charAt(ll.length()-1)=='E') {
            dd = Integer.parseInt(ll.substring(0, 3));
            mm = Integer.parseInt(ll.substring(3, 5));
            ss = Integer.parseInt(ll.substring(5, 7));
        }
        else {
            dd = Integer.parseInt(ll.substring(0,2));
            mm = Integer.parseInt(ll.substring(2,4));
            ss = Integer.parseInt(ll.substring(4,6));
        }
        return dd+mm/60+ss/3600;
    }
}



