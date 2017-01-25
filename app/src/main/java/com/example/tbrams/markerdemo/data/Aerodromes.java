package com.example.tbrams.markerdemo.data;


import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class Aerodromes {
    private static List<Aerodrome> sAerodromeList;
    private static Aerodromes      sAerodromes;

    public Aerodromes(Context context) {
        sAerodromeList = new ArrayList<>();
        //sAerodromeList = getSampleList();
    }

    public List<Aerodrome> getList() { return sAerodromeList;}



    public void setList(List<Aerodrome> adList) {sAerodromeList = adList;}




    public List<Aerodrome> getSampleList() {

        List<Aerodrome> sampleAdList = new ArrayList<>();

        sampleAdList.add(new Aerodrome("EKYT", "Aalborg", "57 05 34.04N 009 50 56.99E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKAH", "Aarhus", "56 18 00.06N 010 37 08.43E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKAE", "Ærø", "54 51 09.35N 010 27 23.05E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKAT", "Anholt", "56 41 56.00N 011 33 21.00E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKBI", "Billund", "55 44 25.16N 009 09 06.40E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKBR", "Bornholm Rønne ", "55 03 47.76N 014 45 34.41E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKEB", "Esbjerg", "55 31 33.39N 008 33 12.25E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKHG", "Herning", "56 11 05.09N 009 02 40.02E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKHS", "Hadsund", "56 45 21.28N 010 13 43.39E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKKL", "Kalundborg", "55 42 00.12N 011 15 00.22E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKKA", "Karup", "56 17 50.85N 009 07 28.66E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKCH", "København Kastrup", "55 37 04.50N 012 39 21.50E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKRK", "København Roskilde", "55 35 08.04N 012 07 53.14E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKVD", "Kolding Vamdrup", "55 26 10.62N 009 19 51.33E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKPB", "Kruså Padborg ", "54 52 13.10N 009 16 44.45E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKLS", "Læsø", "57 16 38.02N 011 00 00.30E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKLV", "Lemvig", "56 30 10.97N 008 18 41.59E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKMB", "Lolland Falster Maribo", "54 41 57.64N 011 26 24.42E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKNM", "Morsø", "56 49 28.05N 008 47 10.92E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKOD", "Odense", "55 28 35.99N 010 19 51.36E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKRD", "Randers", "56 30 24.00N 010 02 11.00E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKRS", "Ringsted", "55 25 33.07N 011 48 24.56E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKSS", "Samsø", "55 53 23.51N 010 36 49.51E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKSN", "Sindal", "57 30 12.69N 010 13 45.74E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKSV", "Skive", "56 33 00.75N 009 10 22.74E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKSB", "Sønderborg", "54 57 51.72N 009 47 30.23E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKVJ", "Stauning", "55 59 24.44N 008 21 14.06E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKST", "Sydfyn Tåsinge", "55 00 59.40N 010 33 47.02E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKTS", "Thisted", "57 04 07.68N 008 42 18.81EE", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKTD", "Tønder", "54 55 46.65N 008 50 25.14E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKVH", "Vesthimmerland", "56 50 49.28N 009 27 30.74E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKVB", "Viborg", "56 24 35.82N 009 24 33.82E", Aerodrome.PUBLIC));
        sampleAdList.add(new Aerodrome("EKSP", "Vojens Skydstrup", "55 13 31.99N 009 15 50.15E", Aerodrome.PUBLIC));

        return sampleAdList;
    }




    public static Aerodromes get(Context context){
        if (sAerodromes==null) {
            sAerodromes = new Aerodromes(context);
        }
        return sAerodromes;
    }
}
