package com.example.malanglinejavamvvm.view;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.malanglinejavamvvm.R;
import com.example.malanglinejavamvvm.model.GraphTask;
import com.example.malanglinejavamvvm.model.GraphTransport;
import com.example.malanglinejavamvvm.model.Interchange;
import com.example.malanglinejavamvvm.model.Line;
import com.example.malanglinejavamvvm.model.LocationModel;
import com.example.malanglinejavamvvm.model.PointTransport;
import com.example.malanglinejavamvvm.model.RouteTransport;
import com.example.malanglinejavamvvm.utilities.MapUtilities;
import com.example.malanglinejavamvvm.viewmodel.MapViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback ,GoogleMap.OnMapLongClickListener {
    private GoogleMap googleMap;
    private MapViewModel viewModel;
    private Marker currentLocationMarker,destinationMarker;
    private GraphTransport graph;

    private RouteAdapter routeAdapter;
    private RecyclerView recyclerView;

    private boolean isRecyclerViewExpanded = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        LinearLayout cardContainer = findViewById(R.id.card_container);
        cardContainer.setVisibility(View.GONE);

        // Initialize the ViewModel
        viewModel = new ViewModelProvider(this).get(MapViewModel.class);
        viewModel.getLocation().observe(this, new Observer<LocationModel>() {
            @Override
            public void onChanged(LocationModel locationModel) {
                updateCurrentLocationMarker(locationModel.getLatLng());
                moveCameraToLocation(locationModel.getLatLng());
            }
        });

//        viewModel.getLines().observe(this, new Observer<List<Line>>() {
//            @Override
//            public void onChanged(List<Line> lines) {
//                if (googleMap != null) {
//                    viewModel.bindPolylineToMap(googleMap);
//                }
//            }
//        });

        // Initialize the routeAdapter and set it to the RecyclerView
        routeAdapter = new RouteAdapter(new ArrayList<>(), new RouteAdapter.RouteAdapterItemClickListener() {
            @Override
            public void onItemClick(RouteTransport routeTransport) {

                // Show the line corresponding to the clicked routeTransport on the map
                List<PointTransport> path = routeTransport.getPath();
                if (path != null && path.size() > 0) {
                    Log.d("MapActivity","Gambar Rute");
                    // Create a PolylineOptions object for the line
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .color(Color.RED)
                            .width(5f);
                    for (PointTransport pointTransport : path) {
                        LatLng latLng = new LatLng(pointTransport.lat(), pointTransport.lng());
                        polylineOptions.add(latLng);
                    }
                    // Add the polyline to the map
                    googleMap.addPolyline(polylineOptions);
                    // Minimize the RecyclerView
                    recyclerView.setVisibility(View.GONE);
                    isRecyclerViewExpanded = false;
                }
            }
        });
        this.recyclerView = findViewById(R.id.route_detail_container);
        this.recyclerView.setAdapter(routeAdapter);
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel.getRoutes().observe(this, new Observer<List<RouteTransport>>() {
            @Override
            public void onChanged(List<RouteTransport> routes) {
                // Update the route list in the adapter
                routeAdapter.setRoutes((ArrayList<RouteTransport>) routes);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
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
            viewModel.setGraph(graph);
            Toast.makeText(this, "Graph generated from " + points.size() + " points,",
                    Toast.LENGTH_SHORT).show();

        } else {
            // Graph is not ready
            Toast.makeText(this, "Graph is not available", Toast.LENGTH_SHORT).show();
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
        if (this.graph == null) {
            Toast.makeText(this, "Graph is not ready yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (destinationMarker  != null ){
            destinationMarker.remove();
            destinationMarker = null;
        }

        this.destinationMarker = MapUtilities.drawMarker(
                this.googleMap, latilongi, BitmapDescriptorFactory.HUE_GREEN, "Destination", "Tap to show route\nto this location");

        // Show the card_container
        LinearLayout cardContainer = findViewById(R.id.card_container);
        cardContainer.setVisibility(View.VISIBLE);

        // Handle the click on the card_container
        cardContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the click action here, e.g., expand/collapse the RecyclerView
                if (isRecyclerViewExpanded) {
                    recyclerView.setVisibility(View.GONE);
                    isRecyclerViewExpanded = false;
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    isRecyclerViewExpanded = true;
                }
            }
        });


        LatLng currentLocation = currentLocationMarker.getPosition();
        LatLng destination= destinationMarker.getPosition();
        int radius = 500; // Set your desired radius value here
        viewModel.calculateShortestPathBetweenMarkers(currentLocation, destination,radius);
    }
    @Override
    protected void onStop() {
        super.onStop();

        // Stop location updates
        viewModel.stopLocationUpdates();
    }
}

