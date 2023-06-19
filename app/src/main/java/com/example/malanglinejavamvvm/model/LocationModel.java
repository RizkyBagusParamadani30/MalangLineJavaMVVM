package com.example.malanglinejavamvvm.model;

import com.google.android.gms.maps.model.LatLng;

public class LocationModel {
    private LatLng latLng;

    public LocationModel(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}