package com.example.malanglinejavamvvm.model;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DijkstraTask extends AsyncTask<Void, DijkstraTask.DijkstraReport, ArrayList<RouteTransport>> {

    private int radius;
    private LatLng source;
    private LatLng destination;
    private DijkstraTransport.Priority priority;
    private DijkstraTaskListener listener;
    private GraphTransport graph;

    private DijkstraReport report;

    public DijkstraTask(GraphTransport graph, LatLng source, LatLng destination, DijkstraTransport.Priority priority, DijkstraTransport.Priority distance1, int distance, DijkstraTaskListener listener) {
        this.graph = graph;
        this.source = source;
        this.destination = destination;
        this.priority = priority;
        this.radius = distance;
        this.listener = listener;

        this.report = new DijkstraReport();
    }


    @Override
    protected ArrayList<RouteTransport> doInBackground(Void... voids) {

        try {

            Set<PointTransport> sources = this.graph.getSeveralNearby(this.source.latitude, this.source.longitude, this.radius);
            Set<PointTransport> destinations = this.graph.getSeveralNearby(this.destination.latitude, this.destination.longitude, this.radius);

            this.report.total = sources.size() * destinations.size();

            ArrayList<List<PointTransport>> paths = new ArrayList<>();
            ArrayList<RouteTransport> routeTransports = new ArrayList<>();

            this.report.progress = 0;
            for(PointTransport source: sources) {

                for (PointTransport destination : destinations) {

                    DijkstraTransport dijkstra = new DijkstraTransport(this.graph.getPointTransports());

                    dijkstra.calculateShortestPathFrom(source, this.priority);

                    List<PointTransport> path = (this.priority == DijkstraTransport.Priority.COST) ?
                            destination.getCheapestPath() : destination.getShortestPath();

                    if (path.size() > 0) {
                        path.add(destination);
                        paths.add(path);
                        RouteTransport routeTransport = new RouteTransport(source, destination, path);
                        routeTransports.add(routeTransport);
                        this.report.routeTransport = routeTransport;
                    }

                    this.report.progress++;
                    publishProgress(this.report);
                }

            }

            // sort the results
            Collections.sort(routeTransports, RouteTransport.getComparator(
                    priority == DijkstraTransport.Priority.COST ?
                            RouteTransport.ComparatorType.PRICE :
                            RouteTransport.ComparatorType.DISTANCE));

            return routeTransports;

        } catch(Exception ex) {
            if(this.listener != null) this.listener.onDijkstraError(ex);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(DijkstraReport... values) {
        super.onProgressUpdate(values);
        if(this.listener != null) this.listener.onDijkstraProgress(values[0]);
    }

    @Override
    protected void onPostExecute(ArrayList<RouteTransport> routeTransports) {
        super.onPostExecute(routeTransports);
        if(this.listener != null) this.listener.onDijkstraComplete(routeTransports);
    }

    public interface DijkstraTaskListener {
        void onDijkstraProgress(DijkstraReport report);
        void onDijkstraComplete(ArrayList<RouteTransport> routes);
        void onDijkstraError(Exception ex);
    }

    public class DijkstraReport {
        public int total;
        public int progress;
        public RouteTransport routeTransport;
    }
}
