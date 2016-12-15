package com.example.tbrams.markerdemo.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class NavAids {
    private static List<NavAid> mNavAids;
    private static NavAids sNavAids;

    private NavAids(Context context) {
        mNavAids = new ArrayList<>();
        mNavAids.add(new NavAid("KORSA", "552622N","0113754E"));
        mNavAids.add(new NavAid("TRANO", "554627N","0112621E"));
        mNavAids.add(new NavAid("ODIN", "553452N", "0103911E"));
        mNavAids.add(new NavAid("KAS", "553526N", "0123649E"));
        mNavAids.add(new NavAid("AALBORG","570613N", "0095944E"));
        mNavAids.add(new NavAid("ALSIE", "545419N", "0095936E"));
        mNavAids.add(new NavAid("CODAN","550005N","0122245E"));
        mNavAids.add(new NavAid("RAMME", "562842N", "0081115E"));
        mNavAids.add((new NavAid("RØNNE","550356N","0144531E")));
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
