package com.example.tbrams.markerdemo.data;

import com.google.android.gms.maps.model.LatLng;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.y;

public class NavAid {

    public static final int VOR =1;
    public static final int NDB=2;
    public static final int TACAN=3;
    public static final int DME=4;
    public static final int VORDME=5;
    public static final int VORTAC=6;

    private String name;
    private int    type;
    private int    max_range;
    private int    max_alt;
    private LatLng position;

    public LatLng getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }


 /*   public NavAid(String n, int what, String lat, String lng) {
        // Convert the Lat and Lng strings to double values and use them to establish a position

        position = new LatLng(convert(lat), convert(lng));
        name = n;
    }
*/

//    public NavAid(String name, String id, String what, String latlong, String limitString) {
    // work with a shorter version until we get it up an running...

    public NavAid(String name, int what, String latlong) {
        // Convert the Lat and Lng strings to double values and use them to establish a position

        position = parseCoordinates(latlong);

        this.name = name;
        this.type = what;
    }

//        max_alt = limits[0];
//        max_range=limits[1];


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

    public int getMax_range() {
        return max_range;
    }

    public void setMax_range(int max_range) {
        this.max_range = max_range;
    }

    public int getMax_alt() {
        return max_alt;
    }

    public void setMax_alt(int max_alt) {
        this.max_alt = max_alt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "NavAid{" +
                "name='" + name + '\'' +
                ", max_range=" + max_range +
                ", max_alt=" + max_alt +
                ", position=" + position +
                '}';
    }


    /*
     * Convert the different variation of limitations from VFG Denmark to
     * an integer array with the FL and the NM as first and second element.
     *
     * Accepts Strings like "FL 500/60 NM", "20NM", "FL 500/200NM"
     */
    private static int[] parseLimitations(String input) {
        int[] result = {0,0};
        String[] parts=input.trim().replace(" ","").split("/");
        for (String s : parts) {
            if (s.indexOf("NM")>=0) {
                result[1]= Integer.parseInt(s.replace("NM",""));
            } else if (s.indexOf("FL")>=0) {
                result[0]=Integer.parseInt(s.replace("FL",""));
            }
        }

        return result;
    }

    /*
     * Convert a Location String in the format from VFG Denmark and converts it to an
     * array with two strings - the first for lat, and the second for lon with N and E
     * stripped.
     *
     * Accepts formats like "57 05 03.80N 009 40 53.20E"
     */
    public static LatLng parseCoordinates(String line) {
        Pattern pattern = Pattern.compile("(.*?)N (.*)E");
        Matcher matcher = pattern.matcher(line);

        if (matcher.find()) {
            String lat = matcher.group(1);
            String lon = matcher.group(2);
            return new LatLng(parseComponent(lat), parseComponent(lon));
        } else {
            return null;
        }
    }

    private static double parseComponent(String component) {
        String[] parts = component.split(" ");
        int dd = Integer.parseInt(parts[0]);
        int mm = Integer.parseInt(parts[1]);
        Double ss = Double.parseDouble(parts[2]);

        return  dd + mm / 60. + ss / 3600.;
    }
}



