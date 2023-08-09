package com.example.malanglinejavamvvm.model;

import android.graphics.Color;

import com.example.malanglinejavamvvm.utilities.CDM;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PointTransport extends Point {

    protected String lineName;
    protected String direction;
    protected String adjacentPointId;
    protected String adjacentPointInterchangeIds;
    public Double lat;
    public Double lng;
    protected String color;
    protected Double price;

    protected Map<PointTransport, TransportCost> adjacentTransportPoints = new HashMap<>(); //point, cost

    // additional attributes

    protected Map<PointTransport, TransportCost> previousTransportPoints = new HashMap<>();

    private LinkedList<PointTransport> via = new LinkedList<>();
    private Map<PointTransport, LinkedHashMap<PointTransport, TransportCost>> mSubChains
            = new HashMap<>();

    public PointTransport() {
    }

    //"l":"1","n":"AL","d":"O","s":"0","a":"1526","i":null},
    public PointTransport(String id,
                          double lat,
                          double lng,
                          boolean stop,
                          int idLine,
                          String lineName,
                          String direction,
                          String color,
                          int sequence,
                          String adjacentPoints,
                          String interchanges) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.stop = stop;
        this.idLine = idLine;
        this.lineName = lineName;
        this.direction = direction;
        this.color = color;
        this.sequence = sequence;
        this.adjacentPointId = adjacentPoints;
        this.adjacentPointInterchangeIds = interchanges;
        this.price = CDM.getStandardCost();
    }

    public Double lat() {
        return this.lat;
    }

    public Double lng() {
        return this.lng;
    }

    public Boolean isStop() {
        return this.stop;
    }

    public String getId() {
        return this.id;
    }

    public int getIdLine() {
        return this.idLine;
    }

    public int getColor() {
        return Color.parseColor(this.color);
    }

    public String getLineName() {
        return this.lineName;
    }

    public Double getPrice() {
        return this.price;
    }

    public String getAdjacentPointId() {
        return this.adjacentPointId;
    }

    public String[] getInterchanges() {
        if (this.adjacentPointInterchangeIds != null)
            return this.adjacentPointInterchangeIds.split(",");
        else return null;
    }

    private List<PointTransport> cheapestPath = new LinkedList<>();
    private List<PointTransport> shortestPath = new LinkedList<>();
    private TransportCost transportCost = new TransportCost();

    public void addDestination(PointTransport destination, TransportCost cost) {
        this.adjacentTransportPoints.put(destination, cost);
    }

    public void clearDestination() {
        this.adjacentTransportPoints.clear();
        this.adjacentTransportPoints = new HashMap<>();
    }

    public Map<PointTransport, TransportCost> getAdjacentTransportPoints() {
        return this.adjacentTransportPoints;
    }

    public void setCost(TransportCost transportCost) {
        this.transportCost = transportCost;
    }

    public TransportCost getCost() {
        return this.transportCost;
    }

    public List<PointTransport> getCheapestPath() {
        return this.cheapestPath;
    }

    public List<PointTransport> getShortestPath() {
        return this.shortestPath;
    }

    public void setCheapestPath(LinkedList<PointTransport> cheapestPath) {
        this.cheapestPath = cheapestPath;
    }

    public void setShortestPath(LinkedList<PointTransport> shortestPath) {
        this.shortestPath = shortestPath;
    }

    public Double getCheapestPathCost() {
        Double cost = 0D;
        int currentLineId = 0;
        for (PointTransport p : this.cheapestPath) {
            if (cost == 0 && currentLineId == 0) {
                cost += p.price;
                currentLineId = p.getIdLine();
            }
            if (currentLineId != p.getIdLine()) {
                currentLineId = p.getIdLine();
                cost += p.price;
            }
        }
        return cost;
    }

    public void setCost(double price, double distance) {
        this.transportCost.distance = distance;
        this.transportCost.price = price;
    }

    public void setDistance(double distance) {
        this.transportCost.distance = distance;
    }

    public void setPrice(double price) {
        this.transportCost.price = price;
    }

    public String getDirection() {
        if (this.direction != null && this.direction.equals("O")) return "Outbound";
        if (this.direction != null && this.direction.equals("I")) return "Inbound";
        return direction;
    }

    public String getColorString() {
        return this.color;
    }

    public static class TransportCost {
        public Double price = Double.MAX_VALUE;
        public Double distance = Double.MAX_VALUE;

        public TransportCost() {
        }

        public TransportCost(Double price, Double distance) {
            this.price = price;
            this.distance = distance;
        }
    }

    // additional method

    public LatLng getLatLng() {
        return new LatLng(this.lat(), this.lng());
    }

    public void addSource(PointTransport source, TransportCost cost) {
        this.previousTransportPoints.put(source, cost);
    }

    public int getDegree() {
        return this.adjacentTransportPoints.size() + this.previousTransportPoints.size();
    }

    public boolean inMiddleOfChain() {
        return (this.adjacentTransportPoints.size() == 1 && this.previousTransportPoints.size() == 1);
    }

    public Map<PointTransport, TransportCost> getPreviousTransportPoints() {
        return this.previousTransportPoints;
    }

    public void addSubChain(PointTransport next, LinkedHashMap<PointTransport, TransportCost> subChain) {
        this.mSubChains.put(next, subChain);
    }

    public LinkedHashMap<PointTransport, TransportCost> getSubChain(PointTransport next) {
        return this.mSubChains.get(next);
    }

    public void clearPath() {
        this.cheapestPath = new LinkedList<>();
        this.shortestPath = new LinkedList<>();
    }


}
