package com.example.tbrams.markerdemo.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class NavAids {
    private static List<NavAid> sNavAidList;
    private static NavAids sNavAids;

    private NavAids(Context context) {
        sNavAidList = getSampleList();
    }


    public void setNavList(List<NavAid> navList) {
        sNavAidList = navList;
    }


    public List<NavAid> getList() {
        return sNavAidList;
    }



    public static NavAids get(Context context){
        if (sNavAids==null) {
            sNavAids = new NavAids(context);
        }
        return sNavAids;
    }


    /*
     * Use to provide sample data when resetting the database
     *
     */
    public List<NavAid> getSampleList() {
        List<NavAid> sampleNavAidList = new ArrayList<>();

        // VOR/DME & VOR
        sampleNavAidList.add(new NavAid("KØRSA", "KØR", NavAid.VORDME, "55 26 21.71N 011 37 53.51E", "112.800", "FL 500/80NM", 136.20));
        sampleNavAidList.add(new NavAid("TRANO", "TNO", NavAid.VORDME, "55 46 26.74N 011 26 21.08E", "117.400", "FL 500/60 NM", -11.90));
        sampleNavAidList.add(new NavAid("ODIN",  "ODN",    NavAid.VORDME, "55 34 51.64N 010 39 10.76E", "115.500", "FL 500/60NM", 24.00));
        sampleNavAidList.add(new NavAid("KASTRUP",   "KAS",    NavAid.VORDME, "55 35 25.87N 012 36 48.97E", "112.500", "FL 500/60NM", 28.90));
        sampleNavAidList.add(new NavAid("AALBORG", "AAL",  NavAid.VOR, "57 06 13.39N 009 59 44.08E", "116.700", "FL 500/100NM", 56.80));
        sampleNavAidList.add(new NavAid("ALSIE", "ALS",    NavAid.VOR, "54 54 19.49N 009 59 36.16E", "114.700", "FL 500/60 NM", null));
        sampleNavAidList.add(new NavAid("CODAN", "CDA",    NavAid.VORDME, "55 00 05.40N 012 22 45.16E", "114.900", "FL 500/60 NM", 90.20));
        sampleNavAidList.add(new NavAid("RAMME", "RAM",    NavAid.VORDME, "56 28 42.14N 008 11 14.51E", "111.850", "FL 500/60NM ", 60.40));
        sampleNavAidList.add(new NavAid("RØNNE","ROE",    NavAid.VORDME, "55 03 56.08N 014 45 31.29E", "112.000", "FL 500/80 NM", null));


        // NDB
        sampleNavAidList.add(new NavAid("BILLUND","LO",    NavAid.NDB, "55 44 40.13N 009 16 46.81E", "341", "40 NM", null));

        // Localizer
        sampleNavAidList.add(new NavAid("AALBORG", "GL", NavAid.LOCALIZER, "57 05 03.80N 009 40 53.20E", "398.000", "20NM", 56.80));
        sampleNavAidList.add(new NavAid("AARHUS","TL",   NavAid.LOCALIZER, "56 18 01.46N 010 37 07.22E", "384.000", "20NM", null));
        sampleNavAidList.add(new NavAid("BILLUND","GE", NavAid.LOCALIZER, "55 44 10.21N 009 01 06.90E", "395.000", "15 NM", null));
        sampleNavAidList.add(new NavAid("BORNHOLM/RØNNE","FAU", NavAid.LOCALIZER, "55 01 41.49N 014 54 01.79E", "78.600", "20NM", 78.60));
        sampleNavAidList.add(new NavAid("DONNA","DN", NavAid.LOCALIZER, "55 28 08.54N 005 07 59.03E", "355.000", "25NM", null));
        sampleNavAidList.add(new NavAid("ESBJERG","HP", NavAid.LOCALIZER, "55 30 41.17N 008 24 45.79E", "376.000", "30NM", null));
        sampleNavAidList.add(new NavAid("ESBJERG","EJ", NavAid.LOCALIZER, "55 32 28.51N 008 41 59.11E", "400.500", "20NM", null));
        sampleNavAidList.add(new NavAid("KARUP","KD", NavAid.LOCALIZER, "56 17 47.51N 008 58 06.53E", "357.000", "25NM", 172.80));
        sampleNavAidList.add(new NavAid("KARUP","KA", NavAid.LOCALIZER, "56 17 54.42N 009 14 13.05E", "369.000", "20NM", 172.80));
        sampleNavAidList.add(new NavAid("KOLDING/VAMDRUP","KD", NavAid.LOCALIZER, "55 26 35.87N 009 20 05.42E", "357.000", "15NM", 174.50));
        sampleNavAidList.add(new NavAid("KOBENHAVN/ROSKILDE","RK", NavAid.LOCALIZER, "55 37 23.27N 011 59 49.81E", "368.000", "30NM", null));
        sampleNavAidList.add(new NavAid("ODENSE","FE", NavAid.LOCALIZER, "55 31 12.45N 010 27 45.21E", "423.000", "20NM", null));
        sampleNavAidList.add(new NavAid("SINDAL","SD", NavAid.LOCALIZER, "57 30 02.77N 010 09 02.53E", "339.000", "15NM", null));
        sampleNavAidList.add(new NavAid("STAUNING","AU", NavAid.LOCALIZER, "55 59 27.58N 008 19 06.09E", "346.000", "15NM", null));
        sampleNavAidList.add(new NavAid("STAUNING","VJ", NavAid.LOCALIZER, "55 59 19.13N 008 25 27.97E", "328.000", "15NM", null));
        sampleNavAidList.add(new NavAid("SØNDERBORG","IN", NavAid.LOCALIZER, "55 01 13.86N 009 42 23.16E", "316.000", "15NM", null));
        sampleNavAidList.add(new NavAid("SØNDERBORG","SB", NavAid.LOCALIZER, "54 56 16.21N 009 49 47.08E", "330.000", "15NM", null));
        sampleNavAidList.add(new NavAid("VOJENS/SKRYDSTRUP","VO", NavAid.LOCALIZER, "55 13 28.74N 009 16 25.36E", "321.000", "25NM", null));


        // TACAN
        sampleNavAidList.add(new NavAid("AALBORG", "AAL", NavAid.TACAN, "57 06 14.16N 009 59 34.11E", "CH 114X", "FL500/200NM", 56.80));
        sampleNavAidList.add(new NavAid("BORNHOLM/RØNNE", "ROE", NavAid.TACAN, "55 03 42.73N 014 45 21.07E", "CH 57X", "FL 500/80 NM", null));
        sampleNavAidList.add(new NavAid("KARUP", "KAR", NavAid.TACAN, "56 17 48.03N 009 00 30.95E", "CH37X", "FL 500/200NM", 172.80));

        // VORTAC
        sampleNavAidList.add(new NavAid("VOJENS/SKRYDSTRUP","SKR", NavAid.VORTAC, "55 13 44.18N 009 12 50.61E", "110.400", "FL 500/80 NM", 138.40));


        // DME
        sampleNavAidList.add(new NavAid("BELLA", "BEL", NavAid.DME, "55 47 28.45N 012 05 44.74E", "114.650", "FL 195/60 NM", 135.00));
        sampleNavAidList.add(new NavAid("KOLDING/VAMDRUP", "VAM", NavAid.DME, "55 26 16.58N 009 20 06.05E", "110.050", "", 174.50));
        sampleNavAidList.add(new NavAid("STAUNING","LME", NavAid.DME, "55 59 33.50N 008 21 15.75E", "115.350", "", 76.10));
        sampleNavAidList.add(new NavAid("VOJENS/SKRYDSTRUP", "ISPA /SRY", NavAid.DME, "55 13 09.34N 009 17 11.49E", "", "CH30Y", null));

        return sampleNavAidList;
    }




}
