package com.example.tbrams.markerdemo.dbModel;


import java.util.ArrayList;
import java.util.List;

public class SampleDataProvider {
    public static List<String>    sTrips;
    private static List<WpItem>    sWpListForTrip;
    public static List<List<WpItem>> sWpListsForTrips;



    static {
        sTrips = new ArrayList<>();
        sWpListsForTrips = new ArrayList<>();

        sTrips.add("Experiment 1 / EKRK - EKOD");
        sWpListForTrip = new ArrayList<>();
        sWpListForTrip.add(new WpItem(null, "EKRK", 55.59,	12.13, null, 1200, null, 1));
        sWpListForTrip.add(new WpItem(null, "Solrød", 55.53, 12.18,3.8,1200, null, 2));
        sWpListForTrip.add(new WpItem(null, "Hvidovre", 55.63, 12.47,11.5,1200, null, 3));
        sWpListForTrip.add(new WpItem(null, "Farum", 55.82, 12.37,11.8,1200, null, 4));
        sWpListForTrip.add(new WpItem(null, "Ølsted", 55.92, 12.06,12.2,1200, null, 5));
        sWpListForTrip.add(new WpItem(null, "Holbæk", 55.70, 11.74, 17.3,1200, null, 6));
        sWpListForTrip.add(new WpItem(null, "Eskildsoe", 55.73, 12.08, 11.5,1200, null, 7));
        sWpListForTrip.add(new WpItem(null, "Gundsoelille", 55.72, 12.13, 2.1,1200, null, 8));
        sWpListForTrip.add(new WpItem(null, "EKRK", 55.58, 12.15, 8.4,1200, null, 9));
        sWpListForTrip.add(new WpItem(null, "Store Merløse", 55.54, 11.71,15.1,1200, null, 10));
        sWpListForTrip.add(new WpItem(null, "Halskov", 55.35, 11.13, 23.0,1200, null, 11));
        sWpListForTrip.add(new WpItem(null, "Sprogø", 55.34, 10.98, 5.4,1200, null, 12));
        sWpListForTrip.add(new WpItem(null, "Knudshoved", 55.30, 10.83, 5.6,1200, null, 13));
        sWpListForTrip.add(new WpItem(null, "Rolfsted'", 55.32, 10.57, 8.9,1200, null, 14));
        sWpListForTrip.add(new WpItem(null, "Munkebo", 55.46, 10.51, 8.8,1200, null, 15));
        sWpListForTrip.add(new WpItem(null, "Klintebjerg", 55.48, 10.45, 2.5,1200, null, 16));
        sWpListForTrip.add(new WpItem(null, "Otterup", 55.50, 10.39, 2.2,1200, null, 17));
        sWpListForTrip.add(new WpItem(null, "Beldringe", 55.47, 10.33, 2.9,1200, null, 18));
        sWpListsForTrips.add(sWpListForTrip);


        sTrips.add("Testing / Sengeløse-Roskilde");
        sWpListForTrip = new ArrayList<>();
        sWpListForTrip.add(new WpItem(null, "Sengeløse", 55.68, 12.23, null,1200, null, 1));
        sWpListForTrip.add(new WpItem(null, "Roskilde", 55.64, 12.08, 5.6,1200, null, 2));
        sWpListsForTrips.add(sWpListForTrip);

        sTrips.add("Back home to Tølløse");
        sWpListForTrip = new ArrayList<>();
        sWpListForTrip.add(new WpItem(null, "WP0", 55.64, 12.09, null,1200, null, 1));
        sWpListForTrip.add(new WpItem(null, "WP1", 55.71, 11.90, 7.6,1200, null, 2));
        sWpListForTrip.add(new WpItem(null, "WP2", 55.61, 11.77, 7.2,1200, null, 3));
        sWpListsForTrips.add(sWpListForTrip);

        sTrips.add("Rundt på Sjælland");
        sWpListForTrip = new ArrayList<>();
        sWpListForTrip.add(new WpItem(null, "Lejre", 55.60, 11.97, null,1200, null, 1));
        sWpListForTrip.add(new WpItem(null, "Kr. Såby", 55.65, 11.88, 4.0,1200, null, 2));
        sWpListForTrip.add(new WpItem(null, "Kr. Hyllinge", 55.72, 11.92, 4.9,1200, null, 3));
        sWpListForTrip.add(new WpItem(null, "Orø", 55.78, 11.81, 4.8,1200, null, 4));
        sWpListForTrip.add(new WpItem(null, "Hagested", 55.75,	11.60, 7.4,1200, null, 5));
        sWpListForTrip.add(new WpItem(null, "Stigs Bjergby", 55.67, 11.46, 6.5,1200, null, 6));
        sWpListForTrip.add(new WpItem(null, "Verup", 55.56, 11.51, 6.9,1200, null, 7));
        sWpListForTrip.add(new WpItem(null, "Bromme", 55.48, 11.53, 5.0,1200, null, 8));
        sWpListForTrip.add(new WpItem(null, "Vrangstrup", 55.40, 11.70, 7.3,1200, null, 8));
        sWpListForTrip.add(new WpItem(null, "Sneslev", 55.39, 11.84, 4.7,1200, null, 10));
        sWpListForTrip.add(new WpItem(null, "Gørslev", 55.44, 11.99, 6.1,1200, null, 11));
        sWpListForTrip.add(new WpItem(null, "Køge",55.48, 12.13, 5.3,1200, null, 12));
        sWpListForTrip.add(new WpItem(null, "EKRK", 55.59, 12.13, 6.4,1200, null, 13));
        sWpListsForTrips.add(sWpListForTrip);

        sTrips.add("Navigation #23");
        sWpListForTrip = new ArrayList<>();
        sWpListForTrip.add(new WpItem(null, "EKRK", 55.59, 12.13, null,1200, null, 1));
        sWpListForTrip.add(new WpItem(null, "Store Valby", 55.69, 12.13, 6.5,1200, null, 2));
        sWpListForTrip.add(new WpItem(null, "Kalred", 55.70, 11.26, 29.4,1200, null, 3));
        sWpListForTrip.add(new WpItem(null, "TNO VOR", 55.77, 11.44, 7.4,1200, null, 4));
        sWpListForTrip.add(new WpItem(null, "Tølløse", 55.61, 11.76, 14.6,1200, null, 5));
        sWpListForTrip.add(new WpItem(null, "Bjæverskov", 55.46, 12.03, 13.,1200, null, 6));
        sWpListForTrip.add(new WpItem(null, "Køge", 55.48, 12.14, 4.0, 1200, null, 7));
        sWpListForTrip.add(new WpItem(null, "EKRK",  55.58, 12.13, 8.2,1200, null, 8));
        sWpListsForTrips.add(sWpListForTrip);

        sTrips.add("Eksperiment");
        sWpListForTrip = new ArrayList<>();
        sWpListForTrip.add(new WpItem(null, "EKRK", 55.59, 12.13, null,1200, null, 1));
        sWpListForTrip.add(new WpItem(null, "St Valby", 55.69, 12.14, 6.6,1200, null, 2));
        sWpListForTrip.add(new WpItem(null, "WP3", 55.79, 11.48, 23.0,1200, null, 3));
        sWpListForTrip.add(new WpItem(null, "Kalred", 55.70, 11.27, 8.9,1200, null, 4));
        sWpListForTrip.add(new WpItem(null, "Tølløse", 55.61, 11.76, 17.5,1200, null, 5));
        sWpListForTrip.add(new WpItem(null, "Bjæverskov", 55.46, 12.03, 13.2,1200, null, 6));
        sWpListForTrip.add(new WpItem(null, "Køge", 55.48, 12.14, 3.8,1200, null, 7));
        sWpListForTrip.add(new WpItem(null, "EKRK", 55.58, 12.13, 6.0,1200, null, 8));
        sWpListsForTrips.add(sWpListForTrip);


        sTrips.add("Just test data");
        sWpListForTrip = new ArrayList<>();
        sWpListForTrip.add(new WpItem(null, "EKRK_1", 55.59,	12.13, null, 2000, null, 1));
        sWpListForTrip.add(new WpItem(null, "EKRK_2", 56.59,	13.13, 11.5, 2000, null, 2));
        sWpListForTrip.add(new WpItem(null, "EKRK_3", 57.59,	14.13, 22.6, 2000, null, 3));
        sWpListsForTrips.add(sWpListForTrip);
    }

}
