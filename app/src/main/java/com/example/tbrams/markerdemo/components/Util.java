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
     * @params String in a format from the list above
     * @return a LatLng object
     *
     */
    public static LatLng convertVFG(String input) {
        String[] parts;
        parts = input.split(" ");

        if (parts.length==8) {
            // Reformat the parts by creating decimal endings here and reducing
            parts[2]=parts[2]+"."+parts[3];
            parts[3]=parts[4];
            parts[4]=parts[5];
            parts[5]=parts[6]+"."+parts[7];
        }
        else if (parts.length==2) {
            // In this case we need to re-format everything so we can do the calculation below
            String[] reformattedParts={"","","","","","","",""};
            reformattedParts[0] = parts[0].substring(0,2);
            reformattedParts[1] = parts[0].substring(2,4);
            reformattedParts[2] = parts[0].substring(4,7);

            reformattedParts[3] =parts[1].substring(0,3);
            reformattedParts[4] =parts[1].substring(3,5);
            reformattedParts[5] =parts[1].substring(5,8);

            parts=reformattedParts;
        }

        // Otherwise just chop off the letter parts here
        parts[2] = removeLastChar(parts[2]);
        parts[5] = removeLastChar(parts[5]);

        Double lat = Double.parseDouble(parts[0])+Double.parseDouble(parts[1])/60+Double.parseDouble(parts[2])/3600;
        Double lon = Double.parseDouble(parts[3])+Double.parseDouble(parts[4])/60+Double.parseDouble(parts[5])/3600;

        return new LatLng(lat, lon);
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
