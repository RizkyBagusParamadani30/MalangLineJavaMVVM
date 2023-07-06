package com.example.malanglinejavamvvm.model;

import android.util.Pair;

import com.example.malanglinejavamvvm.utilities.CDM;
import com.example.malanglinejavamvvm.utilities.Helper;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class GraphTransport {
    private Set<PointTransport> pointTransports;

    private List<PolylineOptions> polylineOptionsList = new ArrayList<>();

    public void addPolyline(PolylineOptions polylineOptions) {
        polylineOptionsList.add(polylineOptions);
    }

    public List<PolylineOptions> getPolylineOptionsList() {
        return polylineOptionsList;
    }

    public void setTransportPoints(Set<PointTransport> transportPoints) {
        this.pointTransports = transportPoints;
    }

    public Set<PointTransport> getPointTransports(){
        return this.pointTransports;
    }

    @Deprecated
    public PointTransport getNearby(Double latitude, Double longitude) {
        if(this.pointTransports == null) return null;
        Double minDistance = Double.MAX_VALUE;
        PointTransport nearestPoint = null;
            for (PointTransport point: this.pointTransports) {
            Double distance = Helper.calculateDistance(point, latitude, longitude);
            if(distance < minDistance) {
                minDistance = distance;
                nearestPoint = point;
            }
        }
        return nearestPoint;
    }

    public Set<PointTransport> getSeveralNearby(Double latitude, Double longitude, int radius) {

        Set<PointTransport> nearbyPointTransports = new HashSet<>();

        if (this.pointTransports == null) return nearbyPointTransports;

        HashMap<PointTransport, Double> pointTransportMap = new HashMap<>();

        for (PointTransport point : this.pointTransports) {
            Double distance = Helper.calculateDistance(point, latitude, longitude);
            if (distance < (radius * CDM.oneMeterInDegree())) {

                boolean exists = false;
                PointTransport existingPoint = null;
                for (PointTransport p : pointTransportMap.keySet()) {
                    if (p.getIdLine() == point.getIdLine() && p.getDirection().equals(point.getDirection())) {
                        exists = true;
                        existingPoint = p;
                    }
                }
                if (!exists)
                    pointTransportMap.put(point, distance);
                else {
                    if (distance < pointTransportMap.get(existingPoint)) {
                        pointTransportMap.remove(existingPoint);
                        pointTransportMap.put(point, distance);
                    }
                }
            }
        }

        nearbyPointTransports.addAll(pointTransportMap.keySet());

        return nearbyPointTransports;
    }

    @Deprecated
    public static Set<PointTransport> build(ArrayList<PointTransport> points) {

        Set<PointTransport> pointSets = new HashSet<>();

        for (PointTransport pointTransport : points) {
            String adjacentPointId = pointTransport.getAdjacentPointId();
            for(PointTransport nextPointTransport : points) {
                if(adjacentPointId != null && adjacentPointId.equals(nextPointTransport.getId())) {
                    Double distance = Helper.calculateDistance(pointTransport, nextPointTransport);
                    PointTransport.TransportCost cost = new PointTransport.TransportCost(0D, distance);
                    pointTransport.addDestination(nextPointTransport, cost);
                    nextPointTransport.addSource(pointTransport, cost);
                }
            }

            String[] nextInterchangePoints = pointTransport.getInterchanges();
            if(nextInterchangePoints != null) {
                for(String nextInterchangePoint: nextInterchangePoints) {
                    for(PointTransport nextPointInterchange : points) {
                        if (nextInterchangePoint != null && nextInterchangePoint.equals(nextPointInterchange.getId())) {
                            PointTransport.TransportCost cost = new PointTransport.TransportCost(CDM.getStandardCost(), 0D);
                            pointTransport.addDestination(nextPointInterchange, cost);
                            nextPointInterchange.addSource(pointTransport, cost);
                        }
                    }
                }
            }

            pointSets.add(pointTransport);
        }

        return pointSets;
    }

    public static Set<PointTransport> build(ArrayList<Line> lines, ArrayList<Interchange> interchanges) {

        Set<PointTransport> pointSets = new HashSet<>();

        for(Line line: lines) {

            PointTransport prevPoint = null;
            for(PointTransport point : line.path) {
                if(prevPoint == null) {
                    prevPoint = point;
                    continue;
                } else {
                    Double distance = Helper.calculateDistance(prevPoint, point);
                    PointTransport.TransportCost cost = new PointTransport.TransportCost(0D, distance);
                    prevPoint.addDestination(point, cost);
                    point.addSource(prevPoint, cost);
                    prevPoint = point;
                }
                pointSets.add(point);
            }

        }

        // assign points to interchange...
        for(Interchange interchange: interchanges) {
            for (PointTransport point : pointSets) {
                for (String pointId : interchange.pointIds) {
                    if (point.id.equals(pointId))
                        interchange.points.add(point);
                }
            }
        }

        for(Interchange interchange: interchanges) {
            for(PointTransport sPoint : interchange.points) {
                for(PointTransport dPoint: interchange.points) {
                    if(sPoint.id.equals(dPoint.id)) continue;
                    PointTransport.TransportCost cost =
                            new PointTransport.TransportCost(
                                    CDM.getStandardCost(),
                                    0D);
                    sPoint.addDestination(dPoint, cost);
                    dPoint.addSource(sPoint, cost);
                }
            }
        }

        return pointSets;
    }

}
