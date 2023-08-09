package com.example.malanglinejavamvvm.utilities;

import android.content.res.Resources;

import com.example.malanglinejavamvvm.model.PointTransport;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class Helper {

    public static double calculateDistance(PointTransport a, PointTransport b) {
        return Math.sqrt(Math.pow((a.lat() - b.lat()), 2) + Math.pow((a.lng() - b.lng()), 2));
    }

    public static double calculateDistance(PointTransport a, Double lat, Double lng) {
        return Math.sqrt(Math.pow((a.lat() - lat), 2) + Math.pow((a.lng() - lng), 2));
    }

    public static double calculateDistance(LatLng source, LatLng destination) {
        return Math.sqrt(Math.pow((source.latitude - destination.latitude), 2)
                + Math.pow((source.longitude - destination.longitude), 2));
    }

    public static String humanReadableDistane(double distance) {
        int unit = 1000;
        if (distance < unit) return String.format(Locale.getDefault(), "%.1f", distance) + " m";
        int exp = (int) (Math.log(distance) / Math.log(unit));
        String pre = "kMGTPE".charAt(exp - 1) + "";
        return String.format(Locale.getDefault(), "%.1f %sm", distance / Math.pow(unit, exp), pre);
    }

    public static int toDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int toPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


}
