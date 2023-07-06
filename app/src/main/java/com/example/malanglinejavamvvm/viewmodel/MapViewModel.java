package com.example.malanglinejavamvvm.viewmodel;



import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.malanglinejavamvvm.R;
import com.example.malanglinejavamvvm.model.GraphTransport;
import com.example.malanglinejavamvvm.model.Interchange;
import com.example.malanglinejavamvvm.model.Line;
import com.example.malanglinejavamvvm.model.LocationModel;
import com.example.malanglinejavamvvm.model.PointTransport;
import com.example.malanglinejavamvvm.utilities.Service;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MapViewModel extends ViewModel {
    private MutableLiveData<LocationModel> location = new MutableLiveData<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    public LiveData<LocationModel> getLocation() {
        return location;
    }


    public void setLocation(LocationModel LocationModel) {
        location.setValue(LocationModel);
    }

    private GoogleMap googleMap;
    private final List<PolylineOptions> polylineOptionsList = new ArrayList<>();


    // You need to initialize the graphTransport instance in the constructor or using some other method


    private MutableLiveData<List<Line>> lineList = new MutableLiveData<>();

    private Handler handler;

    public void startLocationUpdates(final Context context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location loc = locationResult.getLastLocation();
                if (loc != null) {
                    LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                    setLocation(new LocationModel(latLng));
                }
            }
        };

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0000);
            return;
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stopLocationUpdates() {
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    public LiveData<List<Line>> getLines() {
        return lineList;
    }

    public void AmbilPoints(Context context, GoogleMap googleMap) {
        handler = new Handler();
        Service.getManagedPoints(context, new Service.IServiceInterface() {
            @Override
            public void onPointsObtained(ArrayList<Line> lines, ArrayList<Interchange> interchanges) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Set the list of Line and Interchange objects in the MutableLiveData
                        lineList.setValue(lines);
                    }
                });
            }

            @Override
            public void onPointsRequestError(String error) {
                // Handle the error case
            }
        });
    }

    public void bindPolylineToMap(GoogleMap googleMap) {
        List<Line> lines = lineList.getValue();

        if (lines != null && googleMap != null) {
            for (Line line : lines) {
                PolylineOptions polylineOptions = new PolylineOptions()
                        .color(line.color)
                        .width(10f);

                LinkedList<PointTransport> path = line.path;
                if (path != null) {
                    for (PointTransport pointTransport : path) {
                        LatLng latLng = new LatLng(pointTransport.lat, pointTransport.lng);
                        polylineOptions.add(latLng);
                    }

                    // Add the polyline to the map
                    googleMap.addPolyline(polylineOptions);
                }
            }
        }
    }

}