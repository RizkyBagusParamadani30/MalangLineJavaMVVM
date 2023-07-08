package com.example.malanglinejavamvvm.view;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.malanglinejavamvvm.R;
import com.example.malanglinejavamvvm.model.GraphTask;
import com.example.malanglinejavamvvm.model.GraphTransport;
import com.example.malanglinejavamvvm.model.Interchange;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback ,GoogleMap.OnMapLongClickListener {
    private GoogleMap googleMap;
    private MapViewModel viewModel;
    private Marker currentLocationMarker,destinationMarker;
    private Polygon polygon;
    private GraphTransport graph;
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

        // Parse the JSON data and update the ViewModel
        viewModel.AmbilPoints(getApplicationContext(), googleMap);

        // Observe the lines and interchanges data in the ViewModel
        viewModel.getLines().observe(this, new Observer<List<Line>>() {
            @Override
            public void onChanged(List<Line> lines) {
                // Check if both lines and interchanges data is available
                if (lines != null && viewModel.getInterchanges().getValue() != null) {
                    loadGraph();
                }
            }
        });

        viewModel.getInterchanges().observe(this, new Observer<List<Interchange>>() {
            @Override
            public void onChanged(List<Interchange> interchanges) {
                // Check if both lines and interchanges data is available
                if (viewModel.getLines().getValue() != null && interchanges != null) {
                    loadGraph();
                }
            }
        });
    }

    private void loadGraph() {
        // Obtain the lines and interchanges from the ViewModel
        List<Line> lines = viewModel.getLines().getValue();
        List<Interchange> interchanges = viewModel.getInterchanges().getValue();
        if (lines != null && interchanges != null) {
            // Convert the lists to ArrayLists
            ArrayList<Line> linesArrayList = new ArrayList<>(lines);
            ArrayList<Interchange> interchangesArrayList = new ArrayList<>(interchanges);
            // Create a GraphTask to build the graph in the background
            GraphTask graphTask = new GraphTask(linesArrayList, interchangesArrayList, this::handleGraph);
            graphTask.execute();
        }
    }

    private void handleGraph(Set<PointTransport> points) {
        if (points != null && !points.isEmpty()) {
            // Graph is ready
            graph = new GraphTransport();
            graph.setTransportPoints(points);
            Toast.makeText(this, "Graph generated from " + points.size() + " points,",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Graph is not ready
            Toast.makeText(this, "Graph is not available", Toast.LENGTH_SHORT).show();
        }
        if (graph != null && googleMap != null) {
            // Create a PolylineOptions object to configure the polyline
            PolylineOptions polylineOptions = new PolylineOptions();
            // Set the color of the polyline to black
            polylineOptions.color(Color.BLACK);
            polylineOptions.width(1f);
            // Loop through each PointTransport in the graph
            for (PointTransport point : graph.getPointTransports()) {
                // Add the LatLng coordinates of the point to the polyline
                polylineOptions.add(new LatLng(point.lat, point.lng));
            }
            // Add the polyline to the map
            googleMap.addPolyline(polylineOptions);
        }
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

    }
    @Override
    protected void onStop() {
        super.onStop();

        // Stop location updates
        viewModel.stopLocationUpdates();
    }
}

