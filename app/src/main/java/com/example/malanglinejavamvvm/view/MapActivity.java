package com.example.malanglinejavamvvm.view;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.malanglinejavamvvm.R;
import com.example.malanglinejavamvvm.SettingsActivity;
import com.example.malanglinejavamvvm.databinding.MapActivityBinding;
import com.example.malanglinejavamvvm.model.Interchange;
import com.example.malanglinejavamvvm.model.Line;
import com.example.malanglinejavamvvm.model.LocationModel;
import com.example.malanglinejavamvvm.model.RouteTransport;
import com.example.malanglinejavamvvm.utilities.CDM;
import com.example.malanglinejavamvvm.utilities.MapUtilities;
import com.example.malanglinejavamvvm.viewmodel.MapViewModel;
import com.example.malanglinejavamvvm.viewmodel.ViewModelFactory;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback ,GoogleMap.OnMapLongClickListener{
    private GoogleMap googleMap;
    private MapViewModel viewModel;
    private Marker currentLocationMarker,destinationMarker;
    private RouteAdapter routeAdapter;
    private RecyclerView recyclerView;
    private List<Marker> interchangeMarkers = new ArrayList<>();
    private List<Polyline> polylines = new ArrayList<>();

    private MapActivityBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        CDM.cost = Double.parseDouble(
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString("pref_cost", String.valueOf(CDM.cost)));

        binding = MapActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        recyclerView = binding.routeDetail;
        binding.cardContainer.setVisibility(View.GONE);

        // Inisialisasi viewModel
        viewModel = new ViewModelProvider(this, new ViewModelFactory(getApplication())).get(MapViewModel.class);
        binding.setViewModel(viewModel); // mengatur View model untuk data binding
        binding.setLifecycleOwner(this);

        viewModel.getLocation().observe(this, new Observer<LocationModel>() {
            @Override
            public void onChanged(LocationModel locationModel) {
                updateCurrentLocationMarker(locationModel.getLatLng());
                moveCameraToLocation(locationModel.getLatLng());
            }
        });
        // mengatur recyclerview dengan data binding
        routeAdapter = new RouteAdapter(new ArrayList<>(), routeTransport -> {
            viewModel.handleRouteItemClick(routeTransport, googleMap, polylines, interchangeMarkers, currentLocationMarker, destinationMarker);
            binding.cardContainer.setVisibility(View.GONE);
        });
        binding.routeDetail.setAdapter(routeAdapter);
        binding.routeDetail.setLayoutManager(new LinearLayoutManager(this));


        viewModel.getRoutes().observe(this, new Observer<List<RouteTransport>>() {
            @Override
            public void onChanged(List<RouteTransport> routes) {
                // update rute yang ada di adapter
                routeAdapter.setRoutes((ArrayList<RouteTransport>) routes);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(this, SettingsActivity.class);
        this.startActivity(i);
        return true;
    }
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Start location updates
        viewModel.startLocationUpdates(this);

        // Set the map long click listener
        googleMap.setOnMapLongClickListener(this);

        // Parse the JSON data and update the ViewModel
        viewModel.AmbilPoints(getApplicationContext(), googleMap);

//        // Observe the lines and interchanges data in the ViewModel
//        viewModel.getLines().observe(this, new Observer<List<Line>>() {
//            @Override
//            public void onChanged(List<Line> lines) {
//                // Check if both lines and interchanges data is available
//                if (lines != null && viewModel.getInterchanges().getValue() != null) {
//                    viewModel.loadGraph();
//                }
//            }
//        });

        viewModel.getInterchanges().observe(this, new Observer<List<Interchange>>() {
            @Override
            public void onChanged(List<Interchange> interchanges) {
                // Check if both lines and interchanges data is available
                if (viewModel.getLines().getValue() != null && interchanges != null) {
                    viewModel.loadGraph();
                }
            }
        });
       googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.equals(destinationMarker)) {
                    binding.cardContainer.setVisibility(View.VISIBLE);
                }
            }
        });

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
                .zoom(15f)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    public void onMapLongClick(LatLng latilongi){
        LinearLayout cardContainer = binding.cardContainer;
        cardContainer.setVisibility(View.VISIBLE);

        for (Polyline polyline : polylines) {
            polyline.remove();
        }
        polylines.clear();

        if (viewModel.getGraph() == null) {
            Toast.makeText(this, "Graph is not ready yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destinationMarker  != null ){
            cardContainer.setVisibility(View.GONE);
            destinationMarker.remove();
            destinationMarker = null;
        }
        this.destinationMarker = MapUtilities.drawMarker(
                this.googleMap, latilongi, BitmapDescriptorFactory.HUE_GREEN, "Destination", "Tap to show route\nto this location");

        LatLng currentLocation = currentLocationMarker.getPosition();
        LatLng destination= destinationMarker.getPosition();
        viewModel.calculateShortestPathBetweenMarkers(MapActivity.this,currentLocation, destination);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Stop location updates
        viewModel.stopLocationUpdates();
    }
}

