package com.example.malanglinejavamvvm.view;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.malanglinejavamvvm.R;
import com.example.malanglinejavamvvm.model.Line;
import com.example.malanglinejavamvvm.model.LocationModel;
import com.example.malanglinejavamvvm.model.PointTransport;
import com.example.malanglinejavamvvm.viewmodel.MapViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback ,GoogleMap.OnMapLongClickListener {
    private GoogleMap googleMap;
    private MapViewModel viewModel;
    private Marker currentLocationMarker,destinationMarker;
    private Polygon polygon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        viewModel.getLocation().observe(this, new Observer<LocationModel>() {
            @Override
            public void onChanged(LocationModel locationModel) {
                updateCurrentLocationMarker(locationModel.getLatLng());
                moveCameraToLocation(locationModel.getLatLng());
            }
        });

        viewModel.getLines().observe(this, new Observer<List<Line>>() {
            @Override
            public void onChanged(List<Line> lines) {
                if (googleMap != null) {
                    viewModel.bindPolylineToMap(googleMap);
                }
            }
        });



        // Obtain the SupportMapFragment and get notified when the map is ready to be used
       SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
       mapFragment.getMapAsync(this);

        // Parse the JSON data and update the ViewModel
        viewModel.AmbilPoints(getApplicationContext(), googleMap);
    }

    private void updateCurrentLocationMarker(LatLng latLng) {
        if (currentLocationMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title("Current Location");
            currentLocationMarker = googleMap.addMarker(markerOptions);
        } else {
            currentLocationMarker.setPosition(latLng);
        }
    }

    private void moveCameraToLocation(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(13f)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    public void onMapLongClick(LatLng latilongi){
        if (destinationMarker  != null ){
            destinationMarker.remove();
            destinationMarker = null;
            if (polygon != null){
                polygon.remove();
                polygon=null;
            }
        }
        MarkerOptions destinationMarkerOption = new MarkerOptions().position(latilongi).title("Tujuan");
        destinationMarker = googleMap.addMarker(destinationMarkerOption);

        PolygonOptions polygonOptions = new PolygonOptions()
                .add(currentLocationMarker.getPosition())  // Add current location marker position
                .add(destinationMarker.getPosition())  // Add destination marker position
                .strokeColor(Color.RED)  // Set the polygon stroke color
                .fillColor(Color.argb(100, 255, 0, 0));  // Set the polygon fill color with transparency

        // Add the polygon to the map
        polygon= googleMap.addPolygon(polygonOptions);
    }
    @Override
    protected void onStop() {
        super.onStop();

        // Stop location updates
        viewModel.stopLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        //View polyline in google maps
        viewModel.bindPolylineToMap(googleMap);

        // Start location updates
        viewModel.startLocationUpdates(this);

        // Set the map long click listener
        googleMap.setOnMapLongClickListener(this);
    }

}

