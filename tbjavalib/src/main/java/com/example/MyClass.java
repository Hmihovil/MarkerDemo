package com.example;

import java.util.Locale;

public class MyClass {

    public static void main(String[] args) throws InterruptedException {

        // IFR Points
       convertVFG("555806N 0095940E");

        // Obstacle formats
       convertVFG("55 58 06N 009 59 40E");
        convertVFG("55 58 06.00N 009 59 40.00E");

        convertVFG("55 58 06 00N 009 59 40 00E");
        convertVFG("55:58:06 N 009:59:40 E");
    }


    /** Convert location format used in VFG Denmark to a Latlon object.
     *  Support formats like these:
     *  "570613N 0095944E",
     *  "55 01 59N 009 14 55E",
     *  "55 45 22.55N 009 15 22.64E" and
     *  "57 05 34 04N 009 50 56 99E"
     *
     *  Because the site soaringweb.org is using a different format yet again, I have included
     *  the following format in the list
     *
     *  "57:05:34 N 009:50:56 E"
     *
     * @params String in a format from the list above
     * @return a LatLng object
     *
     */
    public static void convertVFG(String input) {
        System.out.println(String.format(Locale.ENGLISH, "Received: %s", input));

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

        System.out.println(String.format(Locale.ENGLISH, "After parsing it is: %f, %f", lat, lon));
    }



    public static String removeLastChar(String str) {
        return str.substring(0,str.length()-1);
    }



}


