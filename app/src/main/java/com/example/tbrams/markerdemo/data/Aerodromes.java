package com.example.tbrams.markerdemo.data;


import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class Aerodromes {
    private static List<Aerodrome> mAerodromes;
    private static Aerodromes      sAerodromes;

    public Aerodromes(Context context) {
        mAerodromes = new ArrayList<>();
        mAerodromes.add(new Aerodrome("EKYT", "57 05 34.04N 009 50 56.99E"));
        mAerodromes.add(new Aerodrome("EKAH", "56 18 00.06N 010 37 08.43E"));
        mAerodromes.add(new Aerodrome("EKAH", "56 18 00.06N 010 37 08.43E"));

    }

    public List<Aerodrome> getList() { return mAerodromes;}


    public static Aerodromes get(Context context){
        if (sAerodromes==null) {
            sAerodromes = new Aerodromes(context);
        }
        return sAerodromes;
    }
}
