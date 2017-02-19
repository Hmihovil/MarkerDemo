package com.example.tbrams.markerdemo.components;


import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {

    /** Convert location format used in VFG Denmark to a Latlon object.
     *  Support formats like these:
     *  "570613N 0095944E",
     *  "55 01 59N 009 14 55E",
     *  "55 45 22.55N 009 15 22.64E" and
     *  "57 05 34 04N 009 50 56 99E"
     *
     *  AND Because the site soaringweb.org is using a different format yet again, I have added
     *  the following format in the list of supported formats
     *
     *  "57:05:34 N 009:50:56 E"
     *
     * @params String in a format from the list above
     * @return a LatLng object
     *
     */
    public static LatLng convertVFG(String input) {
        double nHrs=0, eHrs=0;
        double nMin=0, eMin=0;
        double nSec=0, eSec=0;

        String[] parts = input.split(" ");

        if (parts.length==8) {
            // For example "57 05 34 04N 009 50 56 99E"
            nHrs=Double.parseDouble(parts[0]);
            nMin=Double.parseDouble(parts[1]);
            nSec=Double.parseDouble(parts[2]+"."+removeLastChar(parts[3]));
            eHrs=Double.parseDouble(parts[4]);
            eMin=Double.parseDouble(parts[5]);
            eSec=Double.parseDouble(parts[6]+"."+removeLastChar(parts[7]));
        }
        else if (parts.length==2) {
            // For example "570613N 0095944E"
            nHrs = Double.parseDouble(parts[0].substring(0,2));
            nMin = Double.parseDouble(parts[0].substring(2,4));
            nSec = Double.parseDouble(parts[0].substring(4,6));

            eHrs = Double.parseDouble(parts[1].substring(0,3));
            eMin = Double.parseDouble(parts[1].substring(3,5));
            eSec = Double.parseDouble(parts[1].substring(5,7));

        } else if (parts.length == 4) {
            // soaringweb.org format "57:05:34 N 009:50:56 E"

            String[] nParts = parts[0].split(":");
            nHrs = Double.parseDouble(nParts[0]);
            nMin = Double.parseDouble(nParts[1]);
            nSec = Double.parseDouble(nParts[2]);

            String[] eParts = parts[2].split(":");
            eHrs = Double.parseDouble(eParts[0]);
            eMin = Double.parseDouble(eParts[1]);
            eSec = Double.parseDouble(eParts[2]);

        }  else {
            // Length 6, for example "55 58 06.00N 009 59 40.00E" or "55 58 06N 009 59 40E"
            nHrs=Double.parseDouble(parts[0]);
            nMin=Double.parseDouble(parts[1]);
            nSec=Double.parseDouble(removeLastChar(parts[2]));

            eHrs=Double.parseDouble(parts[3]);
            eMin=Double.parseDouble(parts[4]);
            eSec=Double.parseDouble(removeLastChar(parts[5]));
        }

        Double lat = nHrs+nMin/60+nSec/3600;
        Double lon = eHrs+eMin/60+eSec/3600;

        return new LatLng(lat, lon);
    }


    /**
     * Utility to parse a coordinate string without letters but with spaces.
     * Example input is "011 42 68" or "55 23 11.22"
     *
     * @param component String in one of these formats.
     * @return decimal notation double
     */
    public static double parseComponent(String component) {
        String[] parts = component.split(" ");
        int dd = Integer.parseInt(parts[0]);
        int mm = Integer.parseInt(parts[1]);
        Double ss = Double.parseDouble(parts[2]);

        return dd + mm / 60. + ss / 3600.;
    }

    /**
     * A utility for chopping off the last character of a string
     *
     * @param str   Typically a location string like " "55 01 59N
     * @return      String without the last character.
     */
    public static String removeLastChar(String str) {
        return str.substring(0,str.length()-1);
    }



    /**
     * Parse the different variation of radio limitations in VFG Denmark.
     *
     * @param input String  For example "FL 500/60 NM", "20NM", "FL 500/200NM"
     * @return      int[]   Two components, [0] is the altitude, [1] is the distance
     *
     */
    public static int[] parseLimitations(String input) {
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



    /**
     * Parse the different variation of altitude notations. Will process all altitude strings
     * haveing the format like these:
     *
     * "FL 500", "GND", "UNL, "2300" or "2300 ft"
     *
     * Any other format will yield a negative altitude to indicate the error condition.
     *
     * @param input String
     * @return int     The altitude in feet
     */
    public static int parseAltitude(String input) {
        input = input.trim().toUpperCase();
        int result = -9999;
        boolean flightLevelUnits = false;

        if (input != null) {
            if (input.equals("GND") || input.equals("SFC")) {
                result = 0;
            } else if (input.equals("UNL")) {
                result = 19999;
            } else {
                // First get rid of Feet unit if there at all
                input=input.replace("FT", "").trim();
                if (input.indexOf("FL") >= 0) {
                    flightLevelUnits = true;
                    input = input.replace("FL", "");
                }

                try {
                    result = Integer.parseInt(input);
                    if (flightLevelUnits) result*=100;

                } catch (NumberFormatException e) {
                    result = 0;
                    e.printStackTrace();
                }
            }
        }

        return result;
    }



    /**
     * Get current date.
     *
     * @return String  Current Date formatted as "dd-MM-yyyy"
     *
     */
    public static String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
