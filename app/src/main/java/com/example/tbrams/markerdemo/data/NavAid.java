package com.example.tbrams.markerdemo.data;

import android.content.ContentValues;

import com.example.tbrams.markerdemo.db.NavAidTable;
import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavAid {

    public static final int VOR =1;
    public static final int NDB=2;
    public static final int TACAN=3;
    public static final int DME=4;
    public static final int VORDME=5;
    public static final int VORTAC=6;
    public static final int LOCALIZER=7;

    private String id;
    private int seq_id;
    private String name;
    private String ident;
    private String freq;
    private int naType;
    private int    max_range;
    private int    max_alt;
    private LatLng position;
    private Double elevation;

    public LatLng getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public NavAid() {
        this.id = UUID.randomUUID().toString();
    }

    public int getSeq_id() { return seq_id; }
    public void setSeq_id(int seq_id) { this.seq_id = seq_id; }

    public NavAid(String name, String id, int kind, String latlong, String freq, String limitString, Double elevation) {
        position = parseCoordinates(latlong);

        this.id = UUID.randomUUID().toString();

        this.name = name;
        this.ident=id;
        this.naType = kind;
        this.freq = freq;

        this.seq_id=-1;

        int[] limits = parseLimitations(limitString);
        max_alt = limits[0];
        max_range=limits[1];

        if (elevation!=null)
           this.elevation = elevation;
        else
            this.elevation=0.0;
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
        return naType;
    }

    public void setType(int type) {
        this.naType = type;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }

    public String getFreq() {
        return freq;
    }

    public void setFreq(String freq) {
        this.freq = freq;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    /*
         * Convert the different variation of limitations from VFG Denmark to
         * an integer array with the FL and the NM as first and second element.
         *
         * Accepts Strings like "FL 500/60 NM", "20NM", "FL 500/200NM"
         */
    private static int[] parseLimitations(String input) {
        int[] result = {0,0};
        if (input != null) {
             String[] parts=input.trim().replace(" ","").split("/");
            for (String s : parts) {
                if (s.indexOf("NM")>=0) {
                    result[1]= Integer.parseInt(s.replace("NM",""));
                } else if (s.indexOf("FL")>=0) {
                    result[0]=Integer.parseInt(s.replace("FL",""));
                }
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



    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(NavAidTable.COLUMN_ID,   id);
        values.put(NavAidTable.COLUMN_NAME, name);
        values.put(NavAidTable.COLUMN_IDENT, ident);
        values.put(NavAidTable.COLUMN_TYPE, naType);
        values.put(NavAidTable.COLUMN_LAT, position.latitude);
        values.put(NavAidTable.COLUMN_LON, position.longitude);
        values.put(NavAidTable.COLUMN_FREQ, freq);
        values.put(NavAidTable.COLUMN_MAX_ALT, max_alt);
        values.put(NavAidTable.COLUMN_MAX_DIST, max_range);
        values.put(NavAidTable.COLUMN_ELEV, elevation);

        return values;
    }
}



