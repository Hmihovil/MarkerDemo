package com.example.tbrams.markerdemo.data;


import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class ExtraMarkers {
    private static ExtraMarkers sExtraMarkers;

    private static List<NavAid> sNavAidList;
    private static List<Aerodrome> sAerodromeList;


    public ExtraMarkers(Context context) {
        sAerodromeList = new ArrayList<>();
        sNavAidList = new ArrayList<>();
    }

    public static ExtraMarkers get(Context context){
        if (sExtraMarkers==null) {
            sExtraMarkers = new ExtraMarkers(context);
        }
        return sExtraMarkers;
    }


    public List<Aerodrome> getAerodromeList() { return sAerodromeList;}


    public void setAerodromeList(List<Aerodrome> adList) {
        sAerodromeList = adList;
    }

    public List<NavAid> getNavAidList() { return sNavAidList; }


    public void setNavAidList(List<NavAid> navList) {
        sNavAidList = navList;
    }


    public List<Aerodrome> getSampleAerodromeList() {

        List<Aerodrome> sampleAdList = new ArrayList<>();

        sampleAdList.add(new Aerodrome("EKYT", "Aalborg", "57 05 34.04N 009 50 56.99E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKAH", "Aarhus", "56 18 00.06N 010 37 08.43E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKAE", "Ærø", "54 51 09.35N 010 27 23.05E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKAT", "Anholt", "56 41 56.00N 011 33 21.00E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKBI", "Billund", "55 44 25.16N 009 09 06.40E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKBR", "Bornholm Rønne ", "55 03 47.76N 014 45 34.41E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKEB", "Esbjerg", "55 31 33.39N 008 33 12.25E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKHG", "Herning", "56 11 05.09N 009 02 40.02E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKHS", "Hadsund", "56 45 21.28N 010 13 43.39E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKKL", "Kalundborg", "55 42 00.12N 011 15 00.22E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKKA", "Karup", "56 17 50.85N 009 07 28.66E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKCH", "København Kastrup", "55 37 04.50N 012 39 21.50E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKRK", "København Roskilde", "55 35 08.04N 012 07 53.14E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKVD", "Kolding Vamdrup", "55 26 10.62N 009 19 51.33E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKPB", "Kruså Padborg ", "54 52 13.10N 009 16 44.45E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKLS", "Læsø", "57 16 38.02N 011 00 00.30E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKLV", "Lemvig", "56 30 10.97N 008 18 41.59E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKMB", "Lolland Falster Maribo", "54 41 57.64N 011 26 24.42E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKNM", "Morsø", "56 49 28.05N 008 47 10.92E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKOD", "Odense", "55 28 35.99N 010 19 51.36E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKRD", "Randers", "56 30 24.00N 010 02 11.00E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKRS", "Ringsted", "55 25 33.07N 011 48 24.56E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKSS", "Samsø", "55 53 23.51N 010 36 49.51E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKSN", "Sindal", "57 30 12.69N 010 13 45.74E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKSV", "Skive", "56 33 00.75N 009 10 22.74E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKSB", "Sønderborg", "54 57 51.72N 009 47 30.23E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKVJ", "Stauning", "55 59 24.44N 008 21 14.06E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKST", "Sydfyn Tåsinge", "55 00 59.40N 010 33 47.02E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKTS", "Thisted", "57 04 07.68N 008 42 18.81E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKTD", "Tønder", "54 55 46.65N 008 50 25.14E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKVH", "Vesthimmerland", "56 50 49.28N 009 27 30.74E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKVB", "Viborg", "56 24 35.82N 009 24 33.82E", Aerodrome.PUBLIC, null, null, false, null));
        sampleAdList.add(new Aerodrome("EKSP", "Vojens Skydstrup", "55 13 31.99N 009 15 50.15E", Aerodrome.PUBLIC, null, null, false, null));

        return sampleAdList;
    }



    public List<NavAid> getSampleNavAidList() {
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
        sampleNavAidList.add(new NavAid("AALBORG", "GL", NavAid.LOCATOR, "57 05 03.80N 009 40 53.20E", "398.000", "20NM", 56.80));
        sampleNavAidList.add(new NavAid("AARHUS","TL",   NavAid.LOCATOR, "56 18 01.46N 010 37 07.22E", "384.000", "20NM", null));
        sampleNavAidList.add(new NavAid("BILLUND","GE", NavAid.LOCATOR, "55 44 10.21N 009 01 06.90E", "395.000", "15 NM", null));
        sampleNavAidList.add(new NavAid("BORNHOLM/RØNNE","FAU", NavAid.LOCATOR, "55 01 41.49N 014 54 01.79E", "78.600", "20NM", 78.60));
        sampleNavAidList.add(new NavAid("DONNA","DN", NavAid.LOCATOR, "55 28 08.54N 005 07 59.03E", "355.000", "25NM", null));
        sampleNavAidList.add(new NavAid("ESBJERG","HP", NavAid.LOCATOR, "55 30 41.17N 008 24 45.79E", "376.000", "30NM", null));
        sampleNavAidList.add(new NavAid("ESBJERG","EJ", NavAid.LOCATOR, "55 32 28.51N 008 41 59.11E", "400.500", "20NM", null));
        sampleNavAidList.add(new NavAid("KARUP","KD", NavAid.LOCATOR, "56 17 47.51N 008 58 06.53E", "357.000", "25NM", 172.80));
        sampleNavAidList.add(new NavAid("KARUP","KA", NavAid.LOCATOR, "56 17 54.42N 009 14 13.05E", "369.000", "20NM", 172.80));
        sampleNavAidList.add(new NavAid("KOLDING/VAMDRUP","KD", NavAid.LOCATOR, "55 26 35.87N 009 20 05.42E", "357.000", "15NM", 174.50));
        sampleNavAidList.add(new NavAid("KOBENHAVN/ROSKILDE","RK", NavAid.LOCATOR, "55 37 23.27N 011 59 49.81E", "368.000", "30NM", null));
        sampleNavAidList.add(new NavAid("ODENSE","FE", NavAid.LOCATOR, "55 31 12.45N 010 27 45.21E", "423.000", "20NM", null));
        sampleNavAidList.add(new NavAid("SINDAL","SD", NavAid.LOCATOR, "57 30 02.77N 010 09 02.53E", "339.000", "15NM", null));
        sampleNavAidList.add(new NavAid("STAUNING","AU", NavAid.LOCATOR, "55 59 27.58N 008 19 06.09E", "346.000", "15NM", null));
        sampleNavAidList.add(new NavAid("STAUNING","VJ", NavAid.LOCATOR, "55 59 19.13N 008 25 27.97E", "328.000", "15NM", null));
        sampleNavAidList.add(new NavAid("SØNDERBORG","IN", NavAid.LOCATOR, "55 01 13.86N 009 42 23.16E", "316.000", "15NM", null));
        sampleNavAidList.add(new NavAid("SØNDERBORG","SB", NavAid.LOCATOR, "54 56 16.21N 009 49 47.08E", "330.000", "15NM", null));
        sampleNavAidList.add(new NavAid("VOJENS/SKRYDSTRUP","VO", NavAid.LOCATOR, "55 13 28.74N 009 16 25.36E", "321.000", "25NM", null));


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
