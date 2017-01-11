package com.example.tbrams.markerdemo.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class NavAids {
    private static List<NavAid> mNavAids;
    private static NavAids sNavAids;

    private NavAids(Context context) {
        mNavAids = new ArrayList<>();
        mNavAids.add(new NavAid("KORSA", NavAid.VOR, "55 26 21.71N 011 37 53.51E"));
        mNavAids.add(new NavAid("TRANO", NavAid.VORDME, "55 46 26.74N 011 26 21.08E"));
        mNavAids.add(new NavAid("ODIN", NavAid.TACAN, "55 34 51.64N 010 39 10.76E"));
        mNavAids.add(new NavAid("KAS", NavAid.NDB, "55 35 25.87N 012 36 48.97E"));
        mNavAids.add(new NavAid("AALBORG", NavAid.DME, "57 06 13.39N 009 59 44.08E"));
        mNavAids.add(new NavAid("ALSIE", NavAid.VOR, "54 54 19.49N 009 59 36.16E"));
        mNavAids.add(new NavAid("CODAN", NavAid.VOR, "55 00 05.40N 012 22 45.16E"));
        mNavAids.add(new NavAid("RAMME", NavAid.VOR, "56 28 42.14N 008 11 14.51E"));
        mNavAids.add((new NavAid("RØNNE",NavAid.VOR, "55 03 56.08N 014 45 31.29E")));
    }

    public List<NavAid> getList() {
        return mNavAids;
    }

    public static NavAids get(Context context){
        if (sNavAids==null) {
            sNavAids = new NavAids(context);
        }
        return sNavAids;
    }
}
