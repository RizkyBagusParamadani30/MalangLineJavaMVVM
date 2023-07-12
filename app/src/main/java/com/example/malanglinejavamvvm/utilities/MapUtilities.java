package com.example.malanglinejavamvvm.utilities;

import android.graphics.Color;

import com.example.malanglinejavamvvm.R;
import com.example.malanglinejavamvvm.model.PointTransport;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.Arrays;
import java.util.List;

public class MapUtilities {

    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 10;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYGON_WALKING = Arrays.asList(DASH, GAP);
    private static final int colorWalking = Color.parseColor("#e53935");
    private static final int colorTransfer = Color.parseColor("#ff9800");

    public static PolylineOptions getWalkingPolylineOptions() {
        return new PolylineOptions()
                .color(colorWalking)
                .pattern(PATTERN_POLYGON_WALKING)
                .width(10f);
    }

    public static PolylineOptions getTransferPolylineOptions() {
        Cap roundCap = new RoundCap();
        return new PolylineOptions()
                .color(colorTransfer)
                .pattern(PATTERN_POLYGON_WALKING)
                .endCap(roundCap)
                .startCap(roundCap)
                .width(10f);
    }

    public static CircleOptions getInterchangeCircleOptions() {
        return new CircleOptions()
                .fillColor(Color.WHITE)
                .strokeColor(Color.parseColor("#777777"))
                .strokeWidth(7f)
                .radius(10d);
    }

    public static Polyline drawPolyline(GoogleMap map, List<PointTransport> line, int color) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .color(color)
                .width(7f);
        for(PointTransport point: line) {
            polylineOptions.add(point.getLatLng());
        }
        return map.addPolyline(polylineOptions);
    }

    public static Marker drawInterchangeMarker(GoogleMap map, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_circle))
                .position(position)
                .anchor(0.5f, 0.5f);
        return map.addMarker(markerOptions);
    }

    public static Marker drawMarker(GoogleMap map, LatLng position, float color, String label) {
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .title(label)
                .position(position);
        return map.addMarker(markerOptions);
    }

    public static Marker drawMarker(GoogleMap map, LatLng position, float color, String label, String description) {
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(color))
                .title(label)
                .snippet(description)
                .position(position);
        return map.addMarker(markerOptions);
    }
}