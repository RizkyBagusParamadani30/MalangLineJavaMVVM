package com.example.malanglinejavamvvm.viewmodel;


import static android.os.Looper.getMainLooper;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.example.malanglinejavamvvm.R;
import com.example.malanglinejavamvvm.model.DijkstraTask;
import com.example.malanglinejavamvvm.model.DijkstraTransport;
import com.example.malanglinejavamvvm.model.GraphTask;
import com.example.malanglinejavamvvm.model.GraphTransport;
import com.example.malanglinejavamvvm.model.Interchange;
import com.example.malanglinejavamvvm.model.Line;
import com.example.malanglinejavamvvm.model.LocationModel;
import com.example.malanglinejavamvvm.model.PointTransport;
import com.example.malanglinejavamvvm.model.RouteTransport;
import com.example.malanglinejavamvvm.utilities.MapUtilities;
import com.example.malanglinejavamvvm.utilities.Service;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MapViewModel extends ViewModel {
    private MutableLiveData<LocationModel> location = new MutableLiveData<>();
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Marker endMarker;
    private Marker startMarker;

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

    private Polyline previousPolyline;
    ;

    public void setGraph(GraphTransport graph) {
        this.graph = graph;
        graphLivaData.setValue(graph);
    }

    private ArrayList<RouteTransport> routesList = new ArrayList<>();

    private Context context;

    private Application application;

    public MapViewModel(Application application) {
        this.application = application;
        context = application.getApplicationContext();
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

    public void removePolyline() {
        if (previousPolyline != null) {
            previousPolyline.remove();
            previousPolyline = null;
        }
    }

    public void calculateShortestPathBetweenMarkers(Context context, LatLng currentLocation, LatLng destination, int radius) {
        Log.d("MapViewModel", "calculateShortestPathBetweenMarkers called with currentLocation: " + currentLocation + ", destination: " + destination + ", radius: " + radius);
        if (graph != null) {
            // Inflate the custom layout for the progress dialog
            View customView = LayoutInflater.from(context).inflate(R.layout.custom_progress_dialog, null);
            // Create and show the progress dialog
            AlertDialog dijkstraDialog = new MaterialAlertDialogBuilder(context)
                    .setView(customView)
                    .setCancelable(false)
                    .show();

            DijkstraTask.DijkstraTaskListener dijkstraListener = new DijkstraTask.DijkstraTaskListener() {
                @Override
                public void onDijkstraProgress(DijkstraTask.DijkstraReport report) {
                    Log.d("MapViewModel", "Dijkstra Progress: " + report);
                    // Handle progress updates if needed
                }

                @Override
                public void onDijkstraComplete(ArrayList<RouteTransport> routes) {
                    // Store the routes in the class-level variable
                    routesList = routes;
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
                    dijkstraDialog.dismiss();
                }

                @Override
                public void onDijkstraError(Exception ex) {
                    Log.e("MapViewModel", "Dijkstra Error: ", ex);
                    // Handle any errors during the algorithm calculation
                    dijkstraDialog.dismiss();
                }
            };

            DijkstraTask dijkstraTask = new DijkstraTask(graph, currentLocation, destination,
                    DijkstraTransport.Priority.COST, DijkstraTransport.Priority.DISTANCE, radius, dijkstraListener);
            dijkstraTask.execute();
        } else {
            Log.d("MapViewModel", "Graph is null");
        }
    }


    public void loadGraph() {
        // Obtain the lines and interchanges from the ViewModel
        List<Line> lines = getLines().getValue();
        List<Interchange> interchanges = getInterchanges().getValue();
        if (lines != null && interchanges != null) {
            // Convert the lists to ArrayLists
            ArrayList<Line> linesArrayList = new ArrayList<>(lines);
            ArrayList<Interchange> interchangesArrayList = new ArrayList<>(interchanges);
            // Create a GraphTask to build the graph in the background
            GraphTask graphTask = new GraphTask(linesArrayList, interchangesArrayList, this::handleGraph);
            graphTask.execute();
        }
    }

    public void handleGraph(Set<PointTransport> points) {
        if (points != null && !points.isEmpty()) {
            // Graph is ready
            graph = new GraphTransport();
            graph.setTransportPoints(points);
            graphLivaData.setValue(graph);
            Toast.makeText(context, "Graph generated from " + points.size() + " points", Toast.LENGTH_SHORT).show();
        } else {
            // Graph is not ready
            Toast.makeText(context, "Graph is not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void drawPath(List<PointTransport> path, GoogleMap googleMap, Marker startMarker, Marker endMarker,
                          List<Polyline> polylines, List<Marker> markers) {
        PolylineOptions startWalkingPolylineOptions = MapUtilities.getWalkingPolylineOptions();
        startWalkingPolylineOptions.add(startMarker.getPosition()).add(endMarker.getPosition());
        polylines.add(googleMap.addPolyline(startWalkingPolylineOptions));
        PolylineOptions polylineOptions = new PolylineOptions().width(15);
        PointTransport prevPoint = null;
        for (PointTransport currentPoint : path) {
            if (prevPoint == null)
                polylineOptions.color(currentPoint.getColor());
            if (prevPoint != null && currentPoint.getIdLine() != prevPoint.getIdLine()) {
                // finish the polyline
                Polyline route = googleMap.addPolyline(polylineOptions);
                polylines.add(route);
                // draw interchange markers
                markers.add(MapUtilities.drawInterchangeMarker(googleMap, prevPoint.getLatLng()));
                markers.add(MapUtilities.drawInterchangeMarker(googleMap, currentPoint.getLatLng()));
                // draw interchange walking paths
                PolylineOptions transferWalkingPolylineOptions = MapUtilities.getWalkingPolylineOptions();
                transferWalkingPolylineOptions.add(prevPoint.getLatLng()).add(currentPoint.getLatLng());
                polylines.add(googleMap.addPolyline(transferWalkingPolylineOptions));
                // start next line polyline
                polylineOptions = new PolylineOptions().width(15).color(currentPoint.getColor());
            }
            // add current point
            polylineOptions.add(new LatLng(currentPoint.lat(), currentPoint.lng()));
            prevPoint = currentPoint;
        }
        polylines.add(googleMap.addPolyline(polylineOptions));
        assert prevPoint != null;
        markers.add(MapUtilities.drawInterchangeMarker(googleMap, prevPoint.getLatLng()));
        if (endMarker != null) {
            PolylineOptions endWalkingPolylineOptions = MapUtilities.getWalkingPolylineOptions();
            endWalkingPolylineOptions.add(endMarker.getPosition()).add(endMarker.getPosition());
            polylines.add(googleMap.addPolyline(endWalkingPolylineOptions));
        }
    }

    public void handleRouteItemClick(RouteTransport routeTransport, GoogleMap googleMap,
                                     List<Polyline> polylines, List<Marker> interchangeMarkers) {
        if (googleMap == null || routeTransport == null) {
            return;
        }

        LatLng startLatLng = routeTransport.getSource().getLatLng();
        LatLng endLatLng = routeTransport.getDestination().getLatLng();

        if (startLatLng == null || endLatLng == null) {
            return;
        }

        // Remove any existing markers before adding new ones
        removeMarkers();

        // Draw start and end markers
        Marker startMarker = MapUtilities.drawInterchangeMarker(googleMap, startLatLng);
        Marker endMarker = MapUtilities.drawInterchangeMarker(googleMap, endLatLng);

        List<PointTransport> path = routeTransport.getPath();
        if (path != null && !path.isEmpty()) {
            // Call the drawPath method passing the required parameters
            drawPath(path, googleMap, startMarker, endMarker, polylines, interchangeMarkers);
        }
    }

    private void removeMarkers() {
        // Clear any existing markers
        if (startMarker != null) {
            startMarker.remove();
            startMarker = null;
        }
        if (endMarker != null) {
            endMarker.remove();
            endMarker = null;
        }
    }

    private void clearInterchangeMarkers(List<Marker> interchangeMarkers) {
        for (Marker marker : interchangeMarkers) {
            marker.remove();
        }
        interchangeMarkers.clear();
    }
}