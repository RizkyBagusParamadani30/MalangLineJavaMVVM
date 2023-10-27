package com.example.malanglinejavamvvm.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Trace;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.malanglinejavamvvm.R;
import com.example.malanglinejavamvvm.databinding.MapActivityBinding;
import com.example.malanglinejavamvvm.model.Interchange;
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

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap googleMap;
    private MapViewModel viewModel;
    private Marker currentLocationMarker, destinationMarker;
    private RouteAdapter routeAdapter;
    private List<Marker> interchangeMarkers = new ArrayList<>();
    private List<Polyline> polylines = new ArrayList<>();

    private MapActivityBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        initViewModel();
        initView();
        initMap();
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this, new ViewModelFactory(getApplication())).get(MapViewModel.class);
    }

    private void initView() {
        binding = MapActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        RecyclerView recyclerView = binding.routeDetail;
        binding.cardContainer.setVisibility(View.GONE);

        binding.setViewModel(viewModel);
        binding.setLifecycleOwner(this);

        routeAdapter = new RouteAdapter(new ArrayList<>(), this::handleRouteItemClick);
        binding.routeDetail.setAdapter(routeAdapter);
        binding.routeDetail.setLayoutManager(new LinearLayoutManager(this));

        viewModel.getLocation().observe(this, this::updateCurrentLocationAndCamera);

        viewModel.getRoutes().observe(this, routes -> routeAdapter.setRoutes(new ArrayList<>(routes)));
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void updateCurrentLocationAndCamera(LocationModel locationModel) {
        updateCurrentLocationMarker(locationModel.getLatLng());
        moveCameraToLocation(locationModel.getLatLng());
    }

    private void handleRouteItemClick(RouteTransport routeTransport) {
        viewModel.handleRouteItemClick(routeTransport, googleMap, polylines, interchangeMarkers, currentLocationMarker, destinationMarker);
        binding.cardContainer.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        long waktuMulai = System.currentTimeMillis();
        googleMap = map;
        viewModel.startLocationUpdates(this);
        long waktuSelesai = System.currentTimeMillis();
        long waktu = waktuSelesai - waktuMulai;
        Log.d("Time", "waktu: " + waktu + " milliseconds");
        Toast.makeText(this, waktu +" milliseconds", Toast.LENGTH_SHORT).show();
        googleMap.setOnMapLongClickListener(this);

        viewModel.AmbilPoints(getApplicationContext(), googleMap);

        viewModel.getInterchanges().observe(this, interchanges -> {
            if (viewModel.getLines().getValue() != null && interchanges != null) {
                viewModel.loadGraph();
            }
        });

        googleMap.setOnInfoWindowClickListener(marker -> {
            if (marker.equals(destinationMarker)) {
                binding.cardContainer.setVisibility(View.VISIBLE);
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
                .zoom(11f)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onMapLongClick(LatLng latilongi) {
        CardView cardContainer = binding.cardContainer;
//        cardContainer.setVisibility(View.VISIBLE);

        for (Polyline polyline : polylines) {
            polyline.remove();
        }
        polylines.clear();

        if (viewModel.getGraph() == null) {
            Toast.makeText(this, "Graph is not ready yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (destinationMarker != null) {
            cardContainer.setVisibility(View.GONE);
            destinationMarker.remove();
            destinationMarker = null;
        }

        // Set the destination coordinates here
        LatLng destinationCoordinates = new LatLng(-7.971795618780245, 112.60223139077425);
        this.destinationMarker = MapUtilities.drawMarker(
                this.googleMap, destinationCoordinates, BitmapDescriptorFactory.HUE_GREEN, "Destination", "Tap to show route\nto this location");

        LatLng currentLocation = currentLocationMarker.getPosition();
        LatLng destination = destinationMarker.getPosition();
        viewModel.calculateShortestPathBetweenMarkers(this, currentLocation, destination);
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.stopLocationUpdates();
    }
}

