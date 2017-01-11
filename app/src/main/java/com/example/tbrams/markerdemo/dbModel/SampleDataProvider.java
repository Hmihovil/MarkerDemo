package com.example.tbrams.markerdemo.dbModel;


import com.example.tbrams.markerdemo.data.NavAid;

import java.util.ArrayList;
import java.util.List;

public class SampleDataProvider {
    public static List<String>    sTrips;
    private static List<WpItem>    sWpListForTrip;
    public static List<List<WpItem>> sWpListsForTrips;


    public static List<NavAid>       sNavAidList;



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


        sNavAidList = new ArrayList<>();
        sNavAidList.add(new NavAid());



        // NavAids
        sNavAidList = new ArrayList<>();

        // VOR/DME & VOR
        sNavAidList.add(new NavAid("KORSA", "KOR", NavAid.VORDME, "55 26 21.71N 011 37 53.51E", "112.800", "FL 500/80NM", 136.20));
        sNavAidList.add(new NavAid("TRANO", "TNO", NavAid.VORDME, "55 46 26.74N 011 26 21.08E", "117.400", "FL 500/60 NM", -11.90));
        sNavAidList.add(new NavAid("ODIN",  "ODN",    NavAid.VORDME, "55 34 51.64N 010 39 10.76E", "115.500", "FL 500/60NM", 24.00));
        sNavAidList.add(new NavAid("KASTRUP",   "KAS",    NavAid.VORDME, "55 35 25.87N 012 36 48.97E", "112.500", "FL 500/60NM", 28.90));
        sNavAidList.add(new NavAid("AALBORG", "AAL",  NavAid.VOR, "57 06 13.39N 009 59 44.08E", "116.700", "FL 500/100NM", 56.80));
        sNavAidList.add(new NavAid("ALSIE", "ALS",    NavAid.VOR, "54 54 19.49N 009 59 36.16E", "114.700", "FL 500/60 NM", null));
        sNavAidList.add(new NavAid("CODAN", "CDA",    NavAid.VORDME, "55 00 05.40N 012 22 45.16E", "114.900", "FL 500/60 NM", 90.20));
        sNavAidList.add(new NavAid("RAMME", "RAM",    NavAid.VORDME, "56 28 42.14N 008 11 14.51E", "111.850", "FL 500/60NM ", 60.40));
        sNavAidList.add(new NavAid("RØNNE","ROE",    NavAid.VORDME, "55 03 56.08N 014 45 31.29E", "112.000", "FL 500/80 NM", null));


        // NDB
        sNavAidList.add(new NavAid("BILLUND","LO",    NavAid.NDB, "55 44 40.13N 009 16 46.81E", "341", "40 NM", null));

        // Localizer
        sNavAidList.add(new NavAid("AALBORG", "GL", NavAid.LOCALIZER, "57 05 03.80N 009 40 53.20E", "398.000", "20NM", 56.80));
        sNavAidList.add(new NavAid("AARHUS","TL",   NavAid.LOCALIZER, "56 18 01.46N 010 37 07.22E", "384.000", "20NM", null));
        sNavAidList.add(new NavAid("BILLUND","GE", NavAid.LOCALIZER, "55 44 10.21N 009 01 06.90E", "395.000", "15 NM", null));
        sNavAidList.add(new NavAid("BORNHOLM/RØNNE","FAU", NavAid.LOCALIZER, "55 01 41.49N 014 54 01.79E", "78.600", "20NM", 78.60));
        sNavAidList.add(new NavAid("DONNA","DN", NavAid.LOCALIZER, "55 28 08.54N 005 07 59.03E", "355.000", "25NM", null));
        sNavAidList.add(new NavAid("ESBJERG","HP", NavAid.LOCALIZER, "55 30 41.17N 008 24 45.79E", "376.000", "30NM", null));
        sNavAidList.add(new NavAid("ESBJERG","EJ", NavAid.LOCALIZER, "55 32 28.51N 008 41 59.11E", "400.500", "20NM", null));
        sNavAidList.add(new NavAid("KARUP","KD", NavAid.LOCALIZER, "56 17 47.51N 008 58 06.53E", "357.000", "25NM", 172.80));
        sNavAidList.add(new NavAid("KARUP","KA", NavAid.LOCALIZER, "56 17 54.42N 009 14 13.05E", "369.000", "20NM", 172.80));
        sNavAidList.add(new NavAid("KOLDING/VAMDRUP","KD", NavAid.LOCALIZER, "55 26 35.87N 009 20 05.42E", "357.000", "15NM", 174.50));
        sNavAidList.add(new NavAid("KOBENHAVN/ROSKILDE","RK", NavAid.LOCALIZER, "55 37 23.27N 011 59 49.81E", "368.000", "30NM", null));
        sNavAidList.add(new NavAid("ODENSE","FE", NavAid.LOCALIZER, "55 31 12.45N 010 27 45.21E", "423.000", "20NM", null));
        sNavAidList.add(new NavAid("SINDAL","SD", NavAid.LOCALIZER, "57 30 02.77N 010 09 02.53E", "339.000", "15NM", null));
        sNavAidList.add(new NavAid("STAUNING","AU", NavAid.LOCALIZER, "55 59 27.58N 008 19 06.09E", "346.000", "15NM", null));
        sNavAidList.add(new NavAid("STAUNING","VJ", NavAid.LOCALIZER, "55 59 19.13N 008 25 27.97E", "328.000", "15NM", null));
        sNavAidList.add(new NavAid("SØNDERBORG","IN", NavAid.LOCALIZER, "55 01 13.86N 009 42 23.16E", "316.000", "15NM", null));
        sNavAidList.add(new NavAid("SØNDERBORG","SB", NavAid.LOCALIZER, "54 56 16.21N 009 49 47.08E", "330.000", "15NM", null));
        sNavAidList.add(new NavAid("VOJENS/SKRYDSTRUP","VO", NavAid.LOCALIZER, "55 13 28.74N 009 16 25.36E", "321.000", "25NM", null));


        // TACAN
        sNavAidList.add(new NavAid("AALBORG", "AAL", NavAid.TACAN, "57 06 14.16N 009 59 34.11E", "CH 114X", "FL500/200NM", 56.80));
        sNavAidList.add(new NavAid("BORNHOLM/RØNNE", "ROE", NavAid.TACAN, "55 03 42.73N 014 45 21.07E", "CH 57X", "FL 500/80 NM", null));
        sNavAidList.add(new NavAid("KARUP", "KAR", NavAid.TACAN, "56 17 48.03N 009 00 30.95E", "CH37X", "FL 500/200NM", 172.80));

        // VORTAC
        sNavAidList.add(new NavAid("VOJENS/SKRYDSTRUP","SKR", NavAid.VORTAC, "55 13 44.18N 009 12 50.61E", "110.400", "FL 500/80 NM", 138.40));


        // DME
        sNavAidList.add(new NavAid("BELLA", "BEL", NavAid.DME, "55 47 28.45N 012 05 44.74E", "114.650", "FL 195/60 NM", 135.00));
        sNavAidList.add(new NavAid("KOLDING/VAMDRUP", "VAM", NavAid.DME, "55 26 16.58N 009 20 06.05E", "110.050", "", 174.50));
        sNavAidList.add(new NavAid("STAUNING","LME", NavAid.DME, "55 59 33.50N 008 21 15.75E", "115.350", "", 76.10));
        sNavAidList.add(new NavAid("VOJENS/SKRYDSTRUP", "ISPA /SRY", NavAid.DME, "55 13 09.34N 009 17 11.49E", "", "CH30Y", null));


    }

}
