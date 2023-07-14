package com.example.malanglinejavamvvm.viewmodel;


import static android.os.Looper.getMainLooper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.malanglinejavamvvm.model.DijkstraTask;
import com.example.malanglinejavamvvm.model.DijkstraTransport;
import com.example.malanglinejavamvvm.model.GraphTransport;
import com.example.malanglinejavamvvm.model.Interchange;
import com.example.malanglinejavamvvm.model.Line;
import com.example.malanglinejavamvvm.model.LocationModel;
import com.example.malanglinejavamvvm.model.PointTransport;
import com.example.malanglinejavamvvm.model.RouteTransport;
import com.example.malanglinejavamvvm.utilities.Service;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

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
    private MutableLiveData<List<Line>> lineList = new MutableLiveData<>();
    public LiveData<List<Line>> getLines() {
        return lineList;
    }
    private MutableLiveData<List<Interchange>> interchangelist = new MutableLiveData<>();
    public LiveData<List<Interchange>> getInterchanges() {
        return interchangelist;
    }

    private MutableLiveData<GraphTransport> graphLivaData = new MutableLiveData<>();
    public LiveData<GraphTransport> getGraph() {
        return graphLivaData;
    }
    private GraphTransport graph;

    private Handler handler;

    private MutableLiveData<ArrayList<RouteTransport>> routeList = new MutableLiveData<>();

    public LiveData<ArrayList<RouteTransport>> getRoutes() {
        return routeList;
    }
    private Context context;

    private AlertDialog djikstraDialog;

    public void setGraph(GraphTransport graph) {
        this.graph = graph;
        graphLivaData.setValue(graph);
    }



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

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    public void stopLocationUpdates() {
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    public void AmbilPoints(Context context, GoogleMap googleMap) {
        handler = new Handler();
        Service.getManagedPoints(context, new Service.IServiceInterface() {
            @Override
            public void onPointsObtained(ArrayList<Line> lines, ArrayList<Interchange> interchanges) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Set the list of Line
                        lineList.setValue(lines);
                        // Set the list of Interchange
                        interchangelist.setValue(interchanges); // assuming you have a LiveData object for interchanges
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

    public void calculateShortestPathBetweenMarkers(LatLng currentLocation, LatLng destination, int radius) {
        Log.d("MapViewModel", "calculateShortestPathBetweenMarkers called with currentLocation: " + currentLocation + ", destination: " + destination + ", radius: " + radius);
        if (graph != null) {

            DijkstraTask.DijkstraTaskListener dijkstraListener = new DijkstraTask.DijkstraTaskListener() {
                @Override
                public void onDijkstraProgress(DijkstraTask.DijkstraReport report) {
                    Log.d("MapViewModel", "Dijkstra Progress: " + report);
                    // Handle progress updates if needed
                }

                @Override
                public void onDijkstraComplete(ArrayList<RouteTransport> routes) {
                    Log.d("MapViewModel", "Dijkstra Complete with routes: " + routes.size());
                    // Print the routes received
                    for (RouteTransport route : routes) {
                        Log.d("MapViewModel", "Route: " + route);
                    }

                    // Find the route matching the current location and destination
                    boolean routeFound = false;
                    for (RouteTransport route : routes) {
                        LatLng routeSourceLatLng = route.getSource().getLatLng();
                        LatLng routeDestinationLatLng = route.getDestination().getLatLng();

                        Log.d("MapViewModel", "Route source: " + routeSourceLatLng);
                        Log.d("MapViewModel", "Route destination: " + routeDestinationLatLng);
                        Log.d("MapViewModel", "Current location: " + currentLocation);
                        Log.d("MapViewModel", "Destination: " + destination);

                        if (Math.abs(routeSourceLatLng.latitude - currentLocation.latitude) < 100 &&
                                Math.abs(routeSourceLatLng.longitude - currentLocation.longitude) < 100 &&
                                Math.abs(routeDestinationLatLng.latitude - destination.latitude) < 100 &&
                                Math.abs(routeDestinationLatLng.longitude - destination.longitude) < 100) {
                            Log.d("MapViewModel", "Matching route found.");

                            List<PointTransport> path = route.getPath();
                            Log.d("MapViewModel", "Path: " + path);

                            Log.d("MapViewModel", "Source LatLng: " + route.getSource().getLatLng());
                            Log.d("MapViewModel", "Destination LatLng: " + route.getDestination().getLatLng());

                            if (path != null && !path.isEmpty()) {
                                Log.d("MapViewModel", "Path is not empty: " + path);
                                routeList.postValue(routes);
                            } else {
                                Log.d("MapViewModel", "Path is empty");
                            }

                            routeFound = true;
                            break;
                        }
                    }

                    if (!routeFound) {
                        Log.d("MapViewModel", "No matching route found for the given locations.");
                    }


                }

                @Override
                public void onDijkstraError(Exception ex) {
                    Log.e("MapViewModel", "Dijkstra Error: ", ex);
                    // Handle any errors during the algorithm calculation
                }
            };

            DijkstraTask dijkstraTask = new DijkstraTask(graph, currentLocation, destination,
                    DijkstraTransport.Priority.COST, DijkstraTransport.Priority.DISTANCE, radius, dijkstraListener);
            dijkstraTask.execute();
        } else {
            Log.d("MapViewModel", "Graph is null");
        }
    }
}