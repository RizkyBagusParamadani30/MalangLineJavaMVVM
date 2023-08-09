package com.example.malanglinejavamvvm.model;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class DijkstraTransport {

    public enum Priority {
        COST,
        DISTANCE
    }

    DijkstraTransport(Set<PointTransport> graph) {
        for (PointTransport p : graph) {
            p.clearPath();
            p.setCost(new PointTransport.TransportCost());
        }
    }

    public void calculateShortestPathFrom(PointTransport source, Priority priority) {

        source.setCost(0D, 0D);

        Set<PointTransport> settledPoints = new HashSet<>();
        Set<PointTransport> unsettledPoints = new HashSet<>();
        unsettledPoints.add(source);
        PointTransport currentPoint;
        while (unsettledPoints.size() != 0) {

            if (priority == Priority.COST)
                currentPoint = getLowestPricePoint(unsettledPoints);
            else currentPoint = getLowestDistancePoint(unsettledPoints);

            unsettledPoints.remove(currentPoint);

            for (Map.Entry<PointTransport, PointTransport.TransportCost> adjacencyPair :
                    currentPoint.getAdjacentTransportPoints().entrySet()) {
                PointTransport adjacentPoint = adjacencyPair.getKey();
                PointTransport.TransportCost edgeWeight = adjacencyPair.getValue();
                if (!settledPoints.contains(adjacentPoint)) {
                    calculateMinimumPrice(adjacentPoint, edgeWeight, currentPoint);
                    calculateMinimumDistance(adjacentPoint, edgeWeight, currentPoint);
                    unsettledPoints.add(adjacentPoint);
                }
            }

            settledPoints.add(currentPoint);
        }
    }

    private static PointTransport getLowestPricePoint(Set<PointTransport> unsettledPoints) {
        PointTransport lowestCostPoint = null;
        PointTransport.TransportCost lowestCost = new PointTransport.TransportCost();
        for (PointTransport point : unsettledPoints) {
            PointTransport.TransportCost pointCost = point.getCost();
            if (pointCost.price < lowestCost.price) {
                lowestCost = pointCost;
                lowestCostPoint = point;
            }
        }
        return lowestCostPoint;
    }

    private static PointTransport getLowestDistancePoint(Set<PointTransport> unsettledPoints) {
        PointTransport lowestDistancePoint = null;
        PointTransport.TransportCost lowestDistance = new PointTransport.TransportCost();
        for (PointTransport point : unsettledPoints) {
            PointTransport.TransportCost pointDistance = point.getCost();
            if (pointDistance.distance < lowestDistance.distance) {
                lowestDistance = pointDistance;
                lowestDistancePoint = point;
            }
        }
        return lowestDistancePoint;
    }

    private static void calculateMinimumDistance(PointTransport evaluationPoint,
                                                 PointTransport.TransportCost edgeWeight, PointTransport sourcePoint) {
        PointTransport.TransportCost sourceCost = sourcePoint.getCost();
        if (sourceCost.distance + edgeWeight.distance < evaluationPoint.getCost().distance) {
            evaluationPoint.setDistance(sourceCost.distance + edgeWeight.distance);
            LinkedList<PointTransport> shortestPath = new LinkedList<>(sourcePoint.getShortestPath());
            shortestPath.add(sourcePoint);
            evaluationPoint.setShortestPath(shortestPath);
        }

    }

    private static void calculateMinimumPrice(PointTransport evaluationPoint,
                                              PointTransport.TransportCost edgeCost, PointTransport sourcePoint) {
        PointTransport.TransportCost sourceCost = sourcePoint.getCost();
        if (sourceCost.price + edgeCost.price < evaluationPoint.getCost().price) {
            evaluationPoint.setPrice(sourceCost.price + edgeCost.price);
            LinkedList<PointTransport> cheapestPath = new LinkedList<>(sourcePoint.getCheapestPath());
            cheapestPath.add(sourcePoint);
            evaluationPoint.setCheapestPath(cheapestPath);
        }

    }

}
