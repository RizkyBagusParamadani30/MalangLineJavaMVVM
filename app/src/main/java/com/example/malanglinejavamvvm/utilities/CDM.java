package com.example.malanglinejavamvvm.utilities;

import android.content.Context;
import android.preference.PreferenceManager;

public class CDM {

    public static double cost = 4000D;



    public static Double getStandardCost() { return CDM.cost; }

    public static Double oneMeterInDegree() { return 0.00000898448D; }

    public static int getStandardDistance() {
        return 400;
    }

    public static int getDistance(Context context) {
        String cost = PreferenceManager.getDefaultSharedPreferences(context).getString("pref_walkingDistance",
                String.valueOf(CDM.getStandardDistance()));
        return Integer.valueOf(cost);
    }
}
