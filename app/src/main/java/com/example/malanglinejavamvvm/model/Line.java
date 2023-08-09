package com.example.malanglinejavamvvm.model;

import java.util.LinkedList;

public class Line {

    public int id;
    public String name;
    public String direction;
    public Integer color;

    public Double cost;
    public double distance = 0;

    public LinkedList<PointTransport> path = new LinkedList<>();

    public Line() {
    }

    public Line(int id, String name, int color, String direction) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.direction = direction;
    }
}

